package com.hackathon.nyaymitra.activities;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hackathon.nyaymitra.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_help);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Find all TextViews
        TextView tvGettingStarted = findViewById(R.id.tv_help_getting_started);
        TextView tvScanning = findViewById(R.id.tv_help_scanning);
        TextView tvDictionary = findViewById(R.id.tv_help_dictionary);
        TextView tvAiDoubts = findViewById(R.id.tv_help_ai_doubts);
        TextView tvRights = findViewById(R.id.tv_help_rights);
        TextView tvAccount = findViewById(R.id.tv_help_account);
        TextView tvPrivacy = findViewById(R.id.tv_help_privacy);
        TextView tvTroubleshooting = findViewById(R.id.tv_help_troubleshooting);
        TextView tvContact = findViewById(R.id.tv_help_contact);

        // Set text using Html.fromHtml to render basic tags like <b>
        setTextWithHtml(tvGettingStarted, R.string.help_getting_started_content);
        setTextWithHtml(tvScanning, R.string.help_scanning_content);
        setTextWithHtml(tvDictionary, R.string.help_dictionary_content);
        setTextWithHtml(tvAiDoubts, R.string.help_ai_doubts_content);
        setTextWithHtml(tvRights, R.string.help_rights_content);
        setTextWithHtml(tvAccount, R.string.help_account_content);
        setTextWithHtml(tvPrivacy, R.string.help_privacy_content);
        setTextWithHtml(tvTroubleshooting, R.string.help_troubleshooting_content);
        setTextWithHtml(tvContact, R.string.help_contact_content);
    }

    // Helper method to set text with HTML parsing
    private void setTextWithHtml(TextView textView, int stringResId) {
        String htmlString = getString(stringResId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(htmlString, Html.FROM_HTML_MODE_COMPACT));
        } else {
            textView.setText(Html.fromHtml(htmlString));
        }
    }
}