package com.example.android_vehicle_project;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * AdminProfileActivity displays a clean, read-only Admin Profile view matching the Customer Profile card layout.
 * Displays: Full Name Header, Username Header, First Name, Last Name, Phone Number, and a Logout button.
 */
public class AdminProfileActivity extends AppCompatActivity {

    // View declarations
    private TextView tvProfileName, tvProfileUsername, tvFirstName, tvLastName, tvPhone;
    private Button btnLogoutProfile;

    // Database helper & target admin username
    private DatabaseHandler dbHandler;
    private static final String CURRENT_ADMIN_USERNAME = "admin1";

    /**
     * Activity creation callback initializes toolbar, database helper, view bindings, and logout listener.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        // Toolbar setup with back arrow navigation
        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHandler = new DatabaseHandler(this);

        initViews();
        loadAdminProfile();

        // Logout button listener clears activity task stack
        btnLogoutProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Binds layout text views and logout button.
     */
    private void initViews() {
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvFirstName = findViewById(R.id.tvFirstName);
        tvLastName = findViewById(R.id.tvLastName);
        tvPhone = findViewById(R.id.tvPhone);
        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);
    }

    /**
     * Queries Admin profile record from SQLite database and binds values to read-only TextViews.
     */
    private void loadAdminProfile() {
        Cursor cursor = dbHandler.getUserByUsername(CURRENT_ADMIN_USERNAME);

        if (cursor != null && cursor.moveToFirst()) {
            int unameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_USERNAME);
            int fnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_FIRST_NAME);
            int lnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_LAST_NAME);
            int phoneIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PHONE);

            String uname = unameIdx != -1 ? cursor.getString(unameIdx) : CURRENT_ADMIN_USERNAME;
            String fname = fnameIdx != -1 ? cursor.getString(fnameIdx) : "Admin";
            String lname = lnameIdx != -1 ? cursor.getString(lnameIdx) : "One";
            String phone = phoneIdx != -1 ? cursor.getString(phoneIdx) : "N/A";

            tvProfileName.setText((fname + " " + lname).trim());
            tvProfileUsername.setText("Username: " + uname);
            tvFirstName.setText(fname);
            tvLastName.setText(lname);
            tvPhone.setText(phone);

            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            tvProfileName.setText("Admin One");
            tvProfileUsername.setText("Username: " + CURRENT_ADMIN_USERNAME);
            tvFirstName.setText("Admin");
            tvLastName.setText("One");
            tvPhone.setText("9999999991");
        }
    }
}
