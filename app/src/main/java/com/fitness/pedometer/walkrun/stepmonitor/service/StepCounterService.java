package com.fitness.pedometer.walkrun.stepmonitor.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.activity.MainActivity;
import com.fitness.pedometer.walkrun.stepmonitor.model.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StepCounterService extends Service implements SensorEventListener {

    public static final String ACTION_STOP = "com.fitness.pedometer.walkrun.stepmonitor.action.STOP_STEP_SERVICE";

    private static final String CHANNEL_ID = "step_counter_channel";
    private static final int NOTIFICATION_ID = 1001;

    private static final double KCAL_PER_STEP = 0.04;
    private static final double KM_PER_STEP = 0.0008;

    // Batch saving configuration
    private static final long BATCH_SAVE_INTERVAL_MS = 30_000L; // Save every 30 seconds
    private static final long SHUTDOWN_TIMEOUT_MS = 2_000L; // Max 2 seconds for shutdown

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private DatabaseHelper databaseHelper;

    private int totalSensorSteps = 0;
    private int baselineToday = 0;
    private boolean baselineInitialized = false;

    private boolean foregroundStarted = false;

    // Batch saving variables
    private final AtomicInteger pendingSteps = new AtomicInteger(0);
    private volatile int lastSavedSteps = 0;
    private volatile int currentDisplaySteps = 0;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler batchHandler = new Handler(Looper.getMainLooper());

    private volatile boolean isServiceRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // CRITICAL FIX: Create notification channel FIRST before anything else
        createNotificationChannel();

        // CRITICAL FIX: Start foreground IMMEDIATELY in onCreate()
        // This must happen within 5-10 seconds of startForegroundService() call
        try {
            startForeground(NOTIFICATION_ID, buildNotification(0));
            foregroundStarted = true;
        } catch (Exception e) {
            // If we can't start foreground, stop the service immediately
            stopSelf();
            return;
        }

        // Now do other initialization
        databaseHelper = new DatabaseHelper(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        isServiceRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Handle stop action immediately
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            requestStop();
            return START_NOT_STICKY;
        }

        // Ensure we're in foreground (safety check)
        if (!foregroundStarted) {
            try {
                startForeground(NOTIFICATION_ID, buildNotification(0));
                foregroundStarted = true;
            } catch (Exception e) {
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        if (stepCounterSensor == null || sensorManager == null) {
            stopServiceGracefully();
            return START_NOT_STICKY;
        }

        // Register sensor listener
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Load initial data from database
        loadInitialStepCount();

        // Start batch saving
        startBatchSaving();

        return START_STICKY;
    }

    /**
     * Load initial step count from database on background thread
     */
    private void loadInitialStepCount() {
        ioExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DatabaseHelper.StepData todayData = databaseHelper.getTodayStepData();
                    if (todayData != null) {
                        lastSavedSteps = todayData.steps;
                        currentDisplaySteps = todayData.steps;

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateNotificationFast(currentDisplaySteps);
                            }
                        });
                    }
                } catch (Exception e) {
                    // Ignore errors
                }
            }
        });
    }

    /**
     * Start periodic batch saving to database
     */
    private void startBatchSaving() {
        batchHandler.removeCallbacks(batchSaveRunnable);
        batchHandler.postDelayed(batchSaveRunnable, BATCH_SAVE_INTERVAL_MS);
    }

    /**
     * Batch save runnable - saves accumulated steps periodically
     */
    private final Runnable batchSaveRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isServiceRunning) {
                return;
            }

            final int stepsToAdd = pendingSteps.getAndSet(0);

            if (stepsToAdd > 0) {
                ioExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            double calories = stepsToAdd * KCAL_PER_STEP;
                            double distance = stepsToAdd * KM_PER_STEP;
                            long time = estimateTimeMillis(stepsToAdd);

                            databaseHelper.addToToday(stepsToAdd, calories, distance, time);
                            lastSavedSteps += stepsToAdd;
                        } catch (Exception e) {
                            // Re-add steps if save failed
                            pendingSteps.addAndGet(stepsToAdd);
                        }
                    }
                });
            }

            // Schedule next batch save
            if (isServiceRunning) {
                batchHandler.postDelayed(this, BATCH_SAVE_INTERVAL_MS);
            }
        }
    };

    /**
     * Fast stop - no blocking operations
     */
    private void requestStop() {
        isServiceRunning = false;

        // 1. Cancel batch handler immediately
        batchHandler.removeCallbacks(batchSaveRunnable);

        // 2. Save final pending steps in background (fire and forget)
        saveFinalStepsAsync();

        // 3. Unregister sensor immediately
        if (sensorManager != null) {
            try {
                sensorManager.unregisterListener(this);
            } catch (Exception ignored) {}
            sensorManager = null;
        }

        // 4. Stop foreground immediately
        if (foregroundStarted) {
            try {
                stopForeground(true);
            } catch (Exception ignored) {}
            foregroundStarted = false;
        }

        // 5. Stop service
        stopSelf();
    }

    /**
     * Save final pending steps asynchronously (fire and forget)
     */
    private void saveFinalStepsAsync() {
        final int finalSteps = pendingSteps.getAndSet(0);
        if (finalSteps > 0) {
            ioExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        double calories = finalSteps * KCAL_PER_STEP;
                        double distance = finalSteps * KM_PER_STEP;
                        long time = estimateTimeMillis(finalSteps);

                        databaseHelper.addToToday(finalSteps, calories, distance, time);
                    } catch (Exception ignored) {
                        // Accept data loss on shutdown
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;

        // Cancel handlers immediately
        batchHandler.removeCallbacks(batchSaveRunnable);
        mainHandler.removeCallbacksAndMessages(null);

        // Unregister sensor immediately
        if (sensorManager != null) {
            try {
                sensorManager.unregisterListener(this);
            } catch (Exception ignored) {}
            sensorManager = null;
        }

        // Stop foreground immediately
        if (foregroundStarted) {
            try {
                stopForeground(true);
            } catch (Exception ignored) {}
            foregroundStarted = false;
        }

        // Try to save final data quickly (with timeout)
        final int finalSteps = pendingSteps.getAndSet(0);
        if (finalSteps > 0) {
            ioExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        double calories = finalSteps * KCAL_PER_STEP;
                        double distance = finalSteps * KM_PER_STEP;
                        long time = estimateTimeMillis(finalSteps);
                        databaseHelper.addToToday(finalSteps, calories, distance, time);
                    } catch (Exception ignored) {}
                }
            });
        }

        // Shutdown executor quickly (don't wait)
        try {
            ioExecutor.shutdown();
            // Wait briefly for tasks to complete
            if (!ioExecutor.awaitTermination(SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                ioExecutor.shutdownNow();
            }
        } catch (Exception ignored) {
            ioExecutor.shutdownNow();
        }

        // Close database
        if (databaseHelper != null) {
            try {
                databaseHelper.close();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Graceful service stop
     */
    private void stopServiceGracefully() {
        try {
            stopForeground(true);
        } catch (Exception ignored) {}
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Sensor event handler - lightweight, no blocking operations
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) {
            return;
        }

        totalSensorSteps = (int) event.values[0];

        // Initialize baseline on first sensor reading
        if (!baselineInitialized) {
            baselineToday = loadBaselineForToday();
            if (baselineToday == Integer.MIN_VALUE) {
                baselineToday = totalSensorSteps;
                saveBaselineForToday(baselineToday);
            }
            baselineInitialized = true;
        }

        // Calculate today's steps from sensor
        final int todayStepsFromSensor = Math.max(0, totalSensorSteps - baselineToday);

        // Calculate new steps since last save
        final int newSteps = Math.max(0, todayStepsFromSensor - currentDisplaySteps);

        if (newSteps > 0) {
            // Add to pending batch (thread-safe)
            pendingSteps.addAndGet(newSteps);

            // Update current display count
            currentDisplaySteps = todayStepsFromSensor;

            // Update notification immediately (fast operation)
            updateNotificationFast(currentDisplaySteps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action needed
    }

    /**
     * Update notification without blocking
     */
    private void updateNotificationFast(final int steps) {
        if (!foregroundStarted || !isServiceRunning) {
            return;
        }

        try {
            Notification notification = buildNotification(steps);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.notify(NOTIFICATION_ID, notification);
            }
        } catch (Exception ignored) {
            // Ignore notification errors
        }
    }

    private String getTodayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void saveBaselineForToday(int baseline) {
        try {
            getSharedPreferences("step_counter_baseline", MODE_PRIVATE)
                    .edit()
                    .putInt(getTodayKey(), baseline)
                    .apply();
        } catch (Exception ignored) {}
    }

    private int loadBaselineForToday() {
        try {
            return getSharedPreferences("step_counter_baseline", MODE_PRIVATE)
                    .getInt(getTodayKey(), Integer.MIN_VALUE);
        } catch (Exception e) {
            return Integer.MIN_VALUE;
        }
    }

    private long estimateTimeMillis(int steps) {
        return steps * 500L;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Step Counter";
            String description = "Counting your steps in background";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification(int todaySteps) {
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingOpen = PendingIntent.getActivity(
                this,
                0,
                openIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent stopIntent = new Intent(this, StepCounterService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStop = PendingIntent.getService(
                this,
                1,
                stopIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = getString(R.string.app_name);
        String text = "Today: " + todaySteps + " steps";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_steps)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingOpen)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", pendingStop);

        return builder.build();
    }
}