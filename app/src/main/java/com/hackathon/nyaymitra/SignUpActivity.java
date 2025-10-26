package com.hackathon.nyaymitra;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {

    private EditText etSignUpEmail, etSignUpPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvToggleToSignIn;
    private RadioGroup rgSignUpRole;

    private FirebaseAuth mAuth;

    // --- Executor and Handler for background tasks ---
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String TAG = "SignUpActivity";
    // --- SET YOUR FLASK URL HERE ---
    private static final String BACKEND_URL = "https://nyay-mitra-flask.onrender.com/api/user/login-or-register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etSignUpEmail = findViewById(R.id.etSignUpEmail);
        etSignUpPassword = findViewById(R.id.etSignUpPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvToggleToSignIn = findViewById(R.id.tvToggleToSignIn);
        rgSignUpRole = findViewById(R.id.rgSignUpRole);

        mAuth = FirebaseAuth.getInstance();

        btnSignUp.setOnClickListener(v -> validateAndSignUp());
        tvToggleToSignIn.setOnClickListener(v -> finish());
    }

    private void validateAndSignUp() {
        String email = etSignUpEmail.getText().toString().trim();
        String password = etSignUpPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        int selectedRoleId = rgSignUpRole.getCheckedRadioButtonId();

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

        // Assuming you have IDs R.id.rbLawyer and R.id.rbClient in your XML
        String selectedRole = (selectedRoleId == R.id.rbLawyer) ? "lawyer" : "client";

        // Create user in Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        registerUserInBackend(user, selectedRole);
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "Google Authentication Success!: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUserInBackend(FirebaseUser user, String role) {
        if (user == null) return;
        String email = user.getEmail();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
            if (!tokenTask.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", tokenTask.getException());
                postToast("Failed to get device token. Cannot register.");
                return;
            }
            String fcmToken = tokenTask.getResult();

            // Run network call on background thread
            executor.execute(() -> {
                HttpURLConnection connection = null;
                boolean registrationSuccess = false;
                String responseMessage = "Server registration failed. Please try again.";

                try {
                    JSONObject payload = new JSONObject();
                    payload.put("email", email);
                    payload.put("fcm_token", fcmToken);
                    payload.put("role", role);
                    String jsonPayload = payload.toString();

                    URL url = new URL(BACKEND_URL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setDoOutput(true);

                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                        registrationSuccess = true;
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                            JSONObject jsonResponse = new JSONObject(br.readLine());
                            responseMessage = jsonResponse.getString("message");
                        }
                    } else {
                        Log.e(TAG, "Backend Error: " + responseCode);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "HttpURLConnection Failure: " + e.getMessage());
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }

                    // Post UI updates back to the main thread
                    postToast(responseMessage);
                    if (registrationSuccess) {
                        navigateToLogin();
                    } else {
                        // Delete the Firebase user if backend failed
                        if (user != null) {
                            user.delete().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Log.d(TAG, "Firebase user deleted after backend failure.");
                                }
                            });
                        }
                    }
                }
            });
        });
    }

    private void navigateToLogin() {
        handler.post(() -> {
            Toast.makeText(this, "Registration successful! Please sign in.", Toast.LENGTH_LONG).show();
            mAuth.signOut();
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void postToast(String message) {
        handler.post(() -> Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}