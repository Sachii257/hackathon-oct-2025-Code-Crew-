package com.hackathon.nyaymitra.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationItem {
    private String title;
    private String content;
    private long timestamp; // Store time as milliseconds

    // Constructor needed for Gson deserialization
    public NotificationItem() {}

    public NotificationItem(String title, String content, long timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }

    // Helper to format timestamp
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}