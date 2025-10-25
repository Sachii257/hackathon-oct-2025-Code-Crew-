package com.hackathon.nyaymitra.activities;

import android.Manifest; // <-- Make sure this is imported
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager; // <-- Make sure this is imported
import android.net.Uri;
import android.os.Build; // <-- Make sure this is imported
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // <-- Make sure this is imported
import androidx.activity.result.contract.ActivityResultContracts; // <-- Make sure this is imported
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat; // <-- Make sure this is imported
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.imageview.ShapeableImageView;
import com.hackathon.nyaymitra.LoginActivity;
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.fragments.AiAssistantFragment;
import com.hackathon.nyaymitra.fragments.CommunicationFragment;
import com.hackathon.nyaymitra.fragments.DictionaryFragment;
import com.hackathon.nyaymitra.fragments.HomeFragment;
import com.hackathon.nyaymitra.fragments.LawsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.hackathon.nyaymitra.utils.NotificationHelper; // <-- Make sure this is imported

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Launcher for Notification Permission
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // getSupportActionBar().setTitle("Nyay Mitra");

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setSelected(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        bottomNav.setOnItemSelectedListener(bottomNavListener);

        // Create Channel & Request Permission
        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermission();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        updateNavHeader();
    }

    // Method to request notification permission on Android 13+
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateNavHeader();
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        ShapeableImageView ivNavProfilePic = headerView.findViewById(R.id.iv_nav_profile_pic);
        TextView tvNavEmail = headerView.findViewById(R.id.tv_nav_email);

        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String email = prefs.getString("email", "your.email@example.com");
        String imageUriString = prefs.getString("profileImageUri", null);

        tvNavEmail.setText(email);

        if (imageUriString != null) {
            ivNavProfilePic.setImageURI(Uri.parse(imageUriString));
        } else {
            ivNavProfilePic.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            // --- TEMPORARY: Add a test notification ---
            NotificationHelper.showNotification(this, "Test Notification", "This is a test message triggered from MainActivity.", 999);
            // ------------------------------------------

            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final NavigationBarView.OnItemSelectedListener bottomNavListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_home) {
                        selectedFragment = new HomeFragment();
                    } else if (itemId == R.id.nav_chat) {
                        selectedFragment = new LawsFragment();
                    } else if (itemId == R.id.nav_communication) {
                        selectedFragment = new CommunicationFragment();
                    } else if (itemId == R.id.nav_dictionary) {
                        selectedFragment = new DictionaryFragment();
                    } else if (itemId == R.id.nav_ai_assistant) {
                        selectedFragment = new AiAssistantFragment();
                    }

                    if (selectedFragment != null) {
                        loadFragment(selectedFragment);
                        return true;
                    }
                    return false;
                }
            };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.drawer_profile || itemId == R.id.drawer_settings ||
                itemId == R.id.drawer_help || itemId == R.id.drawer_faq ||
                itemId == R.id.drawer_logout)
        {
            startActivity(new Intent(this, ProfileActivity.class));
            if (itemId == R.id.drawer_settings) Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show();
            else if (itemId == R.id.drawer_help) Toast.makeText(this, "Opening Help...", Toast.LENGTH_SHORT).show();
            else if (itemId == R.id.drawer_faq) Toast.makeText(this, "Opening FAQs...", Toast.LENGTH_SHORT).show();
            else if (itemId == R.id.drawer_logout) Toast.makeText(this, "Opening Logout...", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}