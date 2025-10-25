package com.hackathon.nyaymitra.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.hackathon.nyaymitra.LoginActivity; // <-- Make sure this import is correct
import com.hackathon.nyaymitra.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                // *** THIS IS THE CORRECTED LINE ***
                // It now opens LoginActivity instead of MainActivity
                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(mainIntent);

                // Close this activity
                finish();
            }
        }, SPLASH_DURATION);
    }
}