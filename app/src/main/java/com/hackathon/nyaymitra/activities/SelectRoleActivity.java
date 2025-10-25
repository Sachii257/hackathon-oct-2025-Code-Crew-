package com.hackathon.nyaymitra.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Keep Handler import from main
import android.os.Looper;  // Keep Looper import from main
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// --- REMOVE VOLLEY IMPORTS ---
// import com.android.volley.Request;
// import com.android.volley.RequestQueue;
// import com.android.volley.toolbox.JsonObjectRequest;
// import com.android.volley.toolbox.Volley;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

// Keep App specific imports
import com.hackathon.nyaymitra.LoginActivity; // Keep import for error navigation
import com.hackathon.nyaymitra.R;

// Keep HttpURLConnection related imports from main
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SelectRoleActivity extends AppCompatActivity {

    private RadioGroup rgSelectRole;
    private Button btnConfirmRole;
    private FirebaseAuth mAuth;

    // --- Keep Executor and Handler from main ---
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String TAG = "SelectRoleActivity";
    // --- Keep BACKEND_URL from main ---
    private static final String BACKEND_URL = "https://a0b4be4631c8.ngrok-free.app/api/user/login-or-register";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_role);

        rgSelectRole = findViewById(R.id.rgSelectRole);
        btnConfirmRole = findViewById(R.id.btnConfirmRole);
        mAuth = FirebaseAuth.getInstance();
        // Removed Volley initialization

        // Keep button listener logic
        btnConfirmRole.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            registerNewUserInBackend(user);
        });
    }

    // --- Keep the entire registerNewUserInBackend method from main ---
    private void registerNewUserInBackend(FirebaseUser user) {
        // Assuming you have IDs R.id.rbSelectLawyer and R.id.rbSelectUser in your XML
        String selectedRole = (rgSelectRole.getCheckedRadioButtonId() == R.id.rbSelectLawyer) ? "lawyer" : "client";
        String email = user.getEmail();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
            if (!tokenTask.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", tokenTask.getException());
                postToast("Failed to get device token. Cannot register.");
                // Optionally sign out and redirect if token is critical
                handler.post(() -> {
                    mAuth.signOut();
                    Intent intent = new Intent(SelectRoleActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
                return;
            }
            String fcmToken = tokenTask.getResult();

            executor.execute(() -> {
                HttpURLConnection connection = null;
                boolean registrationSuccess = false;
                String responseMessage = "Server registration failed.";

                try {
                    JSONObject payload = new JSONObject();
                    payload.put("email", email);
                    payload.put("fcm_token", fcmToken);
                    payload.put("role", selectedRole);
                    String jsonPayload = payload.toString();

                    URL url = new URL(BACKEND_URL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    // Keep ngrok header
                    connection.setRequestProperty("ngrok-skip-browser-warning", "true");
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(15000); // Increased timeout
                    connection.setReadTimeout(15000);  // Increased timeout


                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                    }

                    int responseCode = connection.getResponseCode();
                    Log.d(TAG, "POST Response Code :: " + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                        registrationSuccess = true;
                        // Read response message
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                            JSONObject jsonResponse = new JSONObject(response.toString());
                            responseMessage = jsonResponse.getString("message");
                        }
                    } else {
                        Log.e(TAG, "Backend Error: " + responseCode);
                        // Optional: Read error stream
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                            StringBuilder errorResponse = new StringBuilder();
                            String errorLine;
                            while ((errorLine = br.readLine()) != null) {
                                errorResponse.append(errorLine.trim());
                            }
                            Log.e(TAG, "Error Response Body: " + errorResponse.toString());
                            // Try to parse error message if server sends one in JSON
                            try {
                                JSONObject jsonError = new JSONObject(errorResponse.toString());
                                if (jsonError.has("error")) {
                                    responseMessage = jsonError.getString("error");
                                }
                            } catch (Exception jsonEx) {
                                // Ignore if error body isn't JSON
                            }

                        } catch (Exception readEx) {
                            Log.e(TAG, "Error reading error stream", readEx);
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "HttpURLConnection Failure: " + e.getMessage(), e); // Log full exception
                    responseMessage = "Network error. Please try again."; // More specific error
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }

                    // Always post the final message
                    postToast(responseMessage);

                    if (registrationSuccess) {
                        navigateToMain();
                    } else {
                        // If registration fails, sign out and send back to Login
                        handler.post(() -> {
                            mAuth.signOut();
                            Intent intent = new Intent(SelectRoleActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                    }
                }
            });
        });
    }

    // --- Keep navigateToMain method from main ---
    private void navigateToMain() {
        handler.post(() -> {
            Intent intent = new Intent(SelectRoleActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // --- Keep postToast method from main ---
    private void postToast(String message) {
        handler.post(() -> Toast.makeText(SelectRoleActivity.this, message, Toast.LENGTH_LONG).show());
    }
}