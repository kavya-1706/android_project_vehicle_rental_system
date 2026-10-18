package com.example.android_vehicle_project;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminAddEditVehicleActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvToolbarTitle;
    private EditText etVehicleName, etBrand, etModel, etVehicleNum, etFuel, etCapacity, etPrice, etStatus, etDescription;
    private Button btnSaveVehicle;

    private DatabaseHandler dbHandler;
    private int vehicleIdToEdit = -1; // -1 means Add Mode, else Edit Mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_edit_vehicle);

        dbHandler = new DatabaseHandler(this);

        initViews();

        // Check if we received an ID (Edit Mode)
        vehicleIdToEdit = getIntent().getIntExtra("VEHICLE_ID", -1);

        if (vehicleIdToEdit != -1) {
            tvToolbarTitle.setText("Edit Vehicle");
            loadVehicleData(vehicleIdToEdit);
        }

        ivBack.setOnClickListener(v -> finish());

        btnSaveVehicle.setOnClickListener(v -> saveVehicle());
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        
        etVehicleName = findViewById(R.id.etVehicleName);
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etVehicleNum = findViewById(R.id.etVehicleNum);
        etFuel = findViewById(R.id.etFuel);
        etCapacity = findViewById(R.id.etCapacity);
        etPrice = findViewById(R.id.etPrice);
        etStatus = findViewById(R.id.etStatus);
        etDescription = findViewById(R.id.etDescription);
        
        btnSaveVehicle = findViewById(R.id.btnSaveVehicle);
    }

    private void loadVehicleData(int id) {
        Cursor cursor = dbHandler.getReadableDatabase().rawQuery(
                "SELECT * FROM " + DatabaseHandler.TABLE_VEHICLES + " WHERE " + DatabaseHandler.COLUMN_VEHICLE_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        if (cursor != null && cursor.moveToFirst()) {
            etVehicleName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_NAME)));
            etBrand.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_BRAND)));
            etModel.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_MODEL)));
            etVehicleNum.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_NUM)));
            etFuel.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_FUEL)));
            etCapacity.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_SEATING_CAPACITY))));
            etPrice.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_PRICE_PER_DAY))));
            etStatus.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_STATUS)));
            etDescription.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_DESCRIPTION)));
            cursor.close();
        }
    }

    private void saveVehicle() {
        String name = etVehicleName.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String num = etVehicleNum.getText().toString().trim();
        String fuel = etFuel.getText().toString().trim();
        String capStr = etCapacity.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String status = etStatus.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (name.isEmpty() || brand.isEmpty() || num.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Name, Brand, Plate Number, and Price are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = capStr.isEmpty() ? 0 : Integer.parseInt(capStr);
        double price = Double.parseDouble(priceStr);

        boolean success;

        if (vehicleIdToEdit == -1) {
            // Add New
            success = dbHandler.addVehicle(name, brand, model, num, fuel, capacity, price, desc, status);
        } else {
            // Update Existing
            success = dbHandler.updateVehicle(vehicleIdToEdit, name, brand, model, num, fuel, capacity, price, desc, status);
        }

        if (success) {
            Toast.makeText(this, "Vehicle saved successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Go back to list
        } else {
            Toast.makeText(this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}