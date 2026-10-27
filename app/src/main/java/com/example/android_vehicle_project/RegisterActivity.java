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

/**
 * RegisterActivity handles customer account registration.
 * Performs multi-layer validations (email format, phone length, 5+ char username uniqueness,
 * 8+ char password complexity with special characters, and 15-char Indian state-code Driving License regex)
 * before persisting customer accounts into SQLite database and SharedPreferences.
 */
public class RegisterActivity extends AppCompatActivity {

    // Input Views
    private EditText etFirstName, etLastName, etEmail, etDob, etPhone, etAddress, etLicense, etRegUsername, etRegPassword;
    private Button btnCreateAccount;
    private ScrollView scrollViewRegister;

    /**
     * Initializes activity views, toolbar navigation, focus listeners, and button handlers on creation.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Toolbar setup with back arrow navigation
        Toolbar toolbar = findViewById(R.id.toolbarRegister);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind input views
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

        // Auto-scroll listener to keep focused text input fields visible above soft keyboard
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

        // DatePickerDialog for selecting Date of Birth
        etDob.setOnClickListener(v -> showDatePickerDialog());

        // Create Account button listener
        btnCreateAccount.setOnClickListener(v -> handleRegistration());
    }

    /**
     * Launches DatePickerDialog for picking Date of Birth formatted as DD / MM / YYYY.
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 20; // Default calendar view to 20 years ago
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String dateStr = String.format("%02d / %02d / %d", selectedDay, (selectedMonth + 1), selectedYear);
            etDob.setText(dateStr);
        }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Validates all input fields and creates customer account in SQLite DB and SharedPreferences.
     */
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

        // 1. Required fields non-empty check
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || dob.isEmpty() ||
                phone.isEmpty() || address.isEmpty() || license.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Email format validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Phone length validation
        if (phone.length() != 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Username minimum length validation (at least 5 chars)
        if (username.length() < 5) {
            Toast.makeText(this, "Username must be at least 5 characters long", Toast.LENGTH_SHORT).show();
            etRegUsername.requestFocus();
            return;
        }

        // 5. Password complexity validation (8+ chars, letter + digit + special char)
        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 8 characters and include letters, numbers, and special characters", Toast.LENGTH_LONG).show();
            etRegPassword.requestFocus();
            return;
        }

        // 6. Indian State-Code Driving License regex validation (exactly 15 chars: valid 2-letter state code + 13 digits)
        String dlPattern = "^(?i)(AN|AP|AR|AS|BR|CH|CG|DD|DL|GA|GJ|HR|HP|JK|JH|KA|KL|LA|LD|MP|MH|MN|ML|MZ|NL|OD|OR|PY|PB|RJ|SK|TN|TS|TR|UP|UK|UA|WB)[0-9]{13}$";
        if (!license.matches(dlPattern)) {
            Toast.makeText(this, "Driving License must be a valid 15-character Indian DL (e.g. KA1234567890123 with a valid state code)", Toast.LENGTH_LONG).show();
            etLicense.requestFocus();
            return;
        }

        // 7. Username uniqueness check against SQLite database and SharedPreferences
        SharedPreferences prefs = getSharedPreferences("RideRentPrefs", MODE_PRIVATE);
        DatabaseHandler dbHandler = new DatabaseHandler(this);

        if (dbHandler.checkUsernameExists(username) || prefs.contains("pwd_" + username)) {
            Toast.makeText(this, "Username is already registered. Choose another.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save registration details to SQLite Database
        boolean success = dbHandler.registerUser(firstName, lastName, email, dob, phone, address, license, username, password);

        // Save to SharedPreferences for backward compatibility
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

    /**
     * Checks password complexity: requires 8+ characters, at least 1 letter, 1 digit, and 1 special symbol.
     * @param password Password string to test.
     * @return True if password meets all requirements, false otherwise.
     */
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
