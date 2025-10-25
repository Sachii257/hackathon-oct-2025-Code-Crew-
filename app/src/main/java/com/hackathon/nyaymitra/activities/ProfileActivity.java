package com.hackathon.nyaymitra.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast; // Keep this import, it might be used elsewhere or later

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hackathon.nyaymitra.LoginActivity;
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.adapters.ProfileAdapter;

public class ProfileActivity extends AppCompatActivity implements ProfileAdapter.OnOptionClickListener {

    // Options displayed in the list
    private final String[] options = {
            "Update Profile", "Settings", "Theme", "Notification Permission", "Help", "FAQs", "Log Out"
    };

    // Corresponding icons for each option
    private final int[] icons = {
            R.drawable.ic_profile, R.drawable.ic_settings, R.drawable.ic_theme,
            R.drawable.ic_notifications, R.drawable.ic_help, R.drawable.ic_faq, R.drawable.ic_logout
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Ensures back arrow shows
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Handle back press

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view_profile);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ProfileAdapter adapter = new ProfileAdapter(options, icons, this); // 'this' implements the click listener
        recyclerView.setAdapter(adapter);
    }

    // --- THIS IS THE UPDATED onOptionClick METHOD ---
    @Override
    public void onOptionClick(String option) {
        switch (option) {
            case "Update Profile":
                startActivity(new Intent(this, UpdateProfileActivity.class));
                break;
            case "Settings":
                // *** LAUNCH SETTINGS ACTIVITY ***
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case "Theme":
                showThemeDialog();
                break;
            case "Notification Permission":
                openNotificationSettings();
                break;
            case "Help":
                // *** LAUNCH HELP ACTIVITY ***
                startActivity(new Intent(this, HelpActivity.class));
                break;
            case "FAQs":
                // *** LAUNCH FAQ ACTIVITY ***
                startActivity(new Intent(this, FaqActivity.class));
                break;
            case "Log Out":
                showLogoutDialog();
                break;
        }
    }
    // --- END OF UPDATED METHOD ---

    // Method to show the theme selection dialog
    private void showThemeDialog() {
        String[] themes = {"Light", "Dark", "System Default"};
        // TODO: Get current theme to pre-select the radio button
        new AlertDialog.Builder(this)
                .setTitle("Choose Theme")
                .setItems(themes, (dialog, which) -> {
                    switch (which) {
                        case 0: // Light
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case 1: // Dark
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                        case 2: // System
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;
                    }
                    // Optional: recreate(); // Apply theme immediately, might cause flicker
                })
                .show();
    }

    // Method to open the app's notification settings in the phone settings
    private void openNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }

    // Method to show the logout confirmation dialog
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    // TODO: Add any session clearing logic here (e.g., SharedPreferences.clear())
                    // Example: getSharedPreferences("UserProfile", MODE_PRIVATE).edit().clear().apply();

                    // Navigate back to LoginActivity and clear the back stack
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Close ProfileActivity
                })
                .setNegativeButton("Cancel", null) // Just dismisses the dialog
                .show();
    }
}