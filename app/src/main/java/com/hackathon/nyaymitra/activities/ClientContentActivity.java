package com.hackathon.nyaymitra.activities;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.hackathon.nyaymitra.R;

public class ClientContentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_content);

        // Set a title for the action bar
        getSupportActionBar().setTitle("For Clients");

        TextView tvClientContent = findViewById(R.id.tv_client_content);
        String clientHtml = getString(R.string.client_content);

        // This is the code from your old fragment to parse the HTML
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvClientContent.setText(Html.fromHtml(clientHtml, Html.FROM_HTML_MODE_COMPACT));
        } else {
            tvClientContent.setText(Html.fromHtml(clientHtml));
        }
    }
}