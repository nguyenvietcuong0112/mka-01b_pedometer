package com.fitness.pedometer.walkrun.stepmonitor;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.facebook.FacebookSdk;
import com.google.firebase.FirebaseApp;
import com.mallegan.ads.util.AdsApplication;
import com.mallegan.ads.util.AppOpenManager;
import com.mallegan.ads.util.AppsFlyer;
import com.fitness.pedometer.walkrun.stepmonitor.activity.LanguageActivity;
import com.fitness.pedometer.walkrun.stepmonitor.activity.MainActivity;
import com.fitness.pedometer.walkrun.stepmonitor.activity.PermissionActivity;
import com.fitness.pedometer.walkrun.stepmonitor.activity.ProfileActivity;
import com.fitness.pedometer.walkrun.stepmonitor.activity.SplashActivity;
import com.fitness.pedometer.walkrun.stepmonitor.activity.SplashActivityUninstall;
import com.fitness.pedometer.walkrun.stepmonitor.activity.fragmentIntro.IntroActivityNew;
import com.fitness.pedometer.walkrun.stepmonitor.utils.AppActivityTracker;
import com.fitness.pedometer.walkrun.stepmonitor.R;
//import com.stepcounter.healthapplines.pedometer.steptracker.com.fitness.pedometer.walkrun.stepmonitor.utils.TimerManager;


import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MyApplication extends AdsApplication {

    @Override
    public boolean enableAdsResume() {
        return true;
    }

    @Override
    public List<String> getListTestDeviceId() {
        return null;
    }

    @Override
    public String getResumeAdId() {
        return getString(R.string.open_resume);
    }

    @Override
    public Boolean buildDebug() {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(LanguageActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(IntroActivityNew.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(ProfileActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(PermissionActivity.class);

        FacebookSdk.setClientToken(getString(R.string.facebook_client_token));

        AppsFlyer.getInstance().initAppFlyer(this, getString(R.string.AF_DEV_KEY), true);
        AppActivityTracker.getInstance().register(this);

    }
    @Override
    public void onTerminate() {
        super.onTerminate();
//        TimerManager.getInstance().stopTimer();
    }

    public static Context getLocalizedContext2(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    public void updateShortcuts(String langCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            if (shortcutManager == null) return;

            Context localizedContext = getLocalizedContext2(getApplicationContext(), langCode);

            Intent uninstallIntent = new Intent(this, SplashActivityUninstall.class);
            uninstallIntent.setAction(Intent.ACTION_VIEW);
            uninstallIntent.putExtra("shortcut", "uninstall_fake");
            uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            ShortcutInfo uninstallShortcut = new ShortcutInfo.Builder(this, "id_uninstall_fake")
                    .setShortLabel(localizedContext.getString(R.string.uninstall))
                    .setLongLabel(localizedContext.getString(R.string.uninstall))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_uninstall))
                    .setIntent(uninstallIntent)
                    .build();

            shortcutManager.removeAllDynamicShortcuts();
            shortcutManager.setDynamicShortcuts(Collections.singletonList(uninstallShortcut));
        }
    }


}