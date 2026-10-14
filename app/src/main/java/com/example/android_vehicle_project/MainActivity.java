package com.example.android_vehicle_project;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private ImageView ivTogglePassword;
    private Button btnLogin, btnLoginAsAdmin;
    private TextView tvForgotPassword, tvRegister;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginAsAdmin = findViewById(R.id.btnLoginAsAdmin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        // Password visibility toggle
        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isPasswordVisible = false;
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isPasswordVisible = true;
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Register link
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Forgot password link
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Please contact the administrator to reset your password", Toast.LENGTH_LONG).show();
        });

        // Login as Admin button
        btnLoginAsAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });

        // Customer Login button
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check database credentials
            DatabaseHandler dbHandler = new DatabaseHandler(MainActivity.this);
            if (dbHandler.checkUserCredentials(username, password, "customer")) {
                String firstName = "Customer";
                android.database.Cursor cursor = dbHandler.getUserByUsername(username);
                if (cursor != null && cursor.moveToFirst()) {
                    int fnameIndex = cursor.getColumnIndex(DatabaseHandler.COLUMN_FIRST_NAME);
                    if (fnameIndex != -1) {
                        firstName = cursor.getString(fnameIndex);
                    }
                    cursor.close();
                }
                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, CustomerHomeActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("FIRST_NAME", firstName);
                startActivity(intent);
                finish();
                return;
            }

            // Fallback for SharedPreferences or demo accounts
            SharedPreferences prefs = getSharedPreferences("RideRentPrefs", MODE_PRIVATE);

            // Default demo customer
            if ((username.equals("abcd") && password.equals("abcd")) || (username.equals("priya") && password.equals("123456"))) {
                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, CustomerHomeActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("FIRST_NAME", "Priya");
                startActivity(intent);
                finish();
                return;
            }

            // Check registered users in SharedPreferences
            String savedPassword = prefs.getString("pwd_" + username, null);
            if (savedPassword != null && savedPassword.equals(password)) {
                String firstName = prefs.getString("fname_" + username, "Customer");
                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, CustomerHomeActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("FIRST_NAME", firstName);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

