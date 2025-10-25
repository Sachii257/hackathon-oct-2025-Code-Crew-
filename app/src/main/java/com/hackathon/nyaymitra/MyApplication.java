package com.hackathon.nyaymitra;

import android.app.Application;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.hackathon.nyaymitra.workers.DailyTipWorker; // We will create this next
import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleDailyTipWorker();
    }

    private void scheduleDailyTipWorker() {
        // Constraints (optional, e.g., require network)
        Constraints constraints = new Constraints.Builder()
                // .setRequiredNetworkType(NetworkType.CONNECTED) // Example
                .build();

        // Create a periodic request to run once a day
        PeriodicWorkRequest dailyTipRequest =
                new PeriodicWorkRequest.Builder(DailyTipWorker.class, 1, TimeUnit.DAYS)
                        .setConstraints(constraints)
                        .build();

        // Enqueue the work, replacing any existing work with the same name
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailyTipWork",
                ExistingPeriodicWorkPolicy.REPLACE, // Or KEEP if you don't want it replaced
                dailyTipRequest);
    }
}