package com.example.android_vehicle_project;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
 * It combines viewing bookings, payments, and feedback into one searchable, filterable screen.
 */
public class AdminRecordsActivity extends AppCompatActivity {

    private ImageView ivBack, ivSearchBookings;
    private TextView tabAll, tabUpcoming, tabOngoing, tabCompleted;
    private RecyclerView rvAdminBookings;
    
    private DatabaseHandler dbHandler;
    private BookingsAdapter adapter;
    private List<BookingRecord> allBookings; // Stores full dataset from DB
    private List<BookingRecord> filteredBookings; // Stores dataset currently displayed
    
    private String currentFilterStatus = "All"; // Active tab filter

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
        
        tabAll = findViewById(R.id.tabAll);
        tabUpcoming = findViewById(R.id.tabUpcoming);
        tabOngoing = findViewById(R.id.tabOngoing);
        tabCompleted = findViewById(R.id.tabCompleted);

        rvAdminBookings = findViewById(R.id.rvAdminBookings);
    }

    private void setupClickListeners() {
        // Back Button Logic
        ivBack.setOnClickListener(v -> finish()); // Closes this Activity, goes back to Dashboard

        // Search Button Logic (placeholder for search expansion)
        ivSearchBookings.setOnClickListener(v -> {
            Toast.makeText(this, "Search functionality to be implemented", Toast.LENGTH_SHORT).show();
        });

        // Tab Filter Logic
        tabAll.setOnClickListener(v -> applyFilter("All", tabAll));
        tabUpcoming.setOnClickListener(v -> applyFilter("upcoming", tabUpcoming));
        tabOngoing.setOnClickListener(v -> applyFilter("ongoing", tabOngoing));
        tabCompleted.setOnClickListener(v -> applyFilter("completed", tabCompleted));
    }

    private void setupRecyclerView() {
        allBookings = new ArrayList<>();
        filteredBookings = new ArrayList<>();
        
        adapter = new BookingsAdapter(filteredBookings);
        rvAdminBookings.setLayoutManager(new LinearLayoutManager(this));
        rvAdminBookings.setAdapter(adapter);
    }

    /**
     * Executes the JOIN query to fetch Bookings + User Info + Vehicle Info
     */
    private void loadDataFromDatabase() {
        allBookings.clear();
        Cursor cursor = dbHandler.getAllAdminRecords();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Parse DB row into a BookingRecord object
                int bookingId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_BOOKING_ID));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_BOOKING_STATUS));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_USERNAME));
                String vehicleName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_NAME));
                String vehicleNum = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_NUM));
                String pickup = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_PICKUP_DATE));
                String drop = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_DROP_DATE));

                BookingRecord record = new BookingRecord(bookingId, username, vehicleName, vehicleNum, pickup, drop, status);
                allBookings.add(record);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Initially show all
        applyFilter("All", tabAll);
    }

    /**
     * UI logic to switch tabs and filter the dataset
     */
    private void applyFilter(String status, TextView selectedTab) {
        currentFilterStatus = status;

        // Reset all tabs to unselected visual state
        tabAll.setBackgroundResource(R.drawable.bg_input);
        tabAll.setTextColor(getResources().getColor(R.color.text_gray));
        tabUpcoming.setBackgroundResource(R.drawable.bg_input);
        tabUpcoming.setTextColor(getResources().getColor(R.color.text_gray));
        tabOngoing.setBackgroundResource(R.drawable.bg_input);
        tabOngoing.setTextColor(getResources().getColor(R.color.text_gray));
        tabCompleted.setBackgroundResource(R.drawable.bg_input);
        tabCompleted.setTextColor(getResources().getColor(R.color.text_gray));

        // Highlight selected tab
        selectedTab.setBackgroundResource(R.drawable.bg_tab_selected);
        selectedTab.setTextColor(getResources().getColor(R.color.text_dark));

        // Filter the dataset
        filteredBookings.clear();
        if (status.equals("All")) {
            filteredBookings.addAll(allBookings);
        } else {
            for (BookingRecord record : allBookings) {
                if (record.status.equalsIgnoreCase(status)) {
                    filteredBookings.add(record);
                }
            }
        }
        
        // Notify the adapter to refresh the RecyclerView
        adapter.notifyDataSetChanged();
    }


    // ------------------ INNER CLASSES FOR RECYCLER VIEW ------------------ //

    /**
     * Simple POJO (Plain Old Java Object) representing a combined Booking row
     */
    private static class BookingRecord {
        int bookingId;
        String customerName;
        String vehicleName;
        String vehicleNum;
        String pickupDate;
        String dropDate;
        String status;

        public BookingRecord(int bookingId, String customerName, String vehicleName, String vehicleNum, String pickupDate, String dropDate, String status) {
            this.bookingId = bookingId;
            this.customerName = customerName;
            this.vehicleName = vehicleName;
            this.vehicleNum = vehicleNum;
            this.pickupDate = pickupDate;
            this.dropDate = dropDate;
            this.status = status;
        }
    }

    /**
     * RecyclerView Adapter to bind BookingRecord data to the XML view (item_admin_booking.xml)
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

            holder.tvCustomerName.setText(record.customerName);
            holder.tvVehicleDetails.setText(record.vehicleName);
            holder.tvVehicleNum.setText(record.vehicleNum);
            
            // Format dates (e.g. 2026-10-15 - 2026-10-17)
            holder.tvDates.setText(record.pickupDate + " to " + record.dropDate);

            // Set Status tag text and styling
            holder.tvStatusTag.setText(record.status.toUpperCase());
            if (record.status.equalsIgnoreCase("completed")) {
                holder.tvStatusTag.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (record.status.equalsIgnoreCase("ongoing")) {
                holder.tvStatusTag.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            } else {
                holder.tvStatusTag.setTextColor(getResources().getColor(R.color.button_orange));
            }

            // Click listener for row to open full details (placeholder for now)
            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(AdminRecordsActivity.this, "Opening details for Booking #" + record.bookingId, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCustomerName, tvVehicleDetails, tvVehicleNum, tvDates, tvStatusTag;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
                tvVehicleDetails = itemView.findViewById(R.id.tvVehicleDetails);
                tvVehicleNum = itemView.findViewById(R.id.tvVehicleNum);
                tvDates = itemView.findViewById(R.id.tvDates);
                tvStatusTag = itemView.findViewById(R.id.tvStatusTag);
            }
        }
    }
}