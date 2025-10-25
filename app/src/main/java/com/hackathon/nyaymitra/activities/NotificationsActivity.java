package com.hackathon.nyaymitra.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
// Toolbar import is removed
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.adapters.NotificationAdapter;
import com.hackathon.nyaymitra.models.NotificationItem;
import com.hackathon.nyaymitra.utils.NotificationStorage;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // --- Toolbar setup REMOVED ---
        // Toolbar toolbar = findViewById(R.id.toolbar_notifications);
        // setSupportActionBar(toolbar); // <-- THIS LINE IS REMOVED

        // Setup the Up button (back arrow) on the theme's action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // No need for toolbar.setNavigationOnClickListener(...) anymore

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_view_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load notifications from storage
        notificationList = NotificationStorage.getNotifications(this);

        // Initialize and set adapter
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // TODO: Add a TextView for "No notifications yet" visibility handling
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationList = NotificationStorage.getNotifications(this);
        if (adapter != null) {
            adapter.updateData(notificationList);
        }
    }

    // Handles the Up button press on the theme's action bar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go back to the previous activity
        return true;
    }
}