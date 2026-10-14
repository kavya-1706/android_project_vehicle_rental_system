package com.example.android_vehicle_project;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private EditText etSearchAdmin;
    private LinearLayout cardManageVehicles, cardViewBookings, cardCustomerPayments, cardFeedback, cardLateFines;
    private ImageView ivAdminLogout, ivFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        etSearchAdmin = findViewById(R.id.etSearchAdmin);
        ivAdminLogout = findViewById(R.id.ivAdminLogout);
        ivFilter = findViewById(R.id.ivFilter);

        cardManageVehicles = findViewById(R.id.cardManageVehicles);
        cardViewBookings = findViewById(R.id.cardViewBookings);
        cardCustomerPayments = findViewById(R.id.cardCustomerPayments);
        cardFeedback = findViewById(R.id.cardFeedback);
        cardLateFines = findViewById(R.id.cardLateFines);

        ivAdminLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        ivFilter.setOnClickListener(v -> Toast.makeText(this, "Filter options", Toast.LENGTH_SHORT).show());

        // Card clicks open FeaturePlaceholderActivity
        cardManageVehicles.setOnClickListener(v -> openPlaceholder("Manage Vehicles", "Add / edit / remove vehicles"));
        cardViewBookings.setOnClickListener(v -> openPlaceholder("View Bookings", "All customer bookings"));
        cardCustomerPayments.setOnClickListener(v -> openPlaceholder("Customer Payments", "Payment records"));
        cardFeedback.setOnClickListener(v -> openPlaceholder("Feedback", "Customer reviews"));
        cardLateFines.setOnClickListener(v -> openPlaceholder("Late Return Fines", "Manage overdue fines"));

        // Search filter
        etSearchAdmin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDashboard(s.toString().toLowerCase().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void openPlaceholder(String title, String subtitle) {
        Intent intent = new Intent(AdminDashboardActivity.this, FeaturePlaceholderActivity.class);
        intent.putExtra("FEATURE_TITLE", title);
        intent.putExtra("FEATURE_SUBTITLE", subtitle);
        startActivity(intent);
    }

    private void filterDashboard(String query) {
        boolean m1 = query.isEmpty() || "manage vehicles".contains(query) || "vehicles".contains(query);
        boolean m2 = query.isEmpty() || "view bookings".contains(query) || "bookings".contains(query);
        boolean m3 = query.isEmpty() || "customer payments".contains(query) || "payments".contains(query);
        boolean m4 = query.isEmpty() || "feedback".contains(query) || "reviews".contains(query);
        boolean m5 = query.isEmpty() || "late return fines".contains(query) || "fines".contains(query);

        cardManageVehicles.setVisibility(m1 ? View.VISIBLE : View.GONE);
        cardViewBookings.setVisibility(m2 ? View.VISIBLE : View.GONE);
        cardCustomerPayments.setVisibility(m3 ? View.VISIBLE : View.GONE);
        cardFeedback.setVisibility(m4 ? View.VISIBLE : View.GONE);
        cardLateFines.setVisibility(m5 ? View.VISIBLE : View.GONE);
    }
}

