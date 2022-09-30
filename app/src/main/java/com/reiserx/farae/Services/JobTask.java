package com.reiserx.farae.Services;


import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class JobTask extends JobService {
    private static final String TAG = "JobStarted";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "started");

        if (!isMyServiceRunning(JobTask.this)) {
            Intent in = new Intent(JobTask.this, MessageService.class);
            startService(in);
        }
        return true;
    }
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i(TAG, "Stopping job");
        return true;
    }
    private boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MessageService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
