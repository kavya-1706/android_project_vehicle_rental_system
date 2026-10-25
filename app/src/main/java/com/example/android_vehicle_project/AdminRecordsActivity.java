package com.example.android_vehicle_project;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminRecordsActivity maps to the "View Bookings" module.
 * Displays all customer booking records and vehicle details inline.
 */
public class AdminRecordsActivity extends AppCompatActivity {

    private ImageView ivBack, ivSearchBookings;
    private RecyclerView rvAdminBookings;

    private DatabaseHandler dbHandler;
    private BookingsAdapter adapter;
    private List<BookingRecord> bookingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_bookings);

        dbHandler = new DatabaseHandler(this);

        initViews();
        setupClickListeners();
        setupRecyclerView();

        // Load data from SQLite
        loadDataFromDatabase();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivSearchBookings = findViewById(R.id.ivSearchBookings);
        rvAdminBookings = findViewById(R.id.rvAdminBookings);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivSearchBookings.setOnClickListener(v -> {
            Toast.makeText(this, "Search functionality to be implemented", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        bookingsList = new ArrayList<>();
        adapter = new BookingsAdapter(bookingsList);
        rvAdminBookings.setLayoutManager(new LinearLayoutManager(this));
        rvAdminBookings.setAdapter(adapter);
    }

    /**
     * Executes the JOIN query to fetch Bookings + User Info + Vehicle Info
     */
    private void loadDataFromDatabase() {
        bookingsList.clear();
        Cursor cursor = dbHandler.getAllAdminRecords();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int bookingId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_BOOKING_ID));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_BOOKING_STATUS));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_USERNAME));

                int fnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_FIRST_NAME);
                int lnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_LAST_NAME);
                String fname = fnameIdx != -1 ? cursor.getString(fnameIdx) : "";
                String lname = lnameIdx != -1 ? cursor.getString(lnameIdx) : "";
                String customerName = (fname + " " + lname).trim();
                if (customerName.isEmpty()) customerName = username;

                String vehicleName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_NAME));

                int brandIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_BRAND);
                int modelIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_MODEL);
                String brand = brandIdx != -1 ? cursor.getString(brandIdx) : "";
                String model = modelIdx != -1 ? cursor.getString(modelIdx) : "";

                String vehicleNum = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_NUM));
                String pickup = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_PICKUP_DATE));
                String drop = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_DROP_DATE));

                int daysIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_TOTAL_DAYS);
                int amountIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_TOTAL_AMOUNT);
                int paymentIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PAYMENT_STATUS);
                int payTimeIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PAYMENT_TIME);

                int totalDays = daysIdx != -1 ? cursor.getInt(daysIdx) : 1;
                double totalAmount = amountIdx != -1 ? cursor.getDouble(amountIdx) : 0.0;
                String paymentStatus = paymentIdx != -1 ? cursor.getString(paymentIdx) : "success";
                String paymentTime = payTimeIdx != -1 ? cursor.getString(payTimeIdx) : "";

                BookingRecord record = new BookingRecord(bookingId, customerName, username, vehicleName, brand, model,
                        vehicleNum, pickup, drop, totalDays, totalAmount, status, paymentStatus, paymentTime);
                bookingsList.add(record);
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    // ------------------ INNER CLASSES FOR RECYCLER VIEW ------------------ //

    private static class BookingRecord {
        int bookingId;
        String customerName;
        String username;
        String vehicleName;
        String brand;
        String model;
        String vehicleNum;
        String pickupDate;
        String dropDate;
        int totalDays;
        double totalAmount;
        String status;
        String paymentStatus;
        String paymentTime;

        public BookingRecord(int bookingId, String customerName, String username, String vehicleName, String brand,
                             String model, String vehicleNum, String pickupDate, String dropDate, int totalDays,
                             double totalAmount, String status, String paymentStatus, String paymentTime) {
            this.bookingId = bookingId;
            this.customerName = customerName;
            this.username = username;
            this.vehicleName = vehicleName;
            this.brand = brand;
            this.model = model;
            this.vehicleNum = vehicleNum;
            this.pickupDate = pickupDate;
            this.dropDate = dropDate;
            this.totalDays = totalDays;
            this.totalAmount = totalAmount;
            this.status = status;
            this.paymentStatus = paymentStatus;
            this.paymentTime = paymentTime;
        }
    }

    private class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.ViewHolder> {

        private final List<BookingRecord> items;

        public BookingsAdapter(List<BookingRecord> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_booking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BookingRecord record = items.get(position);

            holder.tvCustomerName.setText(record.customerName + " (@" + record.username + ")");
            holder.tvVehicleDetails.setText(record.vehicleName + " (" + record.brand + " " + record.model + ")");
            holder.tvVehicleNum.setText("Vehicle No: " + record.vehicleNum);
            holder.tvDates.setText(record.pickupDate + " to " + record.dropDate + " (" + record.totalDays + " days)");
            holder.tvTotalAmount.setText(String.format("₹%,.0f", record.totalAmount));

            holder.tvStatusTag.setText(record.status.toUpperCase());
            if (record.status.equalsIgnoreCase("completed")) {
                holder.tvStatusTag.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (record.status.equalsIgnoreCase("ongoing")) {
                holder.tvStatusTag.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            } else {
                holder.tvStatusTag.setTextColor(getResources().getColor(R.color.button_orange));
            }

            holder.tvPaymentStatus.setText("Payment: " + record.paymentStatus.toUpperCase()
                    + (record.paymentTime.isEmpty() ? "" : " at " + record.paymentTime));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCustomerName, tvVehicleDetails, tvVehicleNum, tvDates, tvTotalAmount, tvStatusTag, tvPaymentStatus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
                tvVehicleDetails = itemView.findViewById(R.id.tvVehicleDetails);
                tvVehicleNum = itemView.findViewById(R.id.tvVehicleNum);
                tvDates = itemView.findViewById(R.id.tvDates);
                tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
                tvStatusTag = itemView.findViewById(R.id.tvStatusTag);
                tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            }
        }
    }
}
