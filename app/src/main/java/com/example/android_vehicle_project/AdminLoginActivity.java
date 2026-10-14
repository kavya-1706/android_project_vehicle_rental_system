package com.example.android_vehicle_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminUsername, etAdminPassword;
    private ImageView ivAdminTogglePassword;
    private Button btnAdminLoginSubmit;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        Toolbar toolbar = findViewById(R.id.toolbarAdminLogin);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etAdminUsername = findViewById(R.id.etAdminUsername);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        ivAdminTogglePassword = findViewById(R.id.ivAdminTogglePassword);
        btnAdminLoginSubmit = findViewById(R.id.btnAdminLoginSubmit);

        ivAdminTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etAdminPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isPasswordVisible = false;
            } else {
                etAdminPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isPasswordVisible = true;
            }
            etAdminPassword.setSelection(etAdminPassword.getText().length());
        });

        btnAdminLoginSubmit.setOnClickListener(v -> {
            String username = etAdminUsername.getText().toString().trim();
            String password = etAdminPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter admin username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHandler dbHandler = new DatabaseHandler(AdminLoginActivity.this);
            boolean isValidAdminInDb = dbHandler.checkUserCredentials(username, password, "admin");

            // Demo admin credentials: admin / admin or admin / admin123
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
