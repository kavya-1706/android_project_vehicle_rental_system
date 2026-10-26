package com.example.android_vehicle_project;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * BookingActivity handles vehicle booking creation for customers.
 * It extracts vehicle intent extras, auto-fills customer profile information, provides DatePickers
 * for pickup and drop dates, enforces Rental Terms & Conditions, calculates total rental costs,
 * saves new bookings into SQLite database, and displays a summary confirmation dialog.
 */
public class BookingActivity extends AppCompatActivity {

    // Intent parameters
    private String username;
    private int vehicleId;
    private String vehicleName;
    private String vehicleBrand;
    private String vehicleModel;
    private double vehiclePrice;

    // View declarations
    private TextView tvVehicleNameHeader, tvVehicleDetailsHeader, tvVehiclePriceHeader;
    private EditText etCustomerName, etCustomerPhone, etCustomerDL, etCustomerEmail;
    private EditText etPickupDate, etDropDate;
    private RelativeLayout rlPickupDateContainer, rlDropDateContainer;
    private CheckBox cbTerms;
    private TextView tvTermsText;
    private Button btnConfirmBooking;
    private ScrollView scrollViewBooking;

    // Database helper instance
    private DatabaseHandler dbHandler;

    // Date state management
    private Calendar pickupCalendar = Calendar.getInstance();
    private Calendar dropCalendar = Calendar.getInstance();
    private boolean isPickupSet = false;
    private boolean isDropSet = false;

    // Date formatting constants
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Initializes UI layout, extracts intent extras, sets up date pickers and button listeners on launch.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        dbHandler = new DatabaseHandler(this);

        // Extract vehicle and customer details from intent
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        if (username == null || username.isEmpty()) {
            username = "priya";
        }

        vehicleId = intent.getIntExtra("VEHICLE_ID", 1);
        vehicleName = intent.getStringExtra("VEHICLE_NAME");
        if (vehicleName == null) vehicleName = "Honda City";

        vehicleBrand = intent.getStringExtra("VEHICLE_BRAND");
        if (vehicleBrand == null) vehicleBrand = "Honda";

        vehicleModel = intent.getStringExtra("VEHICLE_MODEL");
        if (vehicleModel == null) vehicleModel = "City 2023";

        vehiclePrice = intent.getDoubleExtra("VEHICLE_PRICE", 1800.00);

        // Setup Toolbar back navigation
        Toolbar toolbar = findViewById(R.id.toolbarBooking);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind layout views
        scrollViewBooking = findViewById(R.id.scrollViewBooking);
        tvVehicleNameHeader = findViewById(R.id.tvBookingVehicleNameHeader);
        tvVehicleDetailsHeader = findViewById(R.id.tvBookingVehicleDetailsHeader);
        tvVehiclePriceHeader = findViewById(R.id.tvBookingVehiclePriceHeader);

        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerPhone = findViewById(R.id.etCustomerPhone);
        etCustomerDL = findViewById(R.id.etCustomerDL);
        etCustomerEmail = findViewById(R.id.etCustomerEmail);

        etPickupDate = findViewById(R.id.etPickupDate);
        etDropDate = findViewById(R.id.etDropDate);
        rlPickupDateContainer = findViewById(R.id.rlPickupDateContainer);
        rlDropDateContainer = findViewById(R.id.rlDropDateContainer);

        cbTerms = findViewById(R.id.cbTerms);
        tvTermsText = findViewById(R.id.tvTermsText);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        // Auto-scroll focus listener keeping input fields visible above soft keyboard
        View.OnFocusChangeListener autoScrollFocusListener = (v, hasFocus) -> {
            if (hasFocus && scrollViewBooking != null) {
                scrollViewBooking.postDelayed(() -> {
                    scrollViewBooking.smoothScrollTo(0, v.getBottom() + 100);
                }, 150);
            }
        };

        etCustomerName.setOnFocusChangeListener(autoScrollFocusListener);
        etCustomerPhone.setOnFocusChangeListener(autoScrollFocusListener);
        etCustomerDL.setOnFocusChangeListener(autoScrollFocusListener);
        etCustomerEmail.setOnFocusChangeListener(autoScrollFocusListener);

        // Populate Vehicle Header summary card
        tvVehicleNameHeader.setText(vehicleName);
        tvVehicleDetailsHeader.setText(vehicleBrand + " • " + vehicleModel);
        tvVehiclePriceHeader.setText(String.format(Locale.getDefault(), "₹%,.0f/day", vehiclePrice));

        // Auto-fill Customer Profile Info from SQLite / SharedPreferences
        populateCustomerInfo();

        // Setup Pick-up Date selection listener
        View.OnClickListener pickupListener = v -> showPickupDatePicker();
        rlPickupDateContainer.setOnClickListener(pickupListener);
        etPickupDate.setOnClickListener(pickupListener);

        // Setup Drop Date selection listener
        View.OnClickListener dropListener = v -> showDropDatePicker();
        rlDropDateContainer.setOnClickListener(dropListener);
        etDropDate.setOnClickListener(dropListener);

        // Terms and Conditions Dialog trigger
        tvTermsText.setOnClickListener(v -> showTermsDialog());

        // Confirm Booking Click Handler
        btnConfirmBooking.setOnClickListener(v -> handleBookingConfirmation());
    }

    /**
     * Queries customer profile details from SQLite database and populates booking form input fields.
     */
    private void populateCustomerInfo() {
        Cursor cursor = dbHandler.getUserByUsername(username);
        String fname = "", lname = "", phone = "", license = "", email = "";

        if (cursor != null && cursor.moveToFirst()) {
            int fnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_FIRST_NAME);
            int lnameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_LAST_NAME);
            int phoneIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PHONE);
            int licenseIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_LICENSE);
            int emailIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_EMAIL);

            if (fnameIdx != -1) fname = cursor.getString(fnameIdx);
            if (lnameIdx != -1) lname = cursor.getString(lnameIdx);
            if (phoneIdx != -1) phone = cursor.getString(phoneIdx);
            if (licenseIdx != -1) license = cursor.getString(licenseIdx);
            if (emailIdx != -1) email = cursor.getString(emailIdx);

            cursor.close();
        }

        // Fallback to SharedPreferences if DB details are empty
        if (fname.isEmpty() && phone.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("RideRentPrefs", MODE_PRIVATE);
            fname = prefs.getString("fname_" + username, "");
            lname = prefs.getString("lname_" + username, "");
            phone = prefs.getString("phone_" + username, "");
            license = prefs.getString("license_" + username, "");
            email = prefs.getString("email_" + username, "");
        }

        // Apply fallbacks
        String fullName = (fname + " " + lname).trim();
        if (fullName.isEmpty() || username.equals("priya")) {
            fullName = "Priya Sharma";
        }
        if (phone == null || phone.isEmpty()) {
            phone = "+91 90000 00000";
        }
        if (email == null || email.isEmpty()) {
            email = "priya.sharma@email.com";
        }
        if (license == null || license.isEmpty()) {
            license = "KA01 20230099888";
        }

        etCustomerName.setText(fullName);
        etCustomerPhone.setText(phone);
        etCustomerDL.setText(license);
        etCustomerEmail.setText(email);
    }

    /**
     * Launches DatePickerDialog for pick-up date selection, ensuring past dates cannot be picked.
     */
    private void showPickupDatePicker() {
        Calendar today = Calendar.getInstance();
        int year = isPickupSet ? pickupCalendar.get(Calendar.YEAR) : today.get(Calendar.YEAR);
        int month = isPickupSet ? pickupCalendar.get(Calendar.MONTH) : today.get(Calendar.MONTH);
        int day = isPickupSet ? pickupCalendar.get(Calendar.DAY_OF_MONTH) : today.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            pickupCalendar.set(Calendar.YEAR, selectedYear);
            pickupCalendar.set(Calendar.MONTH, selectedMonth);
            pickupCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);
            pickupCalendar.set(Calendar.HOUR_OF_DAY, 0);
            pickupCalendar.set(Calendar.MINUTE, 0);
            pickupCalendar.set(Calendar.SECOND, 0);

            etPickupDate.setText(displayDateFormat.format(pickupCalendar.getTime()));
            isPickupSet = true;

            // Automatically adjust drop date if it becomes earlier than pick-up
            if (isDropSet && dropCalendar.before(pickupCalendar)) {
                dropCalendar.setTime(pickupCalendar.getTime());
                etDropDate.setText(displayDateFormat.format(dropCalendar.getTime()));
            }
        }, year, month, day);

        // Disallow selecting past dates
        dialog.getDatePicker().setMinDate(today.getTimeInMillis() - 1000);
        dialog.show();
    }

    /**
     * Launches DatePickerDialog for drop date selection, ensuring drop date is on or after pick-up.
     */
    private void showDropDatePicker() {
        Calendar today = Calendar.getInstance();
        Calendar baseCalendar = isDropSet ? dropCalendar : (isPickupSet ? pickupCalendar : today);

        int year = baseCalendar.get(Calendar.YEAR);
        int month = baseCalendar.get(Calendar.MONTH);
        int day = baseCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            Calendar selectedDrop = Calendar.getInstance();
            selectedDrop.set(Calendar.YEAR, selectedYear);
            selectedDrop.set(Calendar.MONTH, selectedMonth);
            selectedDrop.set(Calendar.DAY_OF_MONTH, selectedDay);
            selectedDrop.set(Calendar.HOUR_OF_DAY, 0);
            selectedDrop.set(Calendar.MINUTE, 0);
            selectedDrop.set(Calendar.SECOND, 0);

            if (isPickupSet && selectedDrop.before(pickupCalendar)) {
                Toast.makeText(this, "Drop date cannot be earlier than pick-up date", Toast.LENGTH_SHORT).show();
                return;
            }

            dropCalendar = selectedDrop;
            etDropDate.setText(displayDateFormat.format(dropCalendar.getTime()));
            isDropSet = true;
        }, year, month, day);

        long minTime = isPickupSet ? pickupCalendar.getTimeInMillis() : today.getTimeInMillis() - 1000;
        dialog.getDatePicker().setMinDate(minTime);
        dialog.show();
    }

    /**
     * Displays Rental Terms & Conditions popup dialog and checks terms checkbox upon agreement.
     */
    private void showTermsDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Rental Terms & Conditions")
                .setMessage("1. Driver Requirements:\nMust be at least 21 years of age with a valid, unexpired Driving Licence.\n\n" +
                        "2. Operating Hours:\nVehicle pick-up and drop hours are strictly between 9:00 AM and 5:00 PM. Please collect and return the vehicle within operating hours.\n\n" +
                        "3. Fuel Policy:\nThe vehicle must be returned with the same fuel level as recorded during pick-up.\n\n" +
                        "4. Overdue Policy:\nLate returns beyond agreed drop date will incur extra charges.\n\n" +
                        "5. Safety & Care:\nTraffic violations, toll fees, or physical damage incurred during rental period are customer responsibility.\n\n" +
                        "6. Cancellation Policy:\nBookings can be cancelled only if there is at least 2 days left before the booking start date.")
                .setPositiveButton("I Agree", (d, which) -> cbTerms.setChecked(true))
                .setNegativeButton("Close", null)
                .create();

        dialog.show();
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.WHITE);
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.WHITE);
        }
    }

    /**
     * Validates form inputs, calculates total days and cost, saves booking to SQLite DB, and presents summary dialog.
     */
    private void handleBookingConfirmation() {
        String customerName = etCustomerName.getText().toString().trim();
        String customerPhone = etCustomerPhone.getText().toString().trim();
        String customerDL = etCustomerDL.getText().toString().trim();
        String customerEmail = etCustomerEmail.getText().toString().trim();

        // 1. Validate Customer Details
        if (TextUtils.isEmpty(customerName)) {
            Toast.makeText(this, "Please enter customer name", Toast.LENGTH_SHORT).show();
            etCustomerName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(customerPhone)) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            etCustomerPhone.requestFocus();
            return;
        }

        // 2. Validate Pick-up and Drop Dates
        if (!isPickupSet || TextUtils.isEmpty(etPickupDate.getText().toString().trim())) {
            Toast.makeText(this, "Please select pick-up date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDropSet || TextUtils.isEmpty(etDropDate.getText().toString().trim())) {
            Toast.makeText(this, "Please select drop date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dropCalendar.before(pickupCalendar)) {
            Toast.makeText(this, "Drop date cannot be earlier than pick-up date", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Validate Terms Checkbox Agreement
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "You must agree to the rental terms & conditions to proceed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate total rental duration in days and total price
        long diffInMillis = dropCalendar.getTimeInMillis() - pickupCalendar.getTimeInMillis();
        int totalDays = (int) TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1;
        if (totalDays < 1) totalDays = 1;

        double totalAmount = vehiclePrice * totalDays;

        // Fetch User ID from database
        int userId = dbHandler.getUserIdByUsername(username);
        if (userId == -1) {
            userId = 2; // Default fallback ID for demo
        }

        // Format dates for SQLite database insertion
        String pickupDbStr = dbDateFormat.format(pickupCalendar.getTime());
        String dropDbStr = dbDateFormat.format(dropCalendar.getTime());
        String payTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        // Insert booking into SQLite database
        boolean isSaved = dbHandler.addBooking(userId, vehicleId, pickupDbStr, dropDbStr, totalDays, totalAmount, "upcoming", "success", payTime);

        // Build summary dialog header
        TextView customTitle = new TextView(this);
        customTitle.setText("Booking Summary & Confirmation");
        customTitle.setTextSize(18);
        customTitle.setTypeface(null, Typeface.BOLD);
        customTitle.setTextColor(Color.WHITE);
        customTitle.setPadding(48, 48, 48, 24);

        // Build summary message
        String summaryMessage = "Vehicle: " + vehicleName + "\n" +
                "Customer: " + customerName + "\n" +
                "Phone: " + customerPhone + "\n" +
                "DL No: " + customerDL + "\n" +
                "Email: " + customerEmail + "\n" +
                "Pick-up: " + displayDateFormat.format(pickupCalendar.getTime()) + "\n" +
                "Drop: " + displayDateFormat.format(dropCalendar.getTime()) + " (" + totalDays + " day" + (totalDays > 1 ? "s" : "") + ")\n" +
                "Operating Hours: 9:00 AM - 5:00 PM\n" +
                "\nTotal Amount: ₹" + String.format(Locale.getDefault(), "%,.0f", totalAmount) + "\n" +
                "Payment Status: SUCCESSFUL";

        // Display summary alert dialog
        new AlertDialog.Builder(this, R.style.BookingDialogTheme)
                .setCustomTitle(customTitle)
                .setMessage(summaryMessage)
                .setCancelable(false)
                .setPositiveButton("View My Bookings", (dialog, which) -> {
                    Intent pastIntent = new Intent(BookingActivity.this, PastBookingsActivity.class);
                    pastIntent.putExtra("USERNAME", username);
                    startActivity(pastIntent);
                    finish();
                })
                .setNegativeButton("Back to Home", (dialog, which) -> finish())
                .show();

        if (isSaved) {
            Toast.makeText(this, "Booking confirmed successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}
