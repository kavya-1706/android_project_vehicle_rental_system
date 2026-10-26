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
 * AdminRecordsActivity handles the "View Bookings" module for System Administrators.
 * Features:
 * 1. Queries joined booking, customer, and vehicle records from SQLite database.
 * 2. Computes dynamic real-time statuses ('UPCOMING', 'ONGOING', 'COMPLETED').
 * 3. Binds all details inline on item_admin_booking.xml card views, including customer feedback comments.
 */
public class AdminRecordsActivity extends AppCompatActivity {

    // View declarations
    private ImageView ivBack, ivSearchBookings;
    private RecyclerView rvAdminBookings;

    // Database helper & dataset
    private DatabaseHandler dbHandler;
    private BookingsAdapter adapter;
    private List<BookingRecord> bookingsList;

    /**
     * Activity creation callback initializes database helper, views, RecyclerView, and loads records.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_bookings);

        dbHandler = new DatabaseHandler(this);

        initViews();
        setupClickListeners();
        setupRecyclerView();

        // Load records from SQLite database
        loadDataFromDatabase();
    }

    /**
     * Binds layout views.
     */
    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivSearchBookings = findViewById(R.id.ivSearchBookings);
        rvAdminBookings = findViewById(R.id.rvAdminBookings);
    }

    /**
     * Attaches click listeners for toolbar navigation.
     */
    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivSearchBookings.setOnClickListener(v -> {
            Toast.makeText(this, "Search functionality to be implemented", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Configures RecyclerView layout manager and adapter.
     */
    private void setupRecyclerView() {
        bookingsList = new ArrayList<>();
        adapter = new BookingsAdapter(bookingsList);
        rvAdminBookings.setLayoutManager(new LinearLayoutManager(this));
        rvAdminBookings.setAdapter(adapter);
    }

    /**
     * Executes SQLite JOIN query (Bookings + Users + Vehicles) and populates dataset.
     */
    private void loadDataFromDatabase() {
        bookingsList.clear();
        Cursor cursor = dbHandler.getAllAdminRecords();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Parse booking and customer fields
                int bookingId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_BOOKING_ID));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_USERNAME));

                int fnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_FIRST_NAME);
                int lnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_LAST_NAME);
                String fname = fnameIdx != -1 ? cursor.getString(fnameIdx) : "";
                String lname = lnameIdx != -1 ? cursor.getString(lnameIdx) : "";
                String customerName = (fname + " " + lname).trim();
                if (customerName.isEmpty()) customerName = username;

                // Parse vehicle fields
                String vehicleName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_NAME));

                int brandIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_BRAND);
                int modelIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_MODEL);
                String brand = brandIdx != -1 ? cursor.getString(brandIdx) : "";
                String model = modelIdx != -1 ? cursor.getString(modelIdx) : "";

                String vehicleNum = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_NUM));
                String pickup = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_PICKUP_DATE));
                String drop = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_DROP_DATE));

                // Calculate dynamic status based on current date
                String status = BookingUtils.calculateStatus(pickup, drop);

                int daysIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_TOTAL_DAYS);
                int amountIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_TOTAL_AMOUNT);
                int paymentIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PAYMENT_STATUS);
                int payTimeIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PAYMENT_TIME);
                int feedbackIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_FEEDBACK);
                int ratingIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_RATING);

                int totalDays = daysIdx != -1 ? cursor.getInt(daysIdx) : 1;
                double totalAmount = amountIdx != -1 ? cursor.getDouble(amountIdx) : 0.0;
                String paymentStatus = paymentIdx != -1 ? cursor.getString(paymentIdx) : "success";
                String paymentTime = payTimeIdx != -1 ? cursor.getString(payTimeIdx) : "";
                String feedback = feedbackIdx != -1 ? cursor.getString(feedbackIdx) : "";
                double rating = ratingIdx != -1 ? cursor.getDouble(ratingIdx) : 0.0;

                BookingRecord record = new BookingRecord(bookingId, customerName, username, vehicleName, brand, model,
                        vehicleNum, pickup, drop, totalDays, totalAmount, status, paymentStatus, paymentTime, feedback, rating);
                bookingsList.add(record);
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Data model class representing a combined admin booking record.
     */
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
        String feedback;
        double rating;

        public BookingRecord(int bookingId, String customerName, String username, String vehicleName, String brand,
                             String model, String vehicleNum, String pickupDate, String dropDate, int totalDays,
                             double totalAmount, String status, String paymentStatus, String paymentTime,
                             String feedback, double rating) {
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
            this.feedback = feedback;
            this.rating = rating;
        }
    }

    /**
     * RecyclerView Adapter binding BookingRecord fields to item_admin_booking.xml views.
     */
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

            // Bind customer & vehicle info
            holder.tvCustomerName.setText(record.customerName + " (@" + record.username + ")");
            holder.tvVehicleDetails.setText(record.vehicleName + " (" + record.brand + " " + record.model + ")");
            holder.tvVehicleNum.setText("Vehicle No: " + record.vehicleNum);
            holder.tvDates.setText(record.pickupDate + " to " + record.dropDate + " (" + record.totalDays + " days)");
            holder.tvTotalAmount.setText(String.format("₹%,.0f", record.totalAmount));

            // Status tag
            holder.tvStatusTag.setText(record.status.toUpperCase());

            // Payment status
            holder.tvPaymentStatus.setText("Payment: " + record.paymentStatus.toUpperCase()
                    + (record.paymentTime.isEmpty() ? "" : " at " + record.paymentTime));

            // Customer feedback comment display
            if (record.feedback != null && !record.feedback.trim().isEmpty()) {
                holder.tvAdminFeedback.setVisibility(View.VISIBLE);
                holder.tvAdminFeedback.setText("Customer Feedback: \"" + record.feedback + "\"");
            } else {
                holder.tvAdminFeedback.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCustomerName, tvVehicleDetails, tvVehicleNum, tvDates, tvTotalAmount, tvStatusTag, tvPaymentStatus, tvAdminFeedback;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
                tvVehicleDetails = itemView.findViewById(R.id.tvVehicleDetails);
                tvVehicleNum = itemView.findViewById(R.id.tvVehicleNum);
                tvDates = itemView.findViewById(R.id.tvDates);
                tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
                tvStatusTag = itemView.findViewById(R.id.tvStatusTag);
                tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
                tvAdminFeedback = itemView.findViewById(R.id.tvAdminFeedback);
            }
        }
    }
}
