package com.hackathon.nyaymitra.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.activities.NotificationsActivity; // Changed target activity
import com.hackathon.nyaymitra.models.NotificationItem; // Import Model

public class NotificationHelper {

    private static final String CHANNEL_ID = "nyay_mitra_channel";
    private static final String CHANNEL_NAME = "Nyay Mitra Notifications";
    private static final String CHANNEL_DESC = "General notifications for Nyay Mitra app";

    // Call this once, e.g., in SplashActivity or MainActivity's onCreate
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(Context context, String title, String content, int notificationId) {
        // --- CHECK PERMISSION ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return; // Permission denied, do nothing
            }
        }
        // -----------------------

        // Intent to open NotificationsActivity when notification is tapped
        Intent intent = new Intent(context, NotificationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Use flags to avoid multiple instances
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE); // Use unique request code

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications) // Ensure this drawable exists
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Set the tap action
                .setAutoCancel(true); // Removes notification on tap

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            // Check permission again right before notifying (belt-and-suspenders approach)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(notificationId, builder.build());

            // *** SAVE NOTIFICATION TO HISTORY ***
            NotificationItem item = new NotificationItem(title, content, System.currentTimeMillis());
            NotificationStorage.addNotification(context, item);
            // **********************************

        } catch (SecurityException e) {
            // This might happen if permission was revoked after the check
            e.printStackTrace();
        }
    }
}