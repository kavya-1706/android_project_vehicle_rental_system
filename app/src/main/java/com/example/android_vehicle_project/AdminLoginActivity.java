package com.example.android_vehicle_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * AdminLoginActivity handles system administrator login.
 * Validates admin credentials against SQLite database users table ('admin' role)
 * or default fallback credentials before granting access to AdminDashboardActivity.
 */
public class AdminLoginActivity extends AppCompatActivity {

    // View declarations
    private EditText etAdminUsername, etAdminPassword;
    private ImageView ivAdminTogglePassword;
    private Button btnAdminLoginSubmit;
    private ScrollView scrollViewAdminLogin;

    // Password visibility toggle flag
    private boolean isPasswordVisible = false;

    /**
     * Sets up views, toolbar, focus listeners, and click handlers upon activity creation.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Toolbar setup with back arrow navigation
        Toolbar toolbar = findViewById(R.id.toolbarAdminLogin);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind layout views
        scrollViewAdminLogin = findViewById(R.id.scrollViewAdminLogin);
        etAdminUsername = findViewById(R.id.etAdminUsername);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        ivAdminTogglePassword = findViewById(R.id.ivAdminTogglePassword);
        btnAdminLoginSubmit = findViewById(R.id.btnAdminLoginSubmit);

        // Auto-scroll listener to keep focused text input fields visible above soft keyboard
        View.OnFocusChangeListener autoScrollFocusListener = (v, hasFocus) -> {
            if (hasFocus && scrollViewAdminLogin != null) {
                scrollViewAdminLogin.postDelayed(() -> {
                    scrollViewAdminLogin.smoothScrollTo(0, v.getBottom() + 100);
                }, 150);
            }
        };

        etAdminUsername.setOnFocusChangeListener(autoScrollFocusListener);
        etAdminPassword.setOnFocusChangeListener(autoScrollFocusListener);

        // Password visibility toggle listener
        ivAdminTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide password
                etAdminPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isPasswordVisible = false;
            } else {
                // Reveal plain text password
                etAdminPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isPasswordVisible = true;
            }
            etAdminPassword.setSelection(etAdminPassword.getText().length());
        });

        // Admin Login submission handler
        btnAdminLoginSubmit.setOnClickListener(v -> {
            String username = etAdminUsername.getText().toString().trim();
            String password = etAdminPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter admin username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check admin credentials in SQLite users table
            DatabaseHandler dbHandler = new DatabaseHandler(AdminLoginActivity.this);
            boolean isValidAdminInDb = dbHandler.checkUserCredentials(username, password, "admin");

            // Validate against DB or fallback demo admin credentials
            if (isValidAdminInDb || (username.equals("admin") && (password.equals("admin") || password.equals("admin123")))) {
                Toast.makeText(this, "Admin Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
