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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hackathon.nyaymitra.activities.MainActivity;
import com.hackathon.nyaymitra.activities.SelectRoleActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private TextView tvUsernameError, tvRoleError, tvToggle;
    private RadioGroup rgRole;
    private Button btnSignIn;
    private SignInButton btnGoogleSignIn;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private static final String TAG = "LoginActivity";

    // --- Executor and Handler for background tasks ---
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // --- SET YOUR FLASK URL HERE ---
    // Use "http://10.0.2.2:5000" for emulator
    // Use "http://YOUR_WIFI_IP:5000" for physical phone (e.g., "http://192.168.1.5:5000")
    private static final String BACKEND_URL = "https://nyay-mitra-flask.onrender.com/api/user/login-or-register";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvUsernameError = findViewById(R.id.tvUsernameError);
        tvRoleError = findViewById(R.id.tvRoleError);
        rgRole = findViewById(R.id.rgRole);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvToggle = findViewById(R.id.tvToggle);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Log.w(TAG, "Google sign in success", e);
                            Toast.makeText(LoginActivity.this, "Google Sign-In success", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Google Sign-In success", Toast.LENGTH_SHORT).show();
                    }
                });

        // --- Click Listeners ---
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        btnSignIn.setOnClickListener(v -> validateAndSignIn());
        tvToggle.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    // --- Google Sign-In Flow ---
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        AuthResult authResult = task.getResult();
                        boolean isNewUser = authResult.getAdditionalUserInfo().isNewUser();

                        if (isNewUser) {
                            // NEW USER: Go to SelectRoleActivity
                            Toast.makeText(this, "Welcome! Please select your role.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, SelectRoleActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // EXISTING USER: Call backend to update token and get role
                            callBackendLogin(user, null);
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Google Authentication Success.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // --- Email/Password Sign-In Flow ---
    private void validateAndSignIn() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        tvUsernameError.setVisibility(View.GONE);
        tvRoleError.setVisibility(View.GONE);
        boolean isValid = true;

        if (username.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            tvUsernameError.setText("Please enter a valid email");
            tvUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Calls the Flask backend using HttpURLConnection on a background thread.
     * @param user The FirebaseUser who just logged in.
     * @param role The user's role (null for login, "client" or "lawyer" for signup).
     */
    private void callBackendLogin(FirebaseUser user, @Nullable String role) {
        String email = user.getEmail();

        // 1. Get FCM Token first (this is also async)
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
            if (!tokenTask.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", tokenTask.getException());
                postToast("Google Authentication Success.");
                navigateToMain();
                return;
            }
            String fcmToken = tokenTask.getResult();

            // 2. Run the network request on a background thread
            executor.execute(() -> {
                HttpURLConnection connection = null;
                String responseMessage = "Google Login successful."; // Default message

                try {
                    // 3. Create JSON Payload
                    JSONObject payload = new JSONObject();
                    payload.put("email", email);
                    payload.put("fcm_token", fcmToken);
                    if (role != null) {
                        payload.put("role", role);
                    }
                    String jsonPayload = payload.toString();

                    // 4. Setup Connection
                    URL url = new URL(BACKEND_URL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(10000); // 10 seconds
                    connection.setReadTimeout(10000); // 10 seconds

                    // 5. Write Data
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    // 6. Read Response
                    int responseCode = connection.getResponseCode();
                    Log.d(TAG, "POST Response Code :: " + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        // Read success response
                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                            // Parse the message from the server's JSON response
                            JSONObject jsonResponse = new JSONObject(response.toString());
                            responseMessage = jsonResponse.getString("message");
                        }
                    } else {
                        Log.e(TAG, "Backend Error: " + responseCode);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "HttpURLConnection Failure: " + e.getMessage());
                    responseMessage = "Network error. Please try again.";

                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }

                    // 7. Post result back to UI thread and navigate
                    postToast(responseMessage);
                    navigateToMain();
                }
            });
        });
    }

    // Helper method to show a Toast from any thread
    private void postToast(String message) {
        handler.post(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show());
    }

    private void navigateToMain() {
        // Make sure navigation also happens on the UI thread
        handler.post(() -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}