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
    private LinearLayout navHome, navPast, navRefresh, navProfile;
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
        etSearchVehicle = findViewById(R.id.etSearchVehicle);
        llVehicleContainer = findViewById(R.id.llVehicleContainer);

        navHome = findViewById(R.id.navHome);
        navPast = findViewById(R.id.navPast);
        navRefresh = findViewById(R.id.navRefresh);
        navProfile = findViewById(R.id.navProfile);

        // Profile navigation / icon
        ivProfileIcon.setOnClickListener(v -> openProfile());
        navProfile.setOnClickListener(v -> openProfile());

        // Refresh navigation
        navRefresh.setOnClickListener(v -> {
            etSearchVehicle.setText("");
            loadVehiclesFromDatabase();
            Toast.makeText(this, "Refreshed vehicle list", Toast.LENGTH_SHORT).show();
        });

        // Past navigation
        navPast.setOnClickListener(v -> {
            Toast.makeText(this, "No previous bookings yet.", Toast.LENGTH_SHORT).show();
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
                TextView tvPrice = view.findViewById(R.id.tvVehiclePrice);
                Button btnBook = view.findViewById(R.id.btnBookVehicle);

                tvName.setText(item.name);
                tvDetails.setText(item.brand + " • " + item.model + " • " + item.fuel + " • " + item.seats + " seats");
                tvPrice.setText(String.format("₹%,.0f/day", item.price));

                btnBook.setOnClickListener(v -> Toast.makeText(this, "Booking functionality for " + item.name + " will be added later.", Toast.LENGTH_SHORT).show());

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
