package com.hackathon.nyaymitra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity {

    private EditText etSignUpEmail, etSignUpPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvToggleToSignIn;
    // --- NEW: Add RadioGroup ---
    private RadioGroup rgSignUpRole;

    // --- NEW: Firebase & Volley ---
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;

    private static final String TAG = "SignUpActivity";
    // IMPORTANT: Change this to your Flask server's URL
    private static final String BACKEND_URL = "http://10.92.184.135:5000/api/user/login-or-register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etSignUpEmail = findViewById(R.id.etSignUpEmail);
        etSignUpPassword = findViewById(R.id.etSignUpPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvToggleToSignIn = findViewById(R.id.tvToggleToSignIn);

        // --- NEW: Initialize ---
        // Make sure you have this ID in your activity_sign_up.xml
        rgSignUpRole = findViewById(R.id.rgSignUpRole);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);

        btnSignUp.setOnClickListener(v -> validateAndSignUp());

        tvToggleToSignIn.setOnClickListener(v -> finish());
    }

    private void validateAndSignUp() {
        String email = etSignUpEmail.getText().toString().trim();
        String password = etSignUpPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        int selectedRoleId = rgSignUpRole.getCheckedRadioButtonId();

        // --- NEW: Validation ---
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etSignUpEmail.setError("Please enter a valid email");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            etSignUpPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get role string
        String selectedRole = (selectedRoleId == R.id.rbLawyer) ? "lawyer" : "client"; // Assuming R.id.rbLawyer exists

        // --- NEW: Create user in Firebase ---
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Firebase Sign up success
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Now, register this new user in our backend
                        registerUserInBackend(user, selectedRole);
                    } else {
                        // If sign in fails
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUserInBackend(FirebaseUser user, String role) {
        if (user == null) return;
        String email = user.getEmail();

        // 1. Get FCM Token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                // Proceed without token? Or show error? For now, show error.
                Toast.makeText(this, "Failed to get device token. Cannot register in backend.", Toast.LENGTH_SHORT).show();
                return;
            }
            String fcmToken = task.getResult();

            // 2. Create JSON Payload
            JSONObject payload = new JSONObject();
            try {
                payload.put("email", email);
                payload.put("fcm_token", fcmToken);
                payload.put("role", role); // Role is required
            } catch (JSONException e) {
                Log.e(TAG, "JSON Exception: " + e.getMessage());
                return;
            }

            // 3. Send to Flask Backend
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, BACKEND_URL, payload,
                    response -> {
                        // Backend call was successful
                        try {
                            String message = response.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                            // 4. Send user to Login screen
                            Toast.makeText(this, "Registration successful! Please sign in.", Toast.LENGTH_LONG).show();
                            mAuth.signOut(); // Sign out so they have to log in
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            Log.e(TAG, "Backend JSON response error: " + e.getMessage());
                        }
                    },
                    error -> {
                        // Backend call failed
                        Log.e(TAG, "Volley Error: " + error.toString());
                        // If backend fails, we should probably delete the Firebase user to avoid de-sync
                        user.delete().addOnCompleteListener(deleteTask -> {
                            if(deleteTask.isSuccessful()) {
                                Log.d(TAG, "Firebase user deleted after backend failure.");
                            }
                        });
                        Toast.makeText(this, "Server registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                    });

            requestQueue.add(jsonObjectRequest);
        });
    }
}