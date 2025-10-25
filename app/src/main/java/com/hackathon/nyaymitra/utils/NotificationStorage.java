package com.hackathon.nyaymitra.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hackathon.nyaymitra.models.NotificationItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationStorage {

    private static final String PREFS_NAME = "NotificationHistory";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final int MAX_NOTIFICATIONS = 50; // Limit history size

    // Save a new notification
    public static void addNotification(Context context, NotificationItem newItem) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        List<NotificationItem> notifications = getNotifications(context); // Get current list

        notifications.add(0, newItem); // Add new item at the beginning

        // Limit the list size
        if (notifications.size() > MAX_NOTIFICATIONS) {
            notifications = notifications.subList(0, MAX_NOTIFICATIONS);
        }

        // Save the updated list back to SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(notifications);
        editor.putString(KEY_NOTIFICATIONS, json);
        editor.apply();
    }

    // Retrieve all saved notifications (newest first)
    public static List<NotificationItem> getNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_NOTIFICATIONS, null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<NotificationItem>>() {}.getType();

        List<NotificationItem> notifications = gson.fromJson(json, type);

        if (notifications == null) {
            return new ArrayList<>(); // Return empty list if nothing saved
        }

        // Ensure list is sorted by timestamp descending (newest first)
        Collections.sort(notifications, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

        return notifications;
    }

    // Optional: Clear all notifications
    public static void clearNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_NOTIFICATIONS);
        editor.apply();
    }
}