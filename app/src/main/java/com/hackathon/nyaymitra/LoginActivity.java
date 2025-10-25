package com.hackathon.nyaymitra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hackathon.nyaymitra.activities.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private TextView tvUsernameError, tvRoleError, tvToggle;
    private RadioGroup rgRole;
    private Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvUsernameError = findViewById(R.id.tvUsernameError);
        tvRoleError = findViewById(R.id.tvRoleError);
        rgRole = findViewById(R.id.rgRole);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvToggle = findViewById(R.id.tvToggle);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSignIn();
            }
        });

        tvToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void validateAndSignIn() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        int selectedRoleId = rgRole.getCheckedRadioButtonId();

        tvUsernameError.setVisibility(View.GONE);
        tvRoleError.setVisibility(View.GONE);

        boolean isValid = true;

        if (username.isEmpty()) {
            tvUsernameError.setText("Username cannot be empty");
            tvUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            tvUsernameError.setText("Please enter a valid email");
            tvUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (selectedRoleId == -1) {
            tvRoleError.setText("Please select a role (User or Lawyer)");
            tvRoleError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (isValid) {

            // --- Save the user's email ---
            SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("email", username); // 'username' is the email
            editor.apply();

            Toast.makeText(this, "Sign In Successful (Mock)", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}