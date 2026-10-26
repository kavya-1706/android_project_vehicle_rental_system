package com.example.android_vehicle_project;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etDob, etPhone, etAddress, etLicense, etRegUsername, etRegPassword;
    private Button btnCreateAccount;
    private ScrollView scrollViewRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbarRegister);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        scrollViewRegister = findViewById(R.id.scrollViewRegister);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etDob = findViewById(R.id.etDob);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etLicense = findViewById(R.id.etLicense);
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Auto-scroll focused input fields above soft keyboard
        View.OnFocusChangeListener autoScrollFocusListener = (v, hasFocus) -> {
            if (hasFocus && scrollViewRegister != null) {
                scrollViewRegister.postDelayed(() -> {
                    scrollViewRegister.smoothScrollTo(0, v.getBottom() + 100);
                }, 150);
            }
        };

        etFirstName.setOnFocusChangeListener(autoScrollFocusListener);
        etLastName.setOnFocusChangeListener(autoScrollFocusListener);
        etEmail.setOnFocusChangeListener(autoScrollFocusListener);
        etDob.setOnFocusChangeListener(autoScrollFocusListener);
        etPhone.setOnFocusChangeListener(autoScrollFocusListener);
        etAddress.setOnFocusChangeListener(autoScrollFocusListener);
        etLicense.setOnFocusChangeListener(autoScrollFocusListener);
        etRegUsername.setOnFocusChangeListener(autoScrollFocusListener);
        etRegPassword.setOnFocusChangeListener(autoScrollFocusListener);

        // DatePickerDialog for Date of Birth
        etDob.setOnClickListener(v -> showDatePickerDialog());

        btnCreateAccount.setOnClickListener(v -> handleRegistration());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 20; // Default to 20 years ago
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String dateStr = String.format("%02d / %02d / %d", selectedDay, (selectedMonth + 1), selectedYear);
            etDob.setText(dateStr);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void handleRegistration() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String license = etLicense.getText().toString().trim();
        String username = etRegUsername.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || dob.isEmpty() ||
                phone.isEmpty() || address.isEmpty() || license.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 5) {
            Toast.makeText(this, "Username must be at least 5 characters long", Toast.LENGTH_SHORT).show();
            etRegUsername.requestFocus();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 8 characters and include letters, numbers, and special characters", Toast.LENGTH_LONG).show();
            etRegPassword.requestFocus();
            return;
        }

        // DL validation: exactly 15 characters, valid Indian state code + 13 digits
        String dlPattern = "^(?i)(AN|AP|AR|AS|BR|CH|CG|DD|DL|GA|GJ|HR|HP|JK|JH|KA|KL|LA|LD|MP|MH|MN|ML|MZ|NL|OD|OR|PY|PB|RJ|SK|TN|TS|TR|UP|UK|UA|WB)[0-9]{13}$";
        if (!license.matches(dlPattern)) {
            Toast.makeText(this, "Driving License must be a valid 15-character Indian DL (e.g. KA1234567890123 with a valid state code)", Toast.LENGTH_LONG).show();
            etLicense.requestFocus();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("RideRentPrefs", MODE_PRIVATE);
        DatabaseHandler dbHandler = new DatabaseHandler(this);

        if (dbHandler.checkUsernameExists(username) || prefs.contains("pwd_" + username) || username.equals("priya") || username.equals("abcd")) {
            Toast.makeText(this, "Username is already registered. Choose another.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save registration details to SQLite Database
        boolean success = dbHandler.registerUser(firstName, lastName, email, dob, phone, address, license, username, password);

        // Also save to SharedPreferences for backward compatibility
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("fname_" + username, firstName);
        editor.putString("lname_" + username, lastName);
        editor.putString("email_" + username, email);
        editor.putString("dob_" + username, dob);
        editor.putString("phone_" + username, phone);
        editor.putString("address_" + username, address);
        editor.putString("license_" + username, license);
        editor.putString("pwd_" + username, password);
        editor.apply();

        if (success) {
            Toast.makeText(this, "Account created successfully! Please login.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to register account in database. Saved locally.", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }
        return hasLetter && hasDigit && hasSpecial;
    }
}
