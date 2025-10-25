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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private TextView tvUsernameError, tvRoleError, tvToggle;
    private RadioGroup rgRole;
    private Button btnSignIn;

    // --- NEW: Firebase, Google & Volley ---
    private SignInButton btnGoogleSignIn;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private RequestQueue requestQueue;

    private static final String TAG = "LoginActivity";
    // IMPORTANT: Change this to your Flask server's URL
    private static final String BACKEND_URL = "http://10.92.184.135:5000/api/user/login-or-register";

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
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn); // From activity_login.xml

        // --- NEW: Initialize Firebase, Google, Volley ---
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);

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
                            Log.w(TAG, "Google sign in failed", e);
                            Toast.makeText(LoginActivity.this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Google Sign-In Cancelled", Toast.LENGTH_SHORT).show();
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

                        // --- NEW: Check if user is new ---
                        AuthResult authResult = task.getResult();
                        boolean isNewUser = authResult.getAdditionalUserInfo().isNewUser();

                        if (isNewUser) {
                            // NEW USER: Go to SelectRoleActivity
                            Toast.makeText(this, "Welcome! Please select your role.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, SelectRoleActivity.class);
                            startActivity(intent);
                            finish(); // Finish LoginActivity
                        } else {
                            // EXISTING USER: Call backend to update token and get role
                            callBackendLogin(user, null); // Pass null for role, backend will retrieve it
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // --- Email/Password Sign-In Flow ---
    private void validateAndSignIn() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        int selectedRoleId = rgRole.getCheckedRadioButtonId();

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

        // Role is NOT needed for login, only for sign-up.
        // We will fetch the role from our backend.
        // We can remove the role check from login.
        /*
        if (selectedRoleId == -1) {
            tvRoleError.setText("Please select a role (User or Lawyer)");
            tvRoleError.setVisibility(View.VISIBLE);
            isValid = false;
        }
        */

        if (isValid) {
            // --- NEW: Sign in with Firebase ---
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Call backend to update token and get role
                            callBackendLogin(user, null); // Pass null for role
                        } else {
                            // If sign in fails
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // --- NEW: Common Backend Call Function ---
    /**
     * Calls the Flask backend after a successful Firebase login.
     * @param user The FirebaseUser who just logged in.
     * @param role The user's role (ONLY provided on email sign-up, otherwise null).
     */
    private void callBackendLogin(FirebaseUser user, @Nullable String role) {
        String email = user.getEmail();

        // 1. Get FCM Token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                Toast.makeText(this, "Login successful, but failed to update device token.", Toast.LENGTH_SHORT).show();
                // Still go to main, but backend call failed
                navigateToMain();
                return;
            }
            String fcmToken = task.getResult();

            // 2. Create JSON Payload
            JSONObject payload = new JSONObject();
            try {
                payload.put("email", email);
                payload.put("fcm_token", fcmToken);
                if (role != null) {
                    // This is for a new user registration (from SignUpActivity)
                    payload.put("role", role);
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON Exception: " + e.getMessage());
                navigateToMain(); // Fail gracefully
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
                            navigateToMain(); // Fail gracefully
                        }
                    },
                    error -> {
                        // Backend call failed
                        Log.e(TAG, "Volley Error: " + error.toString());
                        Toast.makeText(this, "Google Login successful", Toast.LENGTH_SHORT).show();
                        navigateToMain(); // Fail gracefully
                    });

            requestQueue.add(jsonObjectRequest);
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close the LoginActivity
    }
}