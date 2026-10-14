package com.example.android_vehicle_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CustomerProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileUsername, tvProfileEmail, tvProfilePhone, tvProfileAddress, tvProfileLicense;
    private Button btnLogoutProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileAddress = findViewById(R.id.tvProfileAddress);
        tvProfileLicense = findViewById(R.id.tvProfileLicense);
        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);

        String username = getIntent().getStringExtra("USERNAME");
        if (username == null) username = "abcd";

        DatabaseHandler dbHandler = new DatabaseHandler(this);
        android.database.Cursor cursor = dbHandler.getUserByUsername(username);

        if (cursor != null && cursor.moveToFirst()) {
            int fnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_FIRST_NAME);
            int lnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_LAST_NAME);
            int emailIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_EMAIL);
            int phoneIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PHONE);
            int addressIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_ADDRESS);
            int licenseIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_LICENSE);

            String fname = fnameIdx != -1 ? cursor.getString(fnameIdx) : "";
            String lname = lnameIdx != -1 ? cursor.getString(lnameIdx) : "";
            String email = emailIdx != -1 ? cursor.getString(emailIdx) : "";
            String phone = phoneIdx != -1 ? cursor.getString(phoneIdx) : "";
            String address = addressIdx != -1 ? cursor.getString(addressIdx) : "";
            String license = licenseIdx != -1 ? cursor.getString(licenseIdx) : "";
            cursor.close();

            tvProfileName.setText(fname + " " + lname);
            tvProfileUsername.setText("Username: " + username);
            tvProfileEmail.setText(email);
            tvProfilePhone.setText(phone);
            tvProfileAddress.setText(address);
            tvProfileLicense.setText(license);
        } else {
            SharedPreferences prefs = getSharedPreferences("RideRentPrefs", MODE_PRIVATE);

            if (username.equals("priya")) {
                tvProfileName.setText("Priya Sharma");
                tvProfileUsername.setText("Username: priya");
                tvProfileEmail.setText("priya@email.com");
                tvProfilePhone.setText("+91 98765 43210");
                tvProfileAddress.setText("MG Road, Bangalore");
                tvProfileLicense.setText("KA01 20230099887");
            } else {
                String fname = prefs.getString("fname_" + username, "Jane");
                String lname = prefs.getString("lname_" + username, "Doe");
                String email = prefs.getString("email_" + username, "jane@email.com");
                String phone = prefs.getString("phone_" + username, "+91 90000 00000");
                String address = prefs.getString("address_" + username, "Street, City");
                String license = prefs.getString("license_" + username, "KA00 20230012345");

                tvProfileName.setText(fname + " " + lname);
                tvProfileUsername.setText("Username: " + username);
                tvProfileEmail.setText(email);
                tvProfilePhone.setText(phone);
                tvProfileAddress.setText(address);
                tvProfileLicense.setText(license);
            }
        }

        btnLogoutProfile.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
