package com.example.android_vehicle_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity serves as the main entry point and Customer Login Screen.
 * It allows customers to enter credentials, toggle password visibility, navigate to Registration,
 * switch to Admin Login, or authenticate and proceed to CustomerHomeActivity.
 */
public class MainActivity extends AppCompatActivity {

    // UI View Declarations
    private EditText etUsername, etPassword;
    private ImageView ivTogglePassword;
    private Button btnLogin, btnLoginAsAdmin;
    private TextView tvRegister;
    private ScrollView scrollViewMain;

    // Password visibility flag
    private boolean isPasswordVisible = false;

    /**
     * Initializes UI layout, view bindings, focus listeners, and click handlers on Activity launch.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind layout views
        scrollViewMain = findViewById(R.id.scrollViewMain);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginAsAdmin = findViewById(R.id.btnLoginAsAdmin);
        tvRegister = findViewById(R.id.tvRegister);

        // Auto-scroll focus listener ensuring focused input field stays visible above the soft keyboard
        View.OnFocusChangeListener autoScrollFocusListener = (v, hasFocus) -> {
            if (hasFocus && scrollViewMain != null) {
                scrollViewMain.postDelayed(() -> {
                    scrollViewMain.smoothScrollTo(0, v.getBottom() + 100);
                }, 150);
            }
        };

        etUsername.setOnFocusChangeListener(autoScrollFocusListener);
        etPassword.setOnFocusChangeListener(autoScrollFocusListener);

        // Password visibility toggle handler
        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide password into dots
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isPasswordVisible = false;
            } else {
                // Show plain text password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isPasswordVisible = true;
            }
            // Keep cursor at the end of input
            etPassword.setSelection(etPassword.getText().length());
        });

        // Navigate to Customer Registration screen
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Navigate to Admin Login screen
        btnLoginAsAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });

        // Handle Customer Login authentication
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate non-empty inputs
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Authenticate against SQLite Database
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

            // Fallback check against SharedPreferences or demo accounts
            SharedPreferences prefs = getSharedPreferences("RideRentPrefs", MODE_PRIVATE);

            // Default demo customer fallback
//            if ((username.equals("abcd") && password.equals("abcd")) || (username.equals("priya") && password.equals("123456"))) {
//                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(MainActivity.this, CustomerHomeActivity.class);
//                intent.putExtra("USERNAME", username);
//                intent.putExtra("FIRST_NAME", "Priya");
//                startActivity(intent);
//                finish();
//                return;
//            }

            // Check registered users stored in SharedPreferences
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
