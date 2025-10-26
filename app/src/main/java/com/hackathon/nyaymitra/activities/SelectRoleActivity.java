package com.hackathon.nyaymitra.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hackathon.nyaymitra.LoginActivity;
import com.hackathon.nyaymitra.R;

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

    // --- Executor and Handler for background tasks ---
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String TAG = "SelectRoleActivity";
    // --- SET YOUR FLASK URL HERE ---
    private static final String BACKEND_URL = "https://nyay-mitra-flask.onrender.com/api/user/login-or-register";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_role);

        rgSelectRole = findViewById(R.id.rgSelectRole);
        btnConfirmRole = findViewById(R.id.btnConfirmRole);
        mAuth = FirebaseAuth.getInstance();

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

    private void registerNewUserInBackend(FirebaseUser user) {
        // Assuming you have IDs R.id.rbSelectLawyer and R.id.rbSelectUser
        String selectedRole = (rgSelectRole.getCheckedRadioButtonId() == R.id.rbSelectLawyer) ? "lawyer" : "client";
        String email = user.getEmail();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
            if (!tokenTask.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", tokenTask.getException());
                postToast("Failed to get device token. Cannot register.");
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

    private void navigateToMain() {
        handler.post(() -> {
            Intent intent = new Intent(SelectRoleActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void postToast(String message) {
        handler.post(() -> Toast.makeText(SelectRoleActivity.this, message, Toast.LENGTH_LONG).show());
    }
}