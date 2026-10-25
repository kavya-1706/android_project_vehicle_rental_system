package com.example.android_vehicle_project;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class CustomerHomeActivity extends AppCompatActivity {

    private String username, firstName;
    private TextView tvGreeting;
    private EditText etSearchVehicle;
    private LinearLayout llVehicleContainer;
    private LinearLayout navHome, navPast, navProfile;
    private ImageView ivProfileIcon;

    private DatabaseHandler dbHandler;
    private List<VehicleItem> vehicleList = new ArrayList<>();

    private static class VehicleItem {
        int id;
        String name;
        String brand;
        String model;
        String fuel;
        int seats;
        double price;
        String description;
        View cardView;

        VehicleItem(int id, String name, String brand, String model, String fuel, int seats, double price, String description) {
            this.id = id;
            this.name = name;
            this.brand = brand;
            this.model = model;
            this.fuel = fuel;
            this.seats = seats;
            this.price = price;
            this.description = description;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        dbHandler = new DatabaseHandler(this);

        username = getIntent().getStringExtra("USERNAME");
        firstName = getIntent().getStringExtra("FIRST_NAME");
        if (firstName == null || firstName.isEmpty()) {
            firstName = "Priya";
        }

        tvGreeting = findViewById(R.id.tvGreeting);
        tvGreeting.setText(getString(R.string.customer_home_greeting, firstName));

        ivProfileIcon = findViewById(R.id.ivProfileIcon);
        ImageView ivLogoutIcon = findViewById(R.id.ivLogoutIcon);
        ImageView ivMenuIcon = findViewById(R.id.ivMenuIcon);
        etSearchVehicle = findViewById(R.id.etSearchVehicle);
        llVehicleContainer = findViewById(R.id.llVehicleContainer);

        navHome = findViewById(R.id.navHome);
        navPast = findViewById(R.id.navPast);
        navProfile = findViewById(R.id.navProfile);

        // Menu icon listener
        if (ivMenuIcon != null) {
            ivMenuIcon.setOnClickListener(v -> showTopMenu());
        }

        // Profile navigation / icon
        ivProfileIcon.setOnClickListener(v -> openProfile());
        navProfile.setOnClickListener(v -> openProfile());

        // Logout icon navigation
        ivLogoutIcon.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerHomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Past navigation
        navPast.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerHomeActivity.this, PastBookingsActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        // Home navigation
        navHome.setOnClickListener(v -> {
            // Already home
        });

        // Load vehicles dynamically from SQLite DB
        loadVehiclesFromDatabase();

        // Search filter
        etSearchVehicle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVehicles(s.toString().toLowerCase().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void openProfile() {
        Intent intent = new Intent(CustomerHomeActivity.this, CustomerProfileActivity.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }

    private void showTopMenu() {
        String[] options = {"Home", "Terms & Conditions"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Customer Menu")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Home: Reset search & reload
                        etSearchVehicle.setText("");
                        loadVehiclesFromDatabase();
                        Toast.makeText(this, "Refreshed Home screen", Toast.LENGTH_SHORT).show();
                    } else if (which == 1) {
                        // Terms & Conditions
                        showTermsAndConditions();
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showTermsAndConditions() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Rental Terms & Conditions")
                .setMessage("1. Driver Requirements:\nMust be at least 21 years of age with a valid, unexpired Driving Licence.\n\n" +
                        "2. Operating Hours:\nVehicle pick-up and drop hours are strictly between 9:00 AM and 5:00 PM. Please collect and return the vehicle within operating hours.\n\n" +
                        "3. Fuel Policy:\nThe vehicle must be returned with the same fuel level as recorded during pick-up.\n\n" +
                        "4. Overdue Policy:\nLate returns beyond agreed drop date will incur extra charges.\n\n" +
                        "5. Safety & Care:\nTraffic violations, toll fees, or physical damage incurred during rental period are customer responsibility.")
                .setPositiveButton("I Understand", null)
                .show();
    }

    private void loadVehiclesFromDatabase() {
        llVehicleContainer.removeAllViews();
        vehicleList.clear();

        Cursor cursor = dbHandler.getAvailableVehicles();
        if (cursor != null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            while (cursor.moveToNext()) {
                int idIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_VEHICLE_ID);
                int nameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_VEHICLE_NAME);
                int brandIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_BRAND);
                int modelIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_MODEL);
                int fuelIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_FUEL);
                int seatsIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_SEATING_CAPACITY);
                int priceIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PRICE_PER_DAY);
                int descIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_DESCRIPTION);

                int id = idIdx != -1 ? cursor.getInt(idIdx) : 0;
                String name = nameIdx != -1 ? cursor.getString(nameIdx) : "Vehicle";
                String brand = brandIdx != -1 ? cursor.getString(brandIdx) : "";
                String model = modelIdx != -1 ? cursor.getString(modelIdx) : "";
                String fuel = fuelIdx != -1 ? cursor.getString(fuelIdx) : "petrol";
                int seats = seatsIdx != -1 ? cursor.getInt(seatsIdx) : 5;
                double price = priceIdx != -1 ? cursor.getDouble(priceIdx) : 1000.00;
                String desc = descIdx != -1 ? cursor.getString(descIdx) : "";

                VehicleItem item = new VehicleItem(id, name, brand, model, fuel, seats, price, desc);

                View view = inflater.inflate(R.layout.item_vehicle, llVehicleContainer, false);
                TextView tvName = view.findViewById(R.id.tvVehicleName);
                TextView tvDetails = view.findViewById(R.id.tvVehicleDetails);
                TextView tvDescription = view.findViewById(R.id.tvVehicleDescription);
                TextView tvPrice = view.findViewById(R.id.tvVehiclePrice);
                Button btnBook = view.findViewById(R.id.btnBookVehicle);

                tvName.setText(item.name);
                String detailsStr = item.brand + " " + item.model + "\nFuel: " + item.fuel + "\nCapacity: " + item.seats + " Seats";
                tvDetails.setText(detailsStr);
                
                if (tvDescription != null) {
                    if (item.description != null && !item.description.trim().isEmpty()) {
                        tvDescription.setText(item.description);
                        tvDescription.setVisibility(View.VISIBLE);
                    } else {
                        tvDescription.setVisibility(View.GONE);
                    }
                }

                tvPrice.setText(String.format("₹%,.0f/day", item.price));

                btnBook.setOnClickListener(v -> {
                    Intent intent = new Intent(CustomerHomeActivity.this, BookingActivity.class);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("VEHICLE_ID", item.id);
                    intent.putExtra("VEHICLE_NAME", item.name);
                    intent.putExtra("VEHICLE_BRAND", item.brand);
                    intent.putExtra("VEHICLE_MODEL", item.model);
                    intent.putExtra("VEHICLE_PRICE", item.price);
                    startActivity(intent);
                });

                item.cardView = view;
                vehicleList.add(item);
                llVehicleContainer.addView(view);
            }
            cursor.close();
        }
    }

    private void filterVehicles(String query) {
        for (VehicleItem item : vehicleList) {
            if (item.cardView != null) {
                boolean matches = query.isEmpty()
                        || item.name.toLowerCase().contains(query)
                        || item.brand.toLowerCase().contains(query)
                        || item.model.toLowerCase().contains(query)
                        || item.fuel.toLowerCase().contains(query)
                        || item.description.toLowerCase().contains(query);
                item.cardView.setVisibility(matches ? View.VISIBLE : View.GONE);
            }
        }
    }
}
