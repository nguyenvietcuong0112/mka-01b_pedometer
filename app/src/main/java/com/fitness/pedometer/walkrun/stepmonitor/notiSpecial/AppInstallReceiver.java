package com.fitness.pedometer.walkrun.stepmonitor.notiSpecial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SharePreferenceUtils;

public class AppInstallReceiver extends BroadcastReceiver {
    private static final String TAG = "AppInstallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!SharePreferenceUtils.isOrganic(context)) {
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                Log.e(TAG, "Handling PACKAGE_ADDED");
                FirebaseAnalytics.getInstance(context)
                        .logEvent("event_show_noti_install_app_new", null);
                handleAppInstalled(context, intent);
            } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                Log.e(TAG, "Handling PACKAGE_REPLACED");
                FirebaseAnalytics.getInstance(context)
                        .logEvent("event_show_noti_replace_app_new", null);
                handleAppReplaced(context, intent);
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                Log.e(TAG, "Handling PACKAGE_REMOVED");
                FirebaseAnalytics.getInstance(context)
                        .logEvent("event_show_noti_remove_app_new", null);
                handleAppRemoved(context, intent);
            } else {
                Log.e(TAG, "Unknown action: " + action);
            }
        }
    }

    private void handleAppInstalled(Context context, Intent intent) {
        try {
            String packageName = intent.getData().getSchemeSpecificPart();
            boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            Log.d(TAG, "App installed: " + packageName + ", isReplacing: " + isReplacing);

            // Bỏ qua nếu đây là app update (không phải cài mới)
            if (isReplacing) {
                Log.d(TAG, "Skipping app update: " + packageName);
                return;
            }

            // Lấy thông tin chi tiết của app
            AppInfo appInfo = getAppInfo(context, packageName);
            if (appInfo != null) {
                Log.d(TAG, "App details - Name: " + appInfo.appName + ", Package: " + appInfo.packageName);

                showAppInstallNotification(context, appInfo);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling app install: " + e.getMessage(), e);
        }
    }

    private void handleAppReplaced(Context context, Intent intent) {
        try {
            String packageName = intent.getData().getSchemeSpecificPart();
            Log.d(TAG, "App updated: " + packageName);

            // Xử lý khi app được update
            AppInfo appInfo = getAppInfo(context, packageName);
            if (appInfo != null) {
                Log.d(TAG, "Updated app: " + appInfo.appName);
                showAppUpdatedNotification(context, appInfo);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling app update: " + e.getMessage(), e);
        }
    }

    private void handleAppRemoved(Context context, Intent intent) {
        try {
            String packageName = intent.getData().getSchemeSpecificPart();
            boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            Log.d(TAG, "App removed: " + packageName + ", isReplacing: " + isReplacing);

            // Chỉ xử lý khi app thực sự bị gỡ (không phải update)
            if (!isReplacing) {
                Log.d(TAG, "App completely removed: " + packageName);
                // Lấy thông tin app trước khi bị gỡ (có thể không thành công)
                AppInfo appInfo = getAppInfo(context, packageName);
                if (appInfo != null) {
                    showAppRemovedNotification(context, appInfo);
                } else {
                    // Tạo AppInfo với thông tin cơ bản nếu không lấy được
                    AppInfo basicAppInfo = new AppInfo(packageName, packageName, null);
                    showAppRemovedNotification(context, basicAppInfo);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling app removal: " + e.getMessage(), e);
        }
    }

    private AppInfo getAppInfo(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);

            String appName = pm.getApplicationLabel(appInfo).toString();
            Drawable appIcon = pm.getApplicationIcon(appInfo);

            return new AppInfo(packageName, appName, appIcon);

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package not found: " + packageName, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting app info for: " + packageName, e);
            return null;
        }
    }

    private void showAppInstallNotification(Context context, AppInfo appInfo) {
        try {
            // Sử dụng NotificationUtil để hiển thị thông báo
            NotificationUtil notificationUtil = new NotificationUtil();
            notificationUtil.showAppInstallNotification(context, appInfo);
        } catch (Exception e) {
            Log.e(TAG, "Error showing app install notification: " + e.getMessage(), e);
        }
    }

    private void showAppRemovedNotification(Context context, AppInfo appInfo) {
        try {
            NotificationUtil notificationUtil = new NotificationUtil();
            notificationUtil.showAppRemovedNotification(context, appInfo);
        } catch (Exception e) {
            Log.e(TAG, "Error showing app removed notification: " + e.getMessage(), e);
        }
    }

    private void showAppUpdatedNotification(Context context, AppInfo appInfo) {
        try {
            NotificationUtil notificationUtil = new NotificationUtil();
            notificationUtil.showAppUpdatedNotification(context, appInfo);
        } catch (Exception e) {
            Log.e(TAG, "Error showing app updated notification: " + e.getMessage(), e);
        }
    }

    // Inner class để lưu thông tin app
    public static class AppInfo {
        public final String packageName;
        public final String appName;
        public final Drawable appIcon;

        public AppInfo(String packageName, String appName, Drawable appIcon) {
            this.packageName = packageName;
            this.appName = appName;
            this.appIcon = appIcon;
        }
    }
}