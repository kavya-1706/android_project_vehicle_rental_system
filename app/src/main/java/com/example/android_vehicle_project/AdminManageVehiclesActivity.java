package com.example.android_vehicle_project;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminManageVehiclesActivity handles vehicle inventory management for System Administrators.
 * Features:
 * 1. Displays all vehicles with status badges ('AVAILABLE' or 'UNAVAILABLE').
 * 2. Floating Action Button (FAB) to launch AdminAddEditVehicleActivity in creation mode.
 * 3. Edit action to launch AdminAddEditVehicleActivity with VEHICLE_ID extra.
 * 4. Delete action with styled red confirmation dialog to remove vehicles.
 */
public class AdminManageVehiclesActivity extends AppCompatActivity {

    // View declarations
    private ImageView ivBack;
    private RecyclerView rvAdminVehicles;
    private FloatingActionButton fabAddVehicle;

    // Database helper & vehicle list dataset
    private DatabaseHandler dbHandler;
    private VehiclesAdapter adapter;
    private List<VehicleRecord> vehicleList;

    /**
     * Initializes activity layout, views, listeners, and RecyclerView.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_vehicles);

        dbHandler = new DatabaseHandler(this);

        ivBack = findViewById(R.id.ivBack);
        rvAdminVehicles = findViewById(R.id.rvAdminVehicles);
        fabAddVehicle = findViewById(R.id.fabAddVehicle);

        // Toolbar back arrow handler
        ivBack.setOnClickListener(v -> finish());

        // Launch Add Vehicle screen
        fabAddVehicle.setOnClickListener(v -> {
            Intent intent = new Intent(AdminManageVehiclesActivity.this, AdminAddEditVehicleActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();
    }

    /**
     * Reloads vehicle dataset when returning to this activity from Add/Edit screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadDataFromDatabase();
    }

    /**
     * Configures RecyclerView layout manager and adapter.
     */
    private void setupRecyclerView() {
        vehicleList = new ArrayList<>();
        adapter = new VehiclesAdapter(vehicleList);
        rvAdminVehicles.setLayoutManager(new LinearLayoutManager(this));
        rvAdminVehicles.setAdapter(adapter);
    }

    /**
     * Queries all vehicles from SQLite database and populates dataset.
     */
    private void loadDataFromDatabase() {
        vehicleList.clear();
        Cursor cursor = dbHandler.getAllVehiclesAdmin();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_NAME));
                String brand = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_BRAND));
                String model = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_MODEL));
                String num = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_NUM));
                String fuel = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_FUEL));
                int capacity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_SEATING_CAPACITY));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_PRICE_PER_DAY));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_DESCRIPTION));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_VEHICLE_STATUS));

                VehicleRecord record = new VehicleRecord(id, name, brand, model, num, fuel, capacity, price, desc, status);
                vehicleList.add(record);
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Shows confirmation dialog to delete a vehicle and removes record from SQLite database.
     *
     * @param vehicleId Primary key vehicle_id.
     * @param position Position index in RecyclerView adapter.
     */
    private void deleteVehicle(int vehicleId, int position) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Vehicle")
                .setMessage("Are you sure you want to completely remove this vehicle?")
                .setPositiveButton("Delete", (dialogInterface, which) -> {
                    boolean deleted = dbHandler.deleteVehicle(vehicleId);
                    if (deleted) {
                        Toast.makeText(this, "Vehicle deleted", Toast.LENGTH_SHORT).show();
                        vehicleList.remove(position);
                        adapter.notifyItemRemoved(position);
                    } else {
                        Toast.makeText(this, "Failed to delete. It might be linked to existing bookings.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Style positive button text red
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.error_red));
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.white));
        }
    }

    /**
     * Data model class representing a vehicle record for Admin management.
     */
    private static class VehicleRecord {
        int id;
        String name, brand, model, num, fuel, desc, status;
        int capacity;
        double price;

        public VehicleRecord(int id, String name, String brand, String model, String num, String fuel, int capacity, double price, String desc, String status) {
            this.id = id;
            this.name = name;
            this.brand = brand;
            this.model = model;
            this.num = num;
            this.fuel = fuel;
            this.capacity = capacity;
            this.price = price;
            this.desc = desc;
            this.status = status;
        }
    }

    /**
     * RecyclerView Adapter binding VehicleRecord items to item_admin_vehicle.xml views.
     */
    private class VehiclesAdapter extends RecyclerView.Adapter<VehiclesAdapter.ViewHolder> {

        private final List<VehicleRecord> items;

        public VehiclesAdapter(List<VehicleRecord> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_vehicle, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VehicleRecord record = items.get(position);

            holder.tvVehicleName.setText(record.name);
            holder.tvVehicleNum.setText(record.num);
            holder.tvVehiclePrice.setText("₹" + record.price + "/day");

            holder.tvVehicleStatus.setText(record.status.toUpperCase());
            if (record.status.equalsIgnoreCase("available")) {
                holder.tvVehicleStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                holder.tvVehicleStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            // Edit Action launches AdminAddEditVehicleActivity with VEHICLE_ID extra
            holder.ivEditVehicle.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManageVehiclesActivity.this, AdminAddEditVehicleActivity.class);
                intent.putExtra("VEHICLE_ID", record.id);
                startActivity(intent);
            });

            // Delete Action
            holder.ivDeleteVehicle.setOnClickListener(v -> {
                deleteVehicle(record.id, position);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvVehicleName, tvVehicleNum, tvVehiclePrice, tvVehicleStatus;
            ImageView ivEditVehicle, ivDeleteVehicle;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvVehicleName = itemView.findViewById(R.id.tvVehicleName);
                tvVehicleNum = itemView.findViewById(R.id.tvVehicleNum);
                tvVehiclePrice = itemView.findViewById(R.id.tvVehiclePrice);
                tvVehicleStatus = itemView.findViewById(R.id.tvVehicleStatus);
                ivEditVehicle = itemView.findViewById(R.id.ivEditVehicle);
                ivDeleteVehicle = itemView.findViewById(R.id.ivDeleteVehicle);
            }
        }
    }
}
