package com.fitness.pedometer.walkrun.stepmonitor.notiSpecial;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SharePreferenceUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class WakeService extends Service {
    private static final String CHANNEL_ID = "WakeServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private FileObserver downloadObserver;
    private final Set<String> notifiedFiles = new HashSet<>();
    private final Handler handler = new Handler();
    private FileObserver screenshotObserver;
    private ContentObserver screenshotContentObserver;
    private final Set<String> notifiedScreenshots = Collections.synchronizedSet(new HashSet<>());

    private NotificationUtil notificationUtil;

    @Override
    public void onCreate() {
        try {
            super.onCreate();
            createNotificationChannel();
        } catch (Exception e) {
            // Silently handle any exceptions
        }
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            notificationUtil = new NotificationUtil();
            Notification notification = buildNotification();
            if (Build.VERSION.SDK_INT >= 29) {
                if (Build.VERSION.SDK_INT >= 34) {
                    startForeground(NOTIFICATION_ID, notification);
                } else {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
                }
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
            handler.postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    // Android 15+
                    Log.d("ProWakeService", "Auto-stop after timeout (Android 15+)");
                    try {
                        stopForeground(STOP_FOREGROUND_REMOVE);
                    } catch (Exception ignore) {
                    }
                    stopSelf();
                }
            }, 10 * 60 * 1000);
            // Khởi tạo FileObserver cho thư mục Download
            if (downloadObserver == null) {
                String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                downloadObserver = new FileObserver(downloadPath, FileObserver.CREATE | FileObserver.CLOSE_WRITE) {
                    @Override
                    public void onEvent(int event, String path) {
                        if (event == FileObserver.CLOSE_WRITE && path != null) {
                            String realFileName;
                            if (path.startsWith(".pending-")) {
                                int index = path.indexOf('-', 9);
                                if (index != -1 && index + 1 < path.length()) {
                                    realFileName = path.substring(index + 1);
                                } else {
                                    realFileName = path;
                                }
                            } else {
                                realFileName = path;
                            }

                            // Đường dẫn đầy đủ đến file
                            String fullFilePath = downloadPath + "/" + realFileName;

                            if (realFileName.endsWith(".doc") || realFileName.endsWith(".docx")
                                    || realFileName.endsWith(".ppt") || realFileName.endsWith(".pptx")
                                    || realFileName.endsWith(".pdf")
                                    || realFileName.endsWith(".xls") || realFileName.endsWith(".xlsx")
                                    || realFileName.endsWith(".xlsm") || realFileName.endsWith(".csv")) {

                                synchronized (notifiedFiles) {
                                    if (!notifiedFiles.contains(fullFilePath)) {
                                        notifiedFiles.add(fullFilePath);
                                        notificationUtil.showDownloadNotification(getApplicationContext(), fullFilePath);
                                        handler.postDelayed(() -> {
                                            synchronized (notifiedFiles) {
                                                notifiedFiles.remove(fullFilePath);
                                            }
                                        }, 5000);
                                    }
                                }
                            }
                        }
                    }
                };
                downloadObserver.startWatching();
            }
            // Khởi tạo FileObserver cho thư mục chup man hinh
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // Android 9 trở xuống: FileObserver
                String screenshotPath = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .getPath() + "/Screenshots";

                screenshotObserver = new FileObserver(screenshotPath, FileObserver.CREATE | FileObserver.CLOSE_WRITE) {
                    @Override
                    public void onEvent(int event, String path) {
                        if (event == FileObserver.CLOSE_WRITE && path != null &&
                                (path.endsWith(".png") || path.endsWith(".jpg"))) {

                            String fullPath = screenshotPath + "/" + path;

                            synchronized (notifiedScreenshots) {
                                if (!notifiedScreenshots.contains(fullPath)) {
                                    notifiedScreenshots.add(fullPath);
                                    Log.d("ScreenshotObserver", "Screenshot detected: " + fullPath);

                                    // Đọc bitmap từ path
                                    Bitmap screenshotBitmap = BitmapFactory.decodeFile(fullPath);
                                    if (screenshotBitmap != null) {
                                        notificationUtil.showScreenShotNotification(getApplicationContext(), screenshotBitmap);
                                    } else {
                                        Log.w("ScreenshotObserver", "Không đọc được bitmap từ: " + fullPath);
                                    }

                                    // Xóa sau 5 giây để tránh spam
                                    handler.postDelayed(() -> {
                                        synchronized (notifiedScreenshots) {
                                            notifiedScreenshots.remove(fullPath);
                                        }
                                    }, 5000);
                                }
                            }
                        }
                    }

                };
                screenshotObserver.startWatching();
                Log.d("ScreenshotObserver", "FileObserver started for: " + screenshotPath);

            } else {
                // Android 10 trở lên: ContentObserver
                screenshotContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        if (uri == null) return;
                        String uriStr = uri.toString();
                        if (!uriStr.startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/")) {
                            Log.w("ScreenshotObserver", "Bỏ qua URI không hợp lệ: " + uriStr);
                            showNotificationScreenShotDefault();
                            return;
                        }
                        handleScreenshotUri(uri, 0); // r
                    }

                };

                getContentResolver().registerContentObserver(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        true,
                        screenshotContentObserver
                );

                Log.d("ScreenshotObserver", "ContentObserver registered for MediaStore");
            }


        } catch (Exception e) {
            // Silently handle any exceptions
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void showNotificationScreenShotDefault() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_app);
        notificationUtil.showScreenShotNotification(this, bitmap);
    }

    private void handleScreenshotUri(Uri uri, int retryCount) {
        if (uri == null
                || !"content".equals(uri.getScheme())
                || uri.getAuthority() == null
                || !uri.getAuthority().contains("media")) {
            Log.w("WakeService", "Invalid screenshot URI: " + uri);
            showNotificationScreenShotDefault();
            return;
        }

        try (Cursor cursor = getContentResolver().query(
                uri,
                new String[]{
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.RELATIVE_PATH,
                        MediaStore.Images.Media.IS_PENDING
                },
                null,
                null,
                null
        )) {
            if (cursor == null || !cursor.moveToFirst()) return;

            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            String relativePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH));
            int isPending = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.IS_PENDING));

            if (isPending == 1 && retryCount < 3) {
                handler.postDelayed(() -> handleScreenshotUri(uri, retryCount + 1), 500);
                return;
            }

            if (relativePath != null && relativePath.contains("Screenshots")) {
                synchronized (notifiedScreenshots) {
                    if (notifiedScreenshots.contains(name)) return;
                    notifiedScreenshots.add(name);

                    Bitmap bitmap = null;

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        try (Cursor pathCursor = getContentResolver().query(
                                uri,
                                new String[]{MediaStore.Images.Media.DATA},
                                null,
                                null,
                                null
                        )) {
                            if (pathCursor != null && pathCursor.moveToFirst()) {
                                int colIndex = pathCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                                if (colIndex != -1) {
                                    String filePath = pathCursor.getString(colIndex);
                                    bitmap = BitmapFactory.decodeFile(filePath);
                                }
                            }
                        } catch (Exception ignore) {
                        }
                    }

                    if (bitmap == null) {
                        try (InputStream is = getContentResolver().openInputStream(uri)) {
                            if (is != null) {
                                bitmap = BitmapFactory.decodeStream(is);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (bitmap != null) {
                        notificationUtil.showScreenShotNotification(getApplicationContext(), bitmap);
                    }

                    // Xóa sau 5 giây để tránh spam
                    handler.postDelayed(() -> {
                        synchronized (notifiedScreenshots) {
                            notifiedScreenshots.remove(name);
                        }
                    }, 5000);
                }
            }
        } catch (SecurityException se) {
            Log.e("WakeService", "No permission to read URI: " + uri, se);
        } catch (Exception e) {
            Log.e("WakeService", "Error handling screenshot URI: " + uri, e);
        }
    }


    @Override
    public void onDestroy() {
        try {
            if (downloadObserver != null) {
                downloadObserver.stopWatching();
                downloadObserver = null;
            }
            if (screenshotObserver != null) {
                screenshotObserver.stopWatching();
                screenshotObserver = null;
            }
            if (screenshotContentObserver != null) {
                getContentResolver().unregisterContentObserver(screenshotContentObserver);
                screenshotContentObserver = null;
            }
            super.onDestroy();
        } catch (Exception e) {
            // Silently handle any exceptions
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel serviceChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "Wake Service Channel",
                        NotificationManager.IMPORTANCE_LOW
                );
                serviceChannel.setShowBadge(false);
                serviceChannel.enableLights(false);
                serviceChannel.enableVibration(false);
                serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

                NotificationManagerCompat manager = NotificationManagerCompat.from(this);
                manager.createNotificationChannel(serviceChannel);
            }
        } catch (Exception e) {
            // Silently handle any exceptions
        }
    }

    private Notification buildNotification() {
        try {
            Intent openAppIntent = new Intent(getApplicationContext(), SplashNotiActivity.class);
            openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent openAppPendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            @SuppressLint("RemoteViewLayout")
            RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification_service_no_ic);
            @SuppressLint("RemoteViewLayout")
            RemoteViews notificationLayoutExpand = new RemoteViews(getPackageName(), R.layout.custom_notification_service);

            return new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(openAppPendingIntent)
                    .setCustomContentView(notificationLayout)
                    .setCustomBigContentView(notificationLayoutExpand)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setSilent(true)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    .build();
        } catch (Exception e) {
            // Return a minimal notification if there's an error
            return new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("")
                    .setContentText("")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSilent(true)
                    .build();
        }
    }

    @Override
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    public void onTimeout(int foregroundServiceType, int reason) {
        try {
            Log.e("WakeService", "Foreground service timeout on Android 15. Stopping service.");
            if (downloadObserver != null) {
                downloadObserver.stopWatching();
                downloadObserver = null;
            }
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            Log.e("WakeService", "onTimeout error: " + e.getMessage(), e);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        SharedPreferences prefs = getSharedPreferences("noti_prefs", MODE_PRIVATE);
        long lastTime = prefs.getLong("last_removed_time", 0);
        long now = System.currentTimeMillis();

        if ( now - lastTime >= 15 * 60 * 1000 && SharePreferenceUtils.isOrganicNoti(getApplicationContext())) {
            try {
                FirebaseAnalytics.getInstance(getApplicationContext())
                        .logEvent("event_show_noti_close_app_new", null);
                notificationUtil.showClosedAppNotification(getApplicationContext());
                prefs.edit().putLong("last_removed_time", now).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

