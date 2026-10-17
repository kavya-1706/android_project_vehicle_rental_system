package com.example.android_vehicle_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    // Top bar interactive icons
    private ImageView ivMenu, ivNotifications;

    // Database handler instance to execute queries for our dashboard stats
    private DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Connect this Activity to the newly redesigned XML layout
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
     * This separates the initialization logic, keeping onCreate clean.
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

        // Toolbar Icons
        ivMenu = findViewById(R.id.ivMenu);
        ivNotifications = findViewById(R.id.ivNotifications);
    }

    /**
     * Fetches current data from the database and updates the UI Dashboard cards.
     * This ensures the admin sees up-to-date numbers as soon as they open the app.
     */
    private void loadDashboardStats() {
        // Fetch total vehicle count from the DB helper
        int totalVehicles = dbHandler.getTotalVehiclesCount();

        // Fetch total bookings count from the DB helper
        int totalBookings = dbHandler.getTotalBookingsCount();

        // Convert the integer counts to Strings and display them in the TextViews
        tvTotalVehicles.setText(String.valueOf(totalVehicles));
        tvTotalBookings.setText(String.valueOf(totalBookings));
    }

    /**
     * Centralized method to attach OnClickListeners to all interactive UI elements.
     * It handles navigation and simple interactive feedback.
     */
    private void setupClickListeners() {
        // Handle Top-Left Menu Icon Click (Placeholder for future side-drawer)
        ivMenu.setOnClickListener(v -> {
            Toast.makeText(this, "Menu Clicked", Toast.LENGTH_SHORT).show();
        });

        // Handle Top-Right Notification Bell Click
        ivNotifications.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications Clicked", Toast.LENGTH_SHORT).show();
        });

        // Handle "View Bookings" action click.
        // This opens the combined AdminRecordsActivity
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
            // Intent to take the user back to the Main (Login) Screen
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);

            // Critical Step: Clear the back-stack flags so the user cannot press
            // the Android 'Back' button and return to this authenticated dashboard.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Launch the Login Activity
            startActivity(intent);

            // Terminate the current Admin Dashboard Activity
            finish();
        });
    }



    // Note: When you return to the dashboard from another activity,
    // it's a good idea to refresh the stats in case data was modified.
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }
}
