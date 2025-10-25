package com.hackathon.nyaymitra.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.fragments.AiAssistantFragment;
import com.hackathon.nyaymitra.fragments.CommunicationFragment;
import com.hackathon.nyaymitra.fragments.DictionaryFragment;
import com.hackathon.nyaymitra.fragments.HomeFragment;
import com.hackathon.nyaymitra.fragments.LawsFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // *** THIS ENABLES THE MARQUEE ***
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setSelected(true);
        // *********************************

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        bottomNav.setOnItemSelectedListener(bottomNavListener);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
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
            Toast.makeText(this, "Notifications clicked!", Toast.LENGTH_SHORT).show();
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

        if (itemId == R.id.drawer_profile) {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.drawer_settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.drawer_help) {
            Toast.makeText(this, "Help clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.drawer_faq) {
            Toast.makeText(this, "FAQ clicked", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.drawer_logout) {
            Toast.makeText(this, "Log Out clicked", Toast.LENGTH_SHORT).show();
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
