package com.hackathon.nyaymitra.activities;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.hackathon.nyaymitra.R;

public class LawyerContentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lawyer_content);

        // Set a title for the action bar
        getSupportActionBar().setTitle("For Lawyers");

        TextView tvLawyerContent = findViewById(R.id.tv_lawyer_content);
        String lawyerHtml = getString(R.string.lawyer_content);

        // This is the code from your old fragment to parse the HTML
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvLawyerContent.setText(Html.fromHtml(lawyerHtml, Html.FROM_HTML_MODE_COMPACT));
        } else {
            tvLawyerContent.setText(Html.fromHtml(lawyerHtml));
        }
    }
}