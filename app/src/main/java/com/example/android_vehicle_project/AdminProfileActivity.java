package com.example.android_vehicle_project;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminProfileActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etFirstName, etLastName, etEmail, etPhone, etAddress, etLicense;
    private Button btnSaveProfile;

    private DatabaseHandler dbHandler;
    
    // In a real app, this would come from SharedPreferences upon Login.
    // For this prototype, the default admin username is 'admin'.
    private final String CURRENT_ADMIN_USERNAME = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        dbHandler = new DatabaseHandler(this);

        initViews();
        loadAdminProfile();

        ivBack.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> saveAdminProfile());
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etLicense = findViewById(R.id.etLicense);
        
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void loadAdminProfile() {
        Cursor cursor = dbHandler.getUserByUsername(CURRENT_ADMIN_USERNAME);

        if (cursor != null && cursor.moveToFirst()) {
            etFirstName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_FIRST_NAME)));
            etLastName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_LAST_NAME)));
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_EMAIL)));
            etPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_PHONE)));
            etAddress.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_ADDRESS)));
            etLicense.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_LICENSE)));
            cursor.close();
        }
    }

    private void saveAdminProfile() {
        String fName = etFirstName.getText().toString().trim();
        String lName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String license = etLicense.getText().toString().trim();

        if (fName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "First Name, Email, and Phone are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // We use the existing updateUserProfile method
        boolean success = dbHandler.updateUserProfile(CURRENT_ADMIN_USERNAME, fName, lName, email, phone, address, license);

        if (success) {
            Toast.makeText(this, "Admin Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
            finish(); // return to dashboard
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}