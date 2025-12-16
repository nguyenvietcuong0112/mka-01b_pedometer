package com.fitness.pedometer.walkrun.stepmonitor.notiSpecial;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SystemUtil;

import java.util.Locale;

public class NotificationUtil {

    public RemoteViews createNotificationClosedAppView(Context context, int layoutId, int rootViewId, PendingIntent pendingIntent) {
        RemoteViews view = new RemoteViews(context.getPackageName(), layoutId);

        int padding = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                ? (int) (20 * context.getResources().getDisplayMetrics().density) : 0;
        view.setViewPadding(rootViewId, padding, padding, padding, padding);

        view.setOnClickPendingIntent(R.id.notification_button, pendingIntent);
        view.setTextViewText(R.id.notification_title_bold, getLocalizedContext(context).getString(R.string.title_out_done));
        view.setTextViewText(R.id.notification_title, getLocalizedContext(context).getString(R.string.body_out_done));
        view.setTextViewText(R.id.notification_button, getLocalizedContext(context).getString(R.string.continued));

        return view;
    }

    public RemoteViews createNotificationDownloadView(Context context, int layoutId, int rootViewId, PendingIntent pendingIntent, String fileName) {
        RemoteViews view = new RemoteViews(context.getPackageName(), layoutId);

        int padding = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                ? (int) (20 * context.getResources().getDisplayMetrics().density) : 0;
        view.setViewPadding(rootViewId, padding, padding, padding, padding);

        view.setOnClickPendingIntent(R.id.notification_button, pendingIntent);
        view.setTextViewText(R.id.notification_title_bold, getLocalizedContext(context).getString(R.string.title_dowload_done));
        view.setTextViewText(R.id.notification_title, getLocalizedContext(context).getString(R.string.body_dowload_done)  + fileName);
        view.setTextViewText(R.id.notification_button, getLocalizedContext(context).getString(R.string.open_now));

        return view;
    }

    public RemoteViews createNotificationScreenShotView(Context context, int layoutId, int rootViewId, PendingIntent pendingIntent) {
        RemoteViews view = new RemoteViews(context.getPackageName(), layoutId);

        int padding = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                ? (int) (20 * context.getResources().getDisplayMetrics().density) : 0;
        view.setViewPadding(rootViewId, padding, padding, padding, padding);

        view.setOnClickPendingIntent(R.id.notification_button, pendingIntent);
        view.setTextViewText(R.id.notification_title_bold, getLocalizedContext(context).getString(R.string.title_screen_shoot_done));
        view.setTextViewText(R.id.notification_title, getLocalizedContext(context).getString(R.string.body_screen_shoot_done));
        view.setTextViewText(R.id.notification_button, getLocalizedContext(context).getString(R.string.open_now));
        return view;
    }

    public RemoteViews createNotificationAppInstallView(Context context, int layoutId, int rootViewId, PendingIntent pendingIntent, String appName) {
        RemoteViews view = new RemoteViews(context.getPackageName(), layoutId);

        int padding = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                ? (int) (20 * context.getResources().getDisplayMetrics().density) : 0;
        view.setViewPadding(rootViewId, padding, padding, padding, padding);

        view.setOnClickPendingIntent(R.id.notification_button, pendingIntent);
        view.setTextViewText(R.id.notification_title_bold, "New App Installed");
        view.setTextViewText(R.id.notification_title, appName + " has been installed");
        view.setTextViewText(R.id.notification_button, "Open Now");
        return view;
    }

    public RemoteViews createNotificationAppRemovedView(Context context, int layoutId, int rootViewId, PendingIntent pendingIntent, String appName) {
        RemoteViews view = new RemoteViews(context.getPackageName(), layoutId);

        int padding = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                ? (int) (20 * context.getResources().getDisplayMetrics().density) : 0;
        view.setViewPadding(rootViewId, padding, padding, padding, padding);

        view.setOnClickPendingIntent(R.id.notification_button, pendingIntent);
        view.setTextViewText(R.id.notification_title_bold, "❌ App Removed");
        view.setTextViewText(R.id.notification_title, "An app just vanished from your phone.");
        view.setTextViewText(R.id.notification_button, "See which one");
        return view;
    }

    public RemoteViews createNotificationAppUpdatedView(Context context, int layoutId, int rootViewId, PendingIntent pendingIntent, String appName) {
        RemoteViews view = new RemoteViews(context.getPackageName(), layoutId);

        int padding = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                ? (int) (20 * context.getResources().getDisplayMetrics().density) : 0;
        view.setViewPadding(rootViewId, padding, padding, padding, padding);

        view.setOnClickPendingIntent(R.id.notification_button, pendingIntent);
        view.setTextViewText(R.id.notification_title_bold, "🚀 App Updated!");
        view.setTextViewText(R.id.notification_title, "Fresh features are waiting for you.");
        view.setTextViewText(R.id.notification_button, "Explore Now");
        return view;
    }

    public void showDownloadNotification(Context context, String filePath) {
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
        String fileName = new java.io.File(filePath).getName();
        Intent openFileIntent = new Intent(context, SplashNotiActivity.class);
        openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        openFileIntent.putExtra("path", filePath);
        openFileIntent.putExtra("name", fileName);
        openFileIntent.putExtra("isFromNotification", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openFileIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        RemoteViews customView = createNotificationDownloadView(context, R.layout.custom_notification_dowload, R.id.notification_root, pendingIntent, fileName);
        RemoteViews customViewHeader = createNotificationDownloadView(context, R.layout.custom_noti_header_dowload, R.id.notification_root, pendingIntent, fileName);
        RemoteViews expandedView = createNotificationDownloadView(context, R.layout.custom_notification_expanded_dowload, R.id.notification_root_expanded, pendingIntent, fileName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setCustomContentView(customView)
                .setCustomHeadsUpContentView(customViewHeader)
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }

    public void showScreenShotNotification(Context context, Bitmap screenshotBitmap) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        String CHANNEL_ID = "DownloadFileObserverChannel";
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Download File Observer Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription("Channel for file observer download notifications");
        }
        notificationManager.createNotificationChannel(channel);


        Intent openFileIntent = new Intent(context, SplashNotiActivity.class);
        openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openFileIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        RemoteViews customView = createNotificationScreenShotView(context, R.layout.custom_notification_dowload, R.id.notification_root, pendingIntent);
        RemoteViews customViewHeader = createNotificationScreenShotView(context, R.layout.custom_noti_header_dowload, R.id.notification_root, pendingIntent);
        RemoteViews expandedView =createNotificationScreenShotView(context, R.layout.custom_notification_expanded_dowload, R.id.notification_root_expanded, pendingIntent);

        if (screenshotBitmap != null) {
            customView.setImageViewBitmap(R.id.notification_image, screenshotBitmap);
            customViewHeader.setImageViewBitmap(R.id.notification_image, screenshotBitmap);
            expandedView.setImageViewBitmap(R.id.notification_image, screenshotBitmap);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setCustomContentView(customView)
                .setCustomHeadsUpContentView(customViewHeader)
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }

    public void showClosedAppNotification(Context context) {
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

        RemoteViews customView = createNotificationClosedAppView(context, R.layout.custom_notification, R.id.notification_root, pendingIntent);
        RemoteViews customViewHeader = createNotificationClosedAppView(context, R.layout.custom_noti_header, R.id.notification_root, pendingIntent);
        RemoteViews expandedView = createNotificationClosedAppView(context, R.layout.custom_notification_expanded, R.id.notification_root_expanded, pendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setCustomContentView(customView)
                .setCustomHeadsUpContentView(customViewHeader)
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }

    public void showAppInstallNotification(Context context, AppInstallReceiver.AppInfo appInfo) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        String CHANNEL_ID = "AppInstallChannel";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Install",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("App install notifications");
            notificationManager.createNotificationChannel(channel);
        }
        Intent openAppIntent = new Intent(context, SplashNotiActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tạo custom views
        RemoteViews customView = createNotificationAppInstallView(context, R.layout.noti_custom_default, R.id.notification_root, pendingIntent, appInfo.appName);
        RemoteViews customViewHeader = createNotificationAppInstallView(context, R.layout.custom_noti_header_dowload, R.id.notification_root, pendingIntent, appInfo.appName);
        RemoteViews expandedView = createNotificationAppInstallView(context, R.layout.custom_notification_expanded_dowload, R.id.notification_root_expanded, pendingIntent, appInfo.appName);

        // Chuyển đổi Drawable thành Bitmap để hiển thị icon
        Bitmap appIconBitmap = null;
        if (appInfo.appIcon != null) {
            appIconBitmap = drawableToBitmap(appInfo.appIcon);
        }

        // Set icon vào notification views
        if (appIconBitmap != null) {
            customView.setImageViewBitmap(R.id.notification_image, appIconBitmap);
            customViewHeader.setImageViewBitmap(R.id.notification_image, appIconBitmap);
            expandedView.setImageViewBitmap(R.id.notification_image, appIconBitmap);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setCustomContentView(customView)
                .setCustomHeadsUpContentView(customViewHeader)
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }

    public void showAppRemovedNotification(Context context, AppInstallReceiver.AppInfo appInfo) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        String CHANNEL_ID = "AppInstallChannel";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Install",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("App install notifications");
            notificationManager.createNotificationChannel(channel);
        }

        // Intent để mở app của chúng ta
        Intent openAppIntent = new Intent(context, SplashNotiActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tạo custom views
        RemoteViews customView = createNotificationAppRemovedView(context, R.layout.custom_notification_dowload, R.id.notification_root, pendingIntent, appInfo.appName);
        RemoteViews customViewHeader = createNotificationAppRemovedView(context, R.layout.custom_noti_header_dowload, R.id.notification_root, pendingIntent, appInfo.appName);
        RemoteViews expandedView = createNotificationAppRemovedView(context, R.layout.custom_notification_expanded_dowload, R.id.notification_root_expanded, pendingIntent, appInfo.appName);

        // Chuyển đổi Drawable thành Bitmap để hiển thị icon
        Bitmap appIconBitmap = null;
        if (appInfo.appIcon != null) {
            appIconBitmap = drawableToBitmap(appInfo.appIcon);
        }

        // Set icon vào notification views
        if (appIconBitmap != null) {
            customView.setImageViewBitmap(R.id.notification_image, appIconBitmap);
            customViewHeader.setImageViewBitmap(R.id.notification_image, appIconBitmap);
            expandedView.setImageViewBitmap(R.id.notification_image, appIconBitmap);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setCustomContentView(customView)
                .setCustomHeadsUpContentView(customViewHeader)
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }

    public void showAppUpdatedNotification(Context context, AppInstallReceiver.AppInfo appInfo) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        String CHANNEL_ID = "AppInstallChannel";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Install",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("App install notifications");
            notificationManager.createNotificationChannel(channel);
        }

        Intent openAppIntent = new Intent(context, SplashNotiActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tạo custom views
        RemoteViews customView = createNotificationAppUpdatedView(context, R.layout.custom_notification_dowload, R.id.notification_root, pendingIntent, appInfo.appName);
        RemoteViews customViewHeader = createNotificationAppUpdatedView(context, R.layout.custom_noti_header_dowload, R.id.notification_root, pendingIntent, appInfo.appName);
        RemoteViews expandedView = createNotificationAppUpdatedView(context, R.layout.custom_notification_expanded_dowload, R.id.notification_root_expanded, pendingIntent, appInfo.appName);

        // Chuyển đổi Drawable thành Bitmap để hiển thị icon
        Bitmap appIconBitmap = null;
        if (appInfo.appIcon != null) {
            appIconBitmap = drawableToBitmap(appInfo.appIcon);
        }

        // Set icon vào notification views
        if (appIconBitmap != null) {
            customView.setImageViewBitmap(R.id.notification_image, appIconBitmap);
            customViewHeader.setImageViewBitmap(R.id.notification_image, appIconBitmap);
            expandedView.setImageViewBitmap(R.id.notification_image, appIconBitmap);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setCustomContentView(customView)
                .setCustomHeadsUpContentView(customViewHeader)
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) return null;
        
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), 
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
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
    public void showAppRealNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        String CHANNEL_ID = "AppInstallChannel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Install",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("App install notifications");
            notificationManager.createNotificationChannel(channel);
        }

        Intent openAppIntent = new Intent(context, SplashNotiActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        RemoteViews customView = createNotificationReal(context, R.layout.custom_notification_dowload, R.id.notification_root, pendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setCustomContentView(customView)
                .setCustomHeadsUpContentView(customView)
                .setCustomBigContentView(customView)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }

    public RemoteViews createNotificationReal(Context context, int layoutId, int rootViewId, PendingIntent pendingIntent) {
        RemoteViews view = new RemoteViews(context.getPackageName(), layoutId);

        int padding = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                ? (int) (20 * context.getResources().getDisplayMetrics().density) : 0;
        view.setViewPadding(rootViewId, padding, padding, padding, padding);

        view.setOnClickPendingIntent(R.id.notification_button, pendingIntent);
        view.setTextViewText(R.id.notification_title_bold, "Welcome Back!");
        view.setTextViewText(R.id.notification_title, "Let’s continue tracking your steps and stay active today!");
        view.setTextViewText(R.id.notification_button, "Call Now!");
        return view;
    }

}
