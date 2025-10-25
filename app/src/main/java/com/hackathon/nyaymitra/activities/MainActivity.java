package com.hackathon.nyaymitra.activities;

// --- Merged Imports ---
import android.Manifest; // Kept from HEAD
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences; // Kept for updateNavHeader
import android.content.pm.PackageManager; // Kept from HEAD
import android.net.Uri; // Kept for updateNavHeader
import android.os.Build; // Kept from HEAD
import android.os.Bundle;
import android.os.Handler; // Added for completeness, might be needed if background tasks update UI
import android.os.Looper; // Added for completeness
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View; // Kept from HEAD
// Removed duplicate ImageView import
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // Kept from HEAD
import androidx.activity.result.contract.ActivityResultContracts; // Kept from HEAD
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat; // Kept from HEAD
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

// --- Material Design Imports ---
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView; // Kept from HEAD
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
// -----------------------------

// --- App Specific Imports ---
import com.hackathon.nyaymitra.LoginActivity; // Kept from HEAD
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.fragments.AiAssistantFragment;
import com.hackathon.nyaymitra.fragments.CommunicationFragment;
import com.hackathon.nyaymitra.fragments.DictionaryFragment;
import com.hackathon.nyaymitra.fragments.HomeFragment;
import com.hackathon.nyaymitra.fragments.LawsFragment;
import com.hackathon.nyaymitra.utils.NotificationHelper; // Kept from HEAD
// --------------------------

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView; // Kept from HEAD (as class member)

    // Launcher for Notification Permission (Kept from HEAD)
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
        // Kept logic from main to disable default title and enable marquee
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) { // Added null check for safety
            toolbarTitle.setSelected(true);
        }
        // -----------------------------------------------------------------

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view); // Initialized class member
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        bottomNav.setOnItemSelectedListener(bottomNavListener);

        // --- Kept Notification Setup from HEAD ---
        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermission();
        // ----------------------------------------

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // --- Kept Header Update from HEAD ---
        updateNavHeader();
        // -----------------------------------
    }

    // --- Kept Notification Permission Request Method from HEAD ---
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Check if the permission rationale should be shown (optional but good practice)
                // if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) { ... }
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            // else { Permission already granted }
        }
        // else { Not needed for older versions }
    }
    // ----------------------------------------------------------

    // --- Kept onResume and updateNavHeader Methods from HEAD ---
    @Override
    protected void onResume() {
        super.onResume();
        updateNavHeader(); // Update header when activity resumes
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        // Use ShapeableImageView ID from header layout
        ShapeableImageView ivNavProfilePic = headerView.findViewById(R.id.iv_nav_profile_pic);
        TextView tvNavEmail = headerView.findViewById(R.id.tv_nav_email); // Use TextView ID from header layout

        // Basic null checks for safety
        if (ivNavProfilePic == null || tvNavEmail == null) {
            Log.e("MainActivity", "Navigation header views not found!");
            return;
        }

        // Use SharedPreferences to get saved user data (example)
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String email = prefs.getString("email", "your.email@example.com"); // Provide default
        String imageUriString = prefs.getString("profileImageUri", null);

        tvNavEmail.setText(email);

        if (imageUriString != null) {
            try {
                ivNavProfilePic.setImageURI(Uri.parse(imageUriString));
            } catch (Exception e) {
                Log.e("MainActivity", "Error setting profile image URI: " + imageUriString, e);
                ivNavProfilePic.setImageResource(R.mipmap.ic_launcher_round); // Fallback
            }
        } else {
            // Set default image if no URI is saved
            ivNavProfilePic.setImageResource(R.mipmap.ic_launcher_round);
        }
    }
    // -----------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            // --- Kept Notification Test & Activity Start from HEAD ---
            // You might want to remove this test line later
            NotificationHelper.showNotification(this, "Test Notification", "This is a test message triggered from MainActivity.", 999);

            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
            // -------------------------------------------------------
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Kept Bottom Navigation Listener ---
    private final NavigationBarView.OnItemSelectedListener bottomNavListener =
            item -> { // Using lambda expression for brevity
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_chat) { // Assuming nav_chat maps to LawsFragment
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
            };
    // --------------------------------------

    // --- Kept Drawer Navigation Listener (Merged logic) ---
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Kept logic from HEAD to start ProfileActivity for all these items
        if (itemId == R.id.drawer_profile || itemId == R.id.drawer_settings ||
                itemId == R.id.drawer_help || itemId == R.id.drawer_faq ||
                itemId == R.id.drawer_logout)
        {
            // Start ProfileActivity - you can pass an extra to tell ProfileActivity which item was clicked
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            profileIntent.putExtra("DRAWER_ITEM_ID", itemId); // Pass the clicked item ID
            startActivity(profileIntent);

            // Optional: Keep Toasts for immediate feedback
            if (itemId == R.id.drawer_settings) Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show();
            else if (itemId == R.id.drawer_help) Toast.makeText(this, "Opening Help...", Toast.LENGTH_SHORT).show();
            else if (itemId == R.id.drawer_faq) Toast.makeText(this, "Opening FAQs...", Toast.LENGTH_SHORT).show();
            else if (itemId == R.id.drawer_logout) {
                Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                // TODO: Add actual logout logic here (e.g., Firebase sign out, clear prefs, navigate to LoginActivity)
            }

        }
        // Handle other drawer items if needed

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    // ----------------------------------------------------

    // --- Kept loadFragment Method ---
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        // Optional: Add to back stack if you want navigation between fragments
        // transaction.addToBackStack(null);
        transaction.commit();
    }
    // ----------------------------

    // --- Kept onBackPressed Method ---
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Check if there are fragments in the back stack before calling super.onBackPressed()
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }
    // ---------------------------
}