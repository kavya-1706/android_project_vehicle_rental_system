package com.example.android_vehicle_project;

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

import java.util.ArrayList;
import java.util.List;

public class AdminManageUsersActivity extends AppCompatActivity {

    private ImageView ivBack;
    private RecyclerView rvAdminUsers;
    
    private DatabaseHandler dbHandler;
    private UsersAdapter adapter;
    private List<UserRecord> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);

        dbHandler = new DatabaseHandler(this);

        ivBack = findViewById(R.id.ivBack);
        rvAdminUsers = findViewById(R.id.rvAdminUsers);

        ivBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadDataFromDatabase();
    }

    private void setupRecyclerView() {
        userList = new ArrayList<>();
        adapter = new UsersAdapter(userList);
        rvAdminUsers.setLayoutManager(new LinearLayoutManager(this));
        rvAdminUsers.setAdapter(adapter);
    }

    private void loadDataFromDatabase() {
        userList.clear();
        Cursor cursor = dbHandler.getAllUsers();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_USER_ID));
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_LAST_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_PHONE));
                String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.COLUMN_ROLE));

                UserRecord record = new UserRecord(id, firstName + " " + lastName, email, phone, role);
                userList.add(record);
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private void deleteUser(int userId, int position) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to completely remove this user account?")
                .setPositiveButton("Delete", (dialogInterface, which) -> {
                    boolean deleted = dbHandler.deleteUser(userId);
                    if (deleted) {
                        Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                        userList.remove(position);
                        adapter.notifyItemRemoved(position);
                    } else {
                        Toast.makeText(this, "Failed to delete. User might have existing bookings.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.error_red));
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.white));
        }
    }

    // ------------------ INNER CLASSES FOR RECYCLER VIEW ------------------ //

    private static class UserRecord {
        int id;
        String name, email, phone, role;

        public UserRecord(int id, String name, String email, String phone, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
        }
    }

    private class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

        private final List<UserRecord> items;

        public UsersAdapter(List<UserRecord> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserRecord record = items.get(position);

            holder.tvUserName.setText(record.name);
            holder.tvUserEmail.setText(record.email);
            holder.tvUserPhone.setText(record.phone);
            holder.tvUserRole.setText(record.role.toUpperCase());

            if (record.role.equalsIgnoreCase("admin")) {
                holder.tvUserRole.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                holder.ivDeleteUser.setVisibility(View.GONE); // Don't allow deleting admins here easily
            } else {
                holder.tvUserRole.setTextColor(getResources().getColor(R.color.primary_navy));
                holder.ivDeleteUser.setVisibility(View.VISIBLE);
            }

            // Delete Action
            holder.ivDeleteUser.setOnClickListener(v -> {
                deleteUser(record.id, position);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserName, tvUserEmail, tvUserPhone, tvUserRole;
            ImageView ivDeleteUser;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUserName = itemView.findViewById(R.id.tvUserName);
                tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
                tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
                tvUserRole = itemView.findViewById(R.id.tvUserRole);
                ivDeleteUser = itemView.findViewById(R.id.ivDeleteUser);
            }
        }
    }
}