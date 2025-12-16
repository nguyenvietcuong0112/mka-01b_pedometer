package com.fitness.pedometer.walkrun.stepmonitor.notiSpecial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.fitness.pedometer.walkrun.stepmonitor.utils.SharePreferenceUtils;


public class UserPresentReceiver extends BroadcastReceiver {

    private static final String PREF_NAME = "ad_prefs";
    private static final String KEY_LAST_AD_TIME = "last_ad_time";

    private static final String KEY_LAST_AD_TIME_OLD = "last_ad_time_old";

    private static final long time_open_noti_old = 20 * 60 * 1000L;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SharePreferenceUtils.isOrganicNoti(context)) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                startWakeService(context);
                showNotiNew(context);
            }

            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                startWakeService(context);
                showNotiOld(context);
            }
        } else {
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                startWakeService(context);
                new NotificationUtil().showAppRealNotification(context);
            }
        }
    }

    private void showNotiNew(Context context) {
        if (shouldShowAd(context, getDynamicTime())) {
            FullScreenAdNotificationHelper.showFullScreenAd(context);
            updateLastShowTime(context);
        }

    }

    private long getDynamicTime() {
        return 30 * 60 * 1000L;
    }


    private void showNotiOld(Context context) {
        if (shouldShowAdOld(context, time_open_noti_old)) {
            UnlockNotifier.handleUserPresent(context.getApplicationContext());
            updateLastShowTimeOld(context);
        }
    }

    private void startWakeService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            try {
                Intent serviceIntent = new Intent(context, WakeService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.getApplicationContext().startForegroundService(serviceIntent);
                } else {
                    context.getApplicationContext().startService(serviceIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean shouldShowAd(Context context, long time) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastShowTime = prefs.getLong(KEY_LAST_AD_TIME, 0);
        long currentTime = System.currentTimeMillis();

        boolean canShow = (currentTime - lastShowTime) >= time;
        Log.d("UserPresentReceiver", "Can show ad: " + canShow);
        return canShow;
    }

    private void updateLastShowTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_AD_TIME, System.currentTimeMillis()).apply();
    }

    private boolean shouldShowAdOld(Context context, long time) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastShowTime = prefs.getLong(KEY_LAST_AD_TIME_OLD, 0);
        long currentTime = System.currentTimeMillis();

        boolean canShow = (currentTime - lastShowTime) >= time;
        Log.d("UserPresentReceiver", "Can show ad: " + canShow);
        return canShow;
    }

    private void updateLastShowTimeOld(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_AD_TIME_OLD, System.currentTimeMillis()).apply();
    }

}