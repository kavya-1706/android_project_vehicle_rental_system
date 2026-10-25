package com.example.android_vehicle_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * AdminDashboardActivity serves as the Home Page for the Admin users.
 * It provides a high-level view of the system statistics (Total Vehicles, Total Bookings)
 * and acts as a central hub for navigating to other management modules using Quick Actions.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    // ---------------- UI Element Declarations ---------------- //
    // Text views to display statistics dynamically
    private TextView tvTotalVehicles, tvTotalBookings;

    // Linear layouts that act as clickable buttons for our Quick Actions
    private LinearLayout actionViewBookings, actionManageVehicles, actionManageUsers, actionProfile, actionLogout;

    // Database handler instance to execute queries for our dashboard stats
    private DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize the DatabaseHandler to communicate with SQLite
        dbHandler = new DatabaseHandler(this);

        // Bind Java view variables to the actual XML UI components
        initViews();

        // Retrieve and display dynamic statistics from the database
        loadDashboardStats();

        // Attach user interaction logic (clicks) to our UI elements
        setupClickListeners();
    }

    /**
     * Finds and assigns all UI components from the XML layout to Java variables.
     */
    private void initViews() {
        // Statistics labels
        tvTotalVehicles = findViewById(R.id.tvTotalVehicles);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);

        // Quick Action cards (used as large buttons)
        actionViewBookings = findViewById(R.id.actionViewBookings);
        actionManageVehicles = findViewById(R.id.actionManageVehicles);
        actionManageUsers = findViewById(R.id.actionManageUsers);
        actionProfile = findViewById(R.id.actionProfile);
        actionLogout = findViewById(R.id.actionLogout);
    }

    /**
     * Fetches current data from the database and updates the UI Dashboard cards.
     */
    private void loadDashboardStats() {
        int totalVehicles = dbHandler.getTotalVehiclesCount();
        int totalBookings = dbHandler.getTotalBookingsCount();

        tvTotalVehicles.setText(String.valueOf(totalVehicles));
        tvTotalBookings.setText(String.valueOf(totalBookings));
    }

    /**
     * Centralized method to attach OnClickListeners to all interactive UI elements.
     */
    private void setupClickListeners() {
        // Handle "View Bookings" action click
        actionViewBookings.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminRecordsActivity.class);
            startActivity(intent);
        });

        // Handle "Manage Vehicles" action click
        actionManageVehicles.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminManageVehiclesActivity.class);
            startActivity(intent);
        });

        // Handle "Manage Users" action click
        actionManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminManageUsersActivity.class);
            startActivity(intent);
        });

        // Handle "Profile" action click
        actionProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminProfileActivity.class);
            startActivity(intent);
        });

        // Handle "Logout" action click securely
        actionLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }
}
