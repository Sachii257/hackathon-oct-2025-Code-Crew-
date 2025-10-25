package com.hackathon.nyaymitra.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.hackathon.nyaymitra.utils.NotificationHelper; // We will create this next

public class DailyTipWorker extends Worker {

    public DailyTipWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // --- TODO: Get a random legal tip ---
        // You could store tips in strings.xml array or a simple database
        String tipTitle = "Legal Tip of the Day";
        String tipContent = "Did you know you have the right to remain silent if questioned by police?";
        // ------------------------------------

        // Send the notification using our helper
        NotificationHelper.showNotification(context, tipTitle, tipContent, 101); // Use a unique ID

        // Indicate success
        return Result.success();
    }
}