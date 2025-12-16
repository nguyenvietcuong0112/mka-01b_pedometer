package com.fitness.pedometer.walkrun.stepmonitor;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.appsflyer.AppsFlyerConversionListener;
import com.facebook.FacebookSdk;
import com.fitness.pedometer.walkrun.stepmonitor.activity.nativefull.ActivityLoadNativeFullV2;
import com.fitness.pedometer.walkrun.stepmonitor.activity.nativefull.ActivityLoadNativeFullV3;
import com.fitness.pedometer.walkrun.stepmonitor.activity.nativefull.ActivityLoadNativeFullV5;
import com.fitness.pedometer.walkrun.stepmonitor.notiSpecial.AppInstallReceiver;
import com.fitness.pedometer.walkrun.stepmonitor.notiSpecial.UserPresentReceiver;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SharePreferenceUtils;
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
import com.mallegan.ads.util.PreferenceManager;
//import com.stepcounter.healthapplines.pedometer.steptracker.com.fitness.pedometer.walkrun.stepmonitor.utils.TimerManager;


import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(new UserPresentReceiver(), filter);
        IntentFilter appInstallFilter = new IntentFilter();
        appInstallFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appInstallFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        appInstallFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appInstallFilter.addDataScheme("package");
        registerReceiver(new AppInstallReceiver(), appInstallFilter);
        FirebaseApp.initializeApp(this);
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(LanguageActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(IntroActivityNew.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(ProfileActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(PermissionActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivityUninstall.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(ActivityLoadNativeFullV2.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(ActivityLoadNativeFullV3.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(ActivityLoadNativeFullV5.class);

        FacebookSdk.setClientToken(getString(R.string.facebook_client_token));

        if (!SharePreferenceUtils.isOrganicNoti(getApplicationContext())) {
            AppsFlyer.getInstance().initAppFlyer(this, getString(R.string.AF_DEV_KEY), true);

        } else {
            AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {
                    String mediaSource = (String) conversionData.get("media_source");

                    SharePreferenceUtils.setOrganicNoti(getApplicationContext(), mediaSource == null || mediaSource.isEmpty() || mediaSource.equals("organic"));
                }

                @Override
                public void onConversionDataFail(String errorMessage) {
                    // Handle conversion data failure
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> attributionData) {
                    // Handle app open attribution
                }

                @Override
                public void onAttributionFailure(String errorMessage) {
                    // Handle attribution failure
                }
            };
            PreferenceManager.getInstance().putBoolean("is_admob_network_full_ads", true);
            AppsFlyer.getInstance().initAppFlyer(this, getString(R.string.AF_DEV_KEY), true, conversionListener);

        }

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