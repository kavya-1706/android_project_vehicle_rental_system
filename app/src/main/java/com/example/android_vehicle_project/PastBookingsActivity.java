package com.example.android_vehicle_project;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PastBookingsActivity extends AppCompatActivity {

    private String username;
    private LinearLayout llBookingsContainer;
    private TextView tvNoBookings;
    private DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_bookings);

        Toolbar toolbar = findViewById(R.id.toolbarPastBookings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        llBookingsContainer = findViewById(R.id.llBookingsContainer);
        tvNoBookings = findViewById(R.id.tvNoBookings);

        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.isEmpty()) {
            username = "priya";
        }

        dbHandler = new DatabaseHandler(this);
        loadCustomerBookings();
    }

    private void loadCustomerBookings() {
        llBookingsContainer.removeAllViews();
        Cursor cursor = dbHandler.getUserBookings(username);

        if (cursor != null && cursor.getCount() > 0) {
            tvNoBookings.setVisibility(View.GONE);
            LayoutInflater inflater = LayoutInflater.from(this);

            while (cursor.moveToNext()) {
                int vehicleNameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_VEHICLE_NAME);
                int brandIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_BRAND);
                int modelIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_MODEL);
                int pickupIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PICKUP_DATE);
                int dropIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_DROP_DATE);
                int daysIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_TOTAL_DAYS);
                int amountIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_TOTAL_AMOUNT);
                int statusIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_BOOKING_STATUS);
                int paymentIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PAYMENT_STATUS);
                int payTimeIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PAYMENT_TIME);

                String vehicleName = vehicleNameIdx != -1 ? cursor.getString(vehicleNameIdx) : "Vehicle";
                String brand = brandIdx != -1 ? cursor.getString(brandIdx) : "";
                String model = modelIdx != -1 ? cursor.getString(modelIdx) : "";
                String pickupDate = pickupIdx != -1 ? cursor.getString(pickupIdx) : "";
                String dropDate = dropIdx != -1 ? cursor.getString(dropIdx) : "";
                int totalDays = daysIdx != -1 ? cursor.getInt(daysIdx) : 1;
                double totalAmount = amountIdx != -1 ? cursor.getDouble(amountIdx) : 0.0;
                String bookingStatus = statusIdx != -1 ? cursor.getString(statusIdx) : "completed";
                String paymentStatus = paymentIdx != -1 ? cursor.getString(paymentIdx) : "success";
                String paymentTime = payTimeIdx != -1 ? cursor.getString(payTimeIdx) : "";

                View cardView = inflater.inflate(R.layout.item_booking, llBookingsContainer, false);

                TextView tvVehicleName = cardView.findViewById(R.id.tvBookingVehicleName);
                TextView tvStatus = cardView.findViewById(R.id.tvBookingStatus);
                TextView tvBrand = cardView.findViewById(R.id.tvBookingVehicleBrand);
                TextView tvDates = cardView.findViewById(R.id.tvBookingDates);
                TextView tvAmount = cardView.findViewById(R.id.tvBookingTotalAmount);
                TextView tvPayment = cardView.findViewById(R.id.tvPaymentStatus);

                tvVehicleName.setText(vehicleName);
                tvBrand.setText(brand + " " + model);
                tvDates.setText(pickupDate + " to " + dropDate + " (" + totalDays + " days)");
                tvAmount.setText(String.format("₹%,.0f", totalAmount));
                tvStatus.setText(bookingStatus.toUpperCase());
                tvPayment.setText("Payment Status: " + paymentStatus.toUpperCase() + (paymentTime.isEmpty() ? "" : " at " + paymentTime));

                llBookingsContainer.addView(cardView);
            }
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            tvNoBookings.setVisibility(View.VISIBLE);
        }
    }
}
