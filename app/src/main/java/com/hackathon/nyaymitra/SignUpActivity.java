package com.hackathon.nyaymitra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText etSignUpEmail, etSignUpPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvToggleToSignIn;
    // You'll also need to initialize your RadioGroup:
    // private RadioGroup rgSignUpRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etSignUpEmail = findViewById(R.id.etSignUpEmail);
        etSignUpPassword = findViewById(R.id.etSignUpPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvToggleToSignIn = findViewById(R.id.tvToggleToSignIn);
        // rgSignUpRole = findViewById(R.id.rgSignUpRole);

        // Click listener for the "Sign Up" button
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Add validation logic for sign up
                // (e.g., check if passwords match, email is valid, etc.)

                // For now, just show a message
                Toast.makeText(SignUpActivity.this, "Sign Up Clicked (Mock)", Toast.LENGTH_SHORT).show();

                // After successful sign up, you might go to Login or Main
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            }
        });

        // Click listener to go back to "Sign In"
        tvToggleToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to LoginActivity
                finish(); // Just close this activity to return to the previous one
            }
        });
    }
}