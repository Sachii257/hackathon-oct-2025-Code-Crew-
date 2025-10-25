package com.hackathon.nyaymitra.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.hackathon.nyaymitra.LoginActivity;
import com.hackathon.nyaymitra.R;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerLanguage;
    private TextView tvPrivacyPolicy;
    private SwitchMaterial switch2FA;
    private Button btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Find Views
        spinnerLanguage = findViewById(R.id.spinner_language);
        tvPrivacyPolicy = findViewById(R.id.tv_privacy_policy);
        switch2FA = findViewById(R.id.switch_2fa);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);

        // Setup Language Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);
        // TODO: Load saved language preference and set spinner selection
        // TODO: Add listener to save selected language

        // Setup Privacy Policy Click
        tvPrivacyPolicy.setOnClickListener(v -> {
            // TODO: Replace with your actual Privacy Policy URL
            String url = "https://www.example.com/privacy";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        // Setup 2FA Switch
        // TODO: Load saved 2FA preference
        switch2FA.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Save 2FA preference
            // TODO: Implement 2FA verification flow if isChecked is true
            Toast.makeText(this, "2FA " + (isChecked ? "Enabled" : "Disabled") + " (Mock)", Toast.LENGTH_SHORT).show();
        });

        // Setup Delete Account Button
        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.setting_delete_confirm_title)
                .setMessage(R.string.setting_delete_confirm_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    // TODO: Implement actual account deletion logic (API call)

                    Toast.makeText(this, "Account Deletion Requested (Mock)", Toast.LENGTH_SHORT).show();
                    // Navigate back to Login screen after deletion
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}