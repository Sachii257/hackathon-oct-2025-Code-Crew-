package com.hackathon.nyaymitra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Make sure this import is correct for your MainActivity
import com.hackathon.nyaymitra.activities.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private TextView tvUsernameError, tvRoleError, tvToggle;
    private RadioGroup rgRole;
    private Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure this layout name is correct
        setContentView(R.layout.activity_login);

        // Initialize all the views from your layout
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvUsernameError = findViewById(R.id.tvUsernameError);
        tvRoleError = findViewById(R.id.tvRoleError);
        rgRole = findViewById(R.id.rgRole);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvToggle = findViewById(R.id.tvToggle);

        // Set click listener for the Sign In button
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSignIn();
            }
        });

        // Set click listener for the "Sign Up" toggle text
        tvToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Make sure you have a SignUpActivity.java
                // If you don't have this class yet, comment out the next two lines
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void validateAndSignIn() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        int selectedRoleId = rgRole.getCheckedRadioButtonId();

        // Reset previous errors
        tvUsernameError.setVisibility(View.GONE);
        tvRoleError.setVisibility(View.GONE);

        boolean isValid = true;

        // 1. Validate Username (check if empty or not a valid email)
        if (username.isEmpty()) {
            tvUsernameError.setText("Username cannot be empty");
            tvUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            // You can remove this if you allow non-email usernames
            tvUsernameError.setText("Please enter a valid email");
            tvUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // 2. Validate Password
        if (password.isEmpty()) {
            // We don't have a password error TextView, so we'll use a Toast
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // 3. Validate Role
        if (selectedRoleId == -1) { // -1 means no RadioButton is checked
            tvRoleError.setText("Please select a role (User or Lawyer)");
            tvRoleError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // If all fields are valid, proceed
        if (isValid) {
            // --- TODO: Implement your real authentication logic here ---
            // (e.g., check with Firebase Auth or your backend API)

            // For now, we'll just pretend the login is successful
            Toast.makeText(this, "Sign In Successful (Mock)", Toast.LENGTH_SHORT).show();

            // Navigate to the main app screen
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            // Clear the activity stack so the user can't press "back" to return to Login
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Close the LoginActivity
        }
    } // <-- THIS BRACE CLOSES validateAndSignIn

} // <-- THIS BRACE CLOSES LoginActivity