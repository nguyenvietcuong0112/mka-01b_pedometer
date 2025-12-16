package com.fitness.pedometer.walkrun.stepmonitor.notiSpecial;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SystemUtil;

import java.util.Calendar;
import java.util.Locale;

public class UnlockNotifier {
    private static final String CHANNEL_ID = "UnlockChannelV3";

    public static void handleUserPresent(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("noti_prefs", Context.MODE_PRIVATE);
        long lastTime = prefs.getLong("last_notification_time", 0);
        long now = System.currentTimeMillis();

        long thirtyMinutes = 30 * 60 * 1000L;
//        long thirtyMinutes = 0;
        if (now - lastTime < thirtyMinutes) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // 0–23

        if (hour >= 7 && hour < 19) {
            FirebaseAnalytics.getInstance(context)
                    .logEvent("event_show_noti_day_new", null);
            showDayNotification(context);
        } else {
            FirebaseAnalytics.getInstance(context)
                    .logEvent("event_show_noti_night_new", null);
            showNightNotification(context);
        }

        // Lưu lại thời gian lần cuối
        prefs.edit().putLong("last_notification_time", now).apply();
    }


    private static void showNightNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        String CHANNEL_ID = "DownloadFileObserverChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Download File Observer Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for file observer download notifications");
            notificationManager.createNotificationChannel(channel);
        }
        Intent openFileIntent = new Intent(context, SplashNotiActivity.class);
        openFileIntent.putExtra("noti_type_special_v2", true);
        openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openFileIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        int[] images = {
                R.drawable.girl1, R.drawable.girl2,
                R.drawable.girl3, R.drawable.girl4,
                R.drawable.girl5, R.drawable.girl6,
                R.drawable.girl7, R.drawable.girl8,
                R.drawable.girl9, R.drawable.girl10,
                R.drawable.girl11, R.drawable.girl12,
                R.drawable.girl13, R.drawable.girl14, R.drawable.girl15};

        int[] smallIcon = {
                R.drawable.icnight1, R.drawable.icnight2,
                R.drawable.icnight3, R.drawable.icnight4,
                R.drawable.icnight5, R.drawable.icnight6,
                R.drawable.icnight7, R.drawable.icnight8,
                R.drawable.icnight9, R.drawable.icnight10,
                R.drawable.icnight11, R.drawable.icnight12,
                R.drawable.icnight13, R.drawable.icnight14, R.drawable.icnight15};

        String[] titles = getLocalizedContext(context).getResources().getStringArray(R.array.special_titles_v2);
        String[] messages = getLocalizedContext(context).getResources().getStringArray(R.array.special_messages_v2);
        int randomIndex = new java.util.Random().nextInt(messages.length);
        RemoteViews customView = createNotificationDayNight(context, R.layout.noti_custom_default, R.id.notification_root, pendingIntent, titles[randomIndex], messages[randomIndex], images[randomIndex]);
        RemoteViews customViewHeader = createNotificationDayNight(context, R.layout.noti_custom_header, R.id.notification_root, pendingIntent, titles[randomIndex], messages[randomIndex], images[randomIndex]);
        RemoteViews expandedView = createNotificationDayNight(context, R.layout.noti_custom_expand, R.id.notification_root_expanded, pendingIntent, titles[randomIndex], messages[randomIndex], images[randomIndex]);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIcon[randomIndex])
                .setContentIntent(pendingIntent)
                .setCustomContentView(customView)
                .setCustomHeadsUpContentView(customViewHeader)
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }

    public static RemoteViews createNotificationDayNight(Context context, int layoutId, int rootViewId, PendingIntent pendingIntent, String title, String message, int srcId) {
        RemoteViews view = new RemoteViews(context.getPackageName(), layoutId);

        int padding = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                ? (int) (20 * context.getResources().getDisplayMetrics().density) : 0;
        view.setViewPadding(rootViewId, padding, padding, padding, padding);

        view.setOnClickPendingIntent(R.id.notification_button, pendingIntent);
        view.setTextViewText(R.id.notification_title_bold, title);
        view.setTextViewText(R.id.notification_title, message);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), srcId);
        view.setImageViewBitmap(R.id.notification_image, bitmap);
        view.setImageViewBitmap(R.id.notification_middle_image, bitmap);
        return view;
    }

    public static Context getLocalizedContext(Context context) {
        String lang = SystemUtil.getPreLanguage(context);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    private static void showDayNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        String CHANNEL_ID = "DownloadFileObserverChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Download File Observer Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for file observer download notifications");
            notificationManager.createNotificationChannel(channel);
        }
        Intent openFileIntent = new Intent(context, SplashNotiActivity.class);
        openFileIntent.putExtra("noti_type_special_v2", true);
        openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openFileIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        int[] images = {
                R.drawable.imgnew1, R.drawable.imgnew2,
                R.drawable.imgnew3, R.drawable.imgnew4,
                R.drawable.imgnew5, R.drawable.imgnew6,
                R.drawable.imgnew7, R.drawable.imgnew8,
                R.drawable.imgnew9, R.drawable.imgnew10,
                R.drawable.imgnew11, R.drawable.imgnew12,
                R.drawable.imgnew13, R.drawable.imgnew14, R.drawable.imgnew15};

        int[] smallIcon = {
                R.drawable.icnew1, R.drawable.icnew2,
                R.drawable.icnew3, R.drawable.icnew4,
                R.drawable.icnew5, R.drawable.icnew6,
                R.drawable.icnew7, R.drawable.icnew8,
                R.drawable.icnew9, R.drawable.icnew10,
                R.drawable.icnew11, R.drawable.icnew12,
                R.drawable.icnew13, R.drawable.icnew14, R.drawable.icnew15};

        String[] titles = getLocalizedContext(context).getResources().getStringArray(R.array.special_titles_news);
        String[] messages = getLocalizedContext(context).getResources().getStringArray(R.array.special_messages_news);
        int randomIndex = new java.util.Random().nextInt(messages.length);
        RemoteViews customView = createNotificationDayNight(context, R.layout.noti_custom_default, R.id.notification_root, pendingIntent, titles[randomIndex], messages[randomIndex], images[randomIndex]);
        RemoteViews customViewHeader = createNotificationDayNight(context, R.layout.noti_custom_header, R.id.notification_root, pendingIntent, titles[randomIndex], messages[randomIndex], images[randomIndex]);
        RemoteViews expandedView = createNotificationDayNight(context, R.layout.custom_notification_expand_news, R.id.notification_root_expanded, pendingIntent, titles[randomIndex], messages[randomIndex], images[randomIndex]);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIcon[randomIndex])
                .setContentIntent(pendingIntent)
                .setCustomContentView(customView)
                .setCustomHeadsUpContentView(customViewHeader)
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }
}


