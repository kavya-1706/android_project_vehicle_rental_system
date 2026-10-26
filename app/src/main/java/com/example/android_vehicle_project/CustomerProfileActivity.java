package com.example.android_vehicle_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * CustomerProfileActivity displays customer profile details in read-only mode and
 * allows editing personal information (First Name, Last Name, Email, Phone, Address, Driving License).
 * Updates are saved to both SQLite database and SharedPreferences.
 */
public class CustomerProfileActivity extends AppCompatActivity {

    // Read-only text views
    private TextView tvProfileName, tvProfileUsername, tvProfileEmail, tvProfilePhone, tvProfileAddress, tvProfileLicense;
    
    // Editable input fields
    private EditText etEditFirstName, etEditLastName, etEditEmail, etEditPhone, etEditAddress, etEditLicense;
    
    // Card containers & action buttons
    private LinearLayout cardProfileView, cardProfileEdit;
    private Button btnEditProfile, btnSaveProfile, btnCancelEdit, btnLogoutProfile;
    private ScrollView scrollViewProfile;

    // Database helper & active user credentials
    private DatabaseHandler dbHandler;
    private String username;
    private String currentFirstName = "Priya";
    private String currentLastName = "Sharma";

    /**
     * Initializes activity components, binds views, auto-scroll focus listeners, and button handlers on creation.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        dbHandler = new DatabaseHandler(this);

        // Setup toolbar back navigation
        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Extract customer username intent extra
        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.isEmpty()) {
            username = "priya";
        }

        // Bind ScrollView
        scrollViewProfile = findViewById(R.id.scrollViewProfile);

        // Bind Read-Only View components
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileAddress = findViewById(R.id.tvProfileAddress);
        tvProfileLicense = findViewById(R.id.tvProfileLicense);

        // Bind Edit Mode input components
        etEditFirstName = findViewById(R.id.etEditFirstName);
        etEditLastName = findViewById(R.id.etEditLastName);
        etEditEmail = findViewById(R.id.etEditEmail);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditAddress = findViewById(R.id.etEditAddress);
        etEditLicense = findViewById(R.id.etEditLicense);

        // Auto-scroll focus listener keeping focused input fields visible above soft keyboard
        View.OnFocusChangeListener autoScrollFocusListener = (v, hasFocus) -> {
            if (hasFocus && scrollViewProfile != null) {
                scrollViewProfile.postDelayed(() -> {
                    scrollViewProfile.smoothScrollBy(0, 150);
                }, 200);
            }
        };

        etEditFirstName.setOnFocusChangeListener(autoScrollFocusListener);
        etEditLastName.setOnFocusChangeListener(autoScrollFocusListener);
        etEditEmail.setOnFocusChangeListener(autoScrollFocusListener);
        etEditPhone.setOnFocusChangeListener(autoScrollFocusListener);
        etEditAddress.setOnFocusChangeListener(autoScrollFocusListener);
        etEditLicense.setOnFocusChangeListener(autoScrollFocusListener);

        // Bind Containers & Action Buttons
        cardProfileView = findViewById(R.id.cardProfileView);
        cardProfileEdit = findViewById(R.id.cardProfileEdit);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);

        // Load initial customer profile data from SQLite / SharedPreferences
        loadProfileData();

        // Button action listeners
        btnEditProfile.setOnClickListener(v -> switchToEditMode());
        btnCancelEdit.setOnClickListener(v -> switchToViewMode());
        btnSaveProfile.setOnClickListener(v -> handleSaveProfile());

        // Logout handler clearing activity stack
        btnLogoutProfile.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Queries customer profile details from SQLite database (or SharedPreferences fallback) and populates UI.
     */
    private void loadProfileData() {
        Cursor cursor = dbHandler.getUserByUsername(username);

        String fname = "", lname = "", email = "", phone = "", address = "", license = "";

        if (cursor != null && cursor.moveToFirst()) {
            int fnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_FIRST_NAME);
            int lnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_LAST_NAME);
            int emailIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_EMAIL);
            int phoneIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PHONE);
            int addressIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_ADDRESS);
            int licenseIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_LICENSE);

            if (fnameIdx != -1) fname = cursor.getString(fnameIdx);
            if (lnameIdx != -1) lname = cursor.getString(lnameIdx);
            if (emailIdx != -1) email = cursor.getString(emailIdx);
            if (phoneIdx != -1) phone = cursor.getString(phoneIdx);
            if (addressIdx != -1) address = cursor.getString(addressIdx);
            if (licenseIdx != -1) license = cursor.getString(licenseIdx);

            cursor.close();
        }

        // Fallback to SharedPreferences if DB record details are empty
        if (fname.isEmpty() && phone.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("RideRentPrefs", MODE_PRIVATE);
            if (username.equals("priya")) {
                fname = prefs.getString("fname_" + username, "Priya");
                lname = prefs.getString("lname_" + username, "Sharma");
                email = prefs.getString("email_" + username, "priya@email.com");
                phone = prefs.getString("phone_" + username, "+91 98765 43210");
                address = prefs.getString("address_" + username, "MG Road, Bangalore");
                license = prefs.getString("license_" + username, "KA01 20230099887");
            } else {
                fname = prefs.getString("fname_" + username, "Jane");
                lname = prefs.getString("lname_" + username, "Doe");
                email = prefs.getString("email_" + username, "jane@email.com");
                phone = prefs.getString("phone_" + username, "+91 90000 00000");
                address = prefs.getString("address_" + username, "Street, City");
                license = prefs.getString("license_" + username, "KA00 20230012345");
            }
        }

        currentFirstName = fname.isEmpty() ? "Priya" : fname;
        currentLastName = lname.isEmpty() ? "Sharma" : lname;

        tvProfileName.setText((currentFirstName + " " + currentLastName).trim());
        tvProfileUsername.setText("Username: " + username);
        tvProfileEmail.setText(email.isEmpty() ? "priya@email.com" : email);
        tvProfilePhone.setText(phone.isEmpty() ? "+91 98765 43210" : phone);
        tvProfileAddress.setText(address.isEmpty() ? "MG Road, Bangalore" : address);
        tvProfileLicense.setText(license.isEmpty() ? "KA01 20230099887" : license);
    }

    /**
     * Pre-fills edit form with current details and switches UI from view mode card to edit mode card.
     */
    private void switchToEditMode() {
        etEditFirstName.setText(currentFirstName);
        etEditLastName.setText(currentLastName);
        etEditEmail.setText(tvProfileEmail.getText().toString());
        etEditPhone.setText(tvProfilePhone.getText().toString());
        etEditAddress.setText(tvProfileAddress.getText().toString());
        etEditLicense.setText(tvProfileLicense.getText().toString());

        cardProfileView.setVisibility(View.GONE);
        cardProfileEdit.setVisibility(View.VISIBLE);
        btnEditProfile.setVisibility(View.GONE);
        btnLogoutProfile.setVisibility(View.GONE);

        etEditFirstName.requestFocus();
    }

    /**
     * Hides edit form card and switches UI back to read-only profile card view.
     */
    private void switchToViewMode() {
        cardProfileView.setVisibility(View.VISIBLE);
        cardProfileEdit.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.VISIBLE);
        btnLogoutProfile.setVisibility(View.VISIBLE);
    }

    /**
     * Validates updated profile fields and saves changes to both SQLite database and SharedPreferences.
     */
    private void handleSaveProfile() {
        String fname = etEditFirstName.getText().toString().trim();
        String lname = etEditLastName.getText().toString().trim();
        String email = etEditEmail.getText().toString().trim();
        String phone = etEditPhone.getText().toString().trim();
        String address = etEditAddress.getText().toString().trim();
        String license = etEditLicense.getText().toString().trim();

        // Field validations
        if (TextUtils.isEmpty(fname)) {
            Toast.makeText(this, "Please enter first name", Toast.LENGTH_SHORT).show();
            etEditFirstName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            etEditEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            etEditPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show();
            etEditAddress.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(license)) {
            Toast.makeText(this, "Please enter driving license number", Toast.LENGTH_SHORT).show();
            etEditLicense.requestFocus();
            return;
        }

        // Persist updates to SQLite Database
        dbHandler.updateUserProfile(username, fname, lname, email, phone, address, license);

        // Persist updates to SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("RideRentPrefs", MODE_PRIVATE).edit();
        editor.putString("fname_" + username, fname);
        editor.putString("lname_" + username, lname);
        editor.putString("email_" + username, email);
        editor.putString("phone_" + username, phone);
        editor.putString("address_" + username, address);
        editor.putString("license_" + username, license);
        editor.apply();

        // Refresh read-only UI fields and switch back to view mode
        loadProfileData();
        switchToViewMode();

        Toast.makeText(this, "Profile details updated successfully!", Toast.LENGTH_SHORT).show();
    }
}
