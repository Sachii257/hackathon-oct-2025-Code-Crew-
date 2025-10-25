package com.hackathon.nyaymitra.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.hackathon.nyaymitra.R;

public class UpdateProfileActivity extends AppCompatActivity {

    // --- New UI elements ---
    private ShapeableImageView ivProfilePicture;
    private TextView tvProfileEmail;
    private Uri profileImageUri; // To hold the URI of the selected image

    // --- Existing UI elements ---
    private RadioGroup rgGender;
    private TextInputEditText etAge, etSchool, etCollege, etUg, etPg, etPhd, etAchievements;
    private Button btnSaveProfile;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "UserProfile";

    // --- Activity Launcher for Image Picker ---
    private final ActivityResultLauncher<String[]> galleryPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean isGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        isGranted = false;
                        break;
                    }
                }
                if (isGranted) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Permission denied to read storage", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    profileImageUri = result.getData().getData();
                    ivProfilePicture.setImageURI(profileImageUri);

                    // Crucial for permanent access to the image URI
                    try {
                        getContentResolver().takePersistableUriPermission(profileImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_update_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Find all views
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        tvProfileEmail = findViewById(R.id.tv_profile_email);
        rgGender = findViewById(R.id.rg_gender);
        etAge = findViewById(R.id.et_age);
        etSchool = findViewById(R.id.et_school);
        etCollege = findViewById(R.id.et_college);
        etUg = findViewById(R.id.et_ug);
        etPg = findViewById(R.id.et_pg);
        etPhd = findViewById(R.id.et_phd);
        etAchievements = findViewById(R.id.et_achievements);
        btnSaveProfile = findViewById(R.id.btn_save_profile);

        // Load existing data
        loadUserProfile();

        // --- Set Click Listeners ---
        btnSaveProfile.setOnClickListener(v -> saveUserProfile());

        ivProfilePicture.setOnClickListener(v -> checkPermissionAndOpenGallery());
    }

    private void loadUserProfile() {
        // Load and set Email (from LoginActivity)
        tvProfileEmail.setText(prefs.getString("email", "no.email@provider.com"));

        // Load and set Profile Picture
        String imageUriString = prefs.getString("profileImageUri", null);
        if (imageUriString != null) {
            ivProfilePicture.setImageURI(Uri.parse(imageUriString));
        }

        // Load and set Gender
        int selectedGenderId = prefs.getInt("genderId", -1);
        if (selectedGenderId != -1) {
            rgGender.check(selectedGenderId);
        }

        // Load and set text fields
        etAge.setText(prefs.getString("age", ""));
        etSchool.setText(prefs.getString("school", ""));
        etCollege.setText(prefs.getString("college", ""));
        etUg.setText(prefs.getString("ug", ""));
        etPg.setText(prefs.getString("pg", ""));
        etPhd.setText(prefs.getString("phd", ""));
        etAchievements.setText(prefs.getString("achievements", ""));
    }

    private void saveUserProfile() {
        SharedPreferences.Editor editor = prefs.edit();

        // Save Profile Picture URI (if a new one was selected)
        if (profileImageUri != null) {
            editor.putString("profileImageUri", profileImageUri.toString());
        }

        // Save Gender
        editor.putInt("genderId", rgGender.getCheckedRadioButtonId());

        // Save text fields
        editor.putString("age", etAge.getText().toString());
        editor.putString("school", etSchool.getText().toString());
        editor.putString("college", etCollege.getText().toString());
        editor.putString("ug", etUg.getText().toString());
        editor.putString("pg", etPg.getText().toString());
        editor.putString("phd", etPhd.getText().toString());
        editor.putString("achievements", etAchievements.getText().toString());

        // Commit the changes
        editor.apply();

        Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();
        finish(); // Go back to ProfileActivity
    }

    private void checkPermissionAndOpenGallery() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            // Older versions
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        if (ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            galleryPermissionLauncher.launch(permissions);
        }
    }

    private void openGallery() {
        // We use ACTION_OPEN_DOCUMENT to get a persistent URI
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*"); // This allows all image types
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        galleryLauncher.launch(intent);
    }
}