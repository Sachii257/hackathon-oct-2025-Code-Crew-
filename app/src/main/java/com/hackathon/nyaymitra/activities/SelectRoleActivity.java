package com.hackathon.nyaymitra.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SelectRoleActivity extends AppCompatActivity {

    private RadioGroup rgSelectRole;
    private Button btnConfirmRole;
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;

    private static final String TAG = "SelectRoleActivity";
    // IMPORTANT: Change this to your Flask server's URL
    private static final String BACKEND_URL = "http://10.92.184.135:5000/api/user/login-or-register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_role);

        rgSelectRole = findViewById(R.id.rgSelectRole);
        btnConfirmRole = findViewById(R.id.btnConfirmRole);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);

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
        String selectedRole = (rgSelectRole.getCheckedRadioButtonId() == R.id.rbSelectLawyer) ? "lawyer" : "client";
        String email = user.getEmail();

        // 1. Get FCM Token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                Toast.makeText(this, "Failed to get device token. Cannot register.", Toast.LENGTH_SHORT).show();
                return;
            }
            String fcmToken = task.getResult();

            // 2. Create JSON Payload
            JSONObject payload = new JSONObject();
            try {
                payload.put("email", email);
                payload.put("fcm_token", fcmToken);
                payload.put("role", selectedRole); // Role is required for new user
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
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                            // 4. Navigate to Main Activity
                            navigateToMain();
                        } catch (JSONException e) {
                            Log.e(TAG, "Backend JSON response error: " + e.getMessage());
                        }
                    },
                    error -> {
                        // Backend call failed
                        Log.e(TAG, "Volley Error: " + error.toString());
                        Toast.makeText(this, "Server registration failed.", Toast.LENGTH_SHORT).show();
                    });

            requestQueue.add(jsonObjectRequest);
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(SelectRoleActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}