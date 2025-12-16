package com.fitness.pedometer.walkrun.stepmonitor.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.activity.nativefull.ActivityFullCallback;
import com.fitness.pedometer.walkrun.stepmonitor.activity.nativefull.ActivityLoadNativeFullV2;
import com.fitness.pedometer.walkrun.stepmonitor.fragment.AchievementFragment;
import com.fitness.pedometer.walkrun.stepmonitor.fragment.ActivityFragment;
import com.fitness.pedometer.walkrun.stepmonitor.fragment.HomeFragment;
import com.fitness.pedometer.walkrun.stepmonitor.fragment.ReportFragment;
import com.fitness.pedometer.walkrun.stepmonitor.fragment.SettingsFragment;
import com.fitness.pedometer.walkrun.stepmonitor.service.StepCounterService;
import com.fitness.pedometer.walkrun.stepmonitor.utils.BottomNavigationHelper;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SharePreferenceUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String TAG_HOME = "home";
    private static final String TAG_ACTIVITY = "activity";
    private static final String TAG_REPORT = "report";
    private static final String TAG_ACHIEVEMENT = "achievement";
    private static final String TAG_SETTINGS = "settings";

    private FrameLayout frAdsBanner;

    private Handler interHandler = new Handler();
    private Runnable interRunnable;
    private boolean isShowingInter = false;
    private static final long INTER_REPEAT_DELAY_MS = 15_000L;
    private long nextInterAllowedAt = 0L;
    private boolean hasShownInterOnce = false;

    // NEW: flag to avoid showing interstitial on initial activity load
    private boolean isInitialLoad = true;

    private int currentNav = BottomNavigationHelper.NAV_STEPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        hideNavigationBar();

        if (!PermissionActivity.hasAllRequiredPermissions(this)) {
            Intent intent = new Intent(this, PermissionActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (!com.fitness.pedometer.walkrun.stepmonitor.utils.ProfileDataManager.isProfileCompleted(this)) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        frAdsBanner = findViewById(R.id.fr_ads_banner);

        startStepServiceIfNeeded();

        int initialNav = getIntent().getIntExtra("nav", BottomNavigationHelper.NAV_STEPS);
        currentNav = initialNav;

        // Reset any scheduled cooldown so first navigation can show interstitial on user click
        cancelScheduledInter();
        nextInterAllowedAt = 0L;
        hasShownInterOnce = false;

        // Load initial fragment but DO NOT treat this as a "nav click"
        loadFragment(initialNav);
        // After initial load, subsequent loadFragment(...) calls count as user navigation
        isInitialLoad = false;

        setupBottomNavigation();
        loadAdsBanner();
    }

    private void startStepServiceIfNeeded() {
        // Check if service is already running
        if (isServiceRunning(StepCounterService.class)) {
            Log.d(TAG, "StepCounterService is already running");
            return;
        }

        Intent serviceIntent = new Intent(this, StepCounterService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
                Log.d(TAG, "Started StepCounterService as foreground service");
            } else {
                startService(serviceIntent);
                Log.d(TAG, "Started StepCounterService as background service");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start StepCounterService", e);
            // Service will be started again when app is opened next time
        }
    }

    /**
     * Check if a service is currently running
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            try {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (serviceClass.getName().equals(service.service.getClassName())) {
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking if service is running", e);
            }
        }
        return false;
    }

    private void setupBottomNavigation() {
        BottomNavigationHelper.setupBottomNavigation(this, currentNav);
    }

    /**
     * Load fragment for given nav.
     * NOTE: showInterOnNavChange() is only called when not the initial load (i.e., when user actually navigates).
     */
    public void loadFragment(int nav) {
        Fragment fragment = null;
        String tag = null;

        switch (nav) {
            case BottomNavigationHelper.NAV_STEPS:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_HOME);
                if (fragment == null) {
                    fragment = new HomeFragment();
                }
                tag = TAG_HOME;
                break;
            case BottomNavigationHelper.NAV_ACTIVITY:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_ACTIVITY);
                if (fragment == null) {
                    fragment = ActivityFragment.newInstance(false);
                }
                tag = TAG_ACTIVITY;
                break;
            case BottomNavigationHelper.NAV_REPORT:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_REPORT);
                if (fragment == null) {
                    fragment = new ReportFragment();
                }
                tag = TAG_REPORT;
                break;
            case BottomNavigationHelper.NAV_ACHIEVEMENT:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_ACHIEVEMENT);
                if (fragment == null) {
                    fragment = new AchievementFragment();
                }
                tag = TAG_ACHIEVEMENT;
                break;
            case BottomNavigationHelper.NAV_SETTINGS:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS);
                if (fragment == null) {
                    fragment = new SettingsFragment();
                }
                tag = TAG_SETTINGS;
                break;
        }

        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment, tag);
            transaction.commit();
            currentNav = nav;
            setupBottomNavigation();

            // Only show interstitial when this navigation is user-initiated (i.e., not initial load)
            if (!isInitialLoad) {
                showInterOnNavChange();
            }
        }
    }

    private void showInterOnNavChange() {
        if (SharePreferenceUtils.isOrganic(MainActivity.this)) return;

        long now = System.currentTimeMillis();

        // Nếu đã show ít nhất 1 lần, áp dụng cooldown như trước
        if (hasShownInterOnce) {
            if (now < nextInterAllowedAt) return;
            if (isShowingInter) return;
        } else {
            // lần đầu (do user click), cho phép show ngay cả khi nextInterAllowedAt bị set (chúng ta reset ở onCreate)
            if (isShowingInter) return;
        }

        isShowingInter = true;

        final String interAdUnit = getString(R.string.inter_home);

        Admob.getInstance().loadAndShowInter(
                MainActivity.this,
                interAdUnit,
                0,
                30000,
                new com.mallegan.ads.callback.InterCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        isShowingInter = false;
                        // chỉ mark đã show 1 lần khi ad thực sự được shown và user đóng nó
                        hasShownInterOnce = true;
                        openNativeFullThenScheduleNext();
                    }

                    @Override
                    public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        isShowingInter = false;
                        // KHÔNG set hasShownInterOnce = true ở đây để tránh bị block bởi cooldown
                        // Nếu muốn fallback UI, mở native-full nhưng KHÔNG schedule cooldown
                        openNativeFullFallbackNoSchedule();
                    }

                    @Override
                    public void onAdFailedToShow(com.google.android.gms.ads.AdError adError) {
                        super.onAdFailedToShow(adError);
                        isShowingInter = false;
                        // KHÔNG set hasShownInterOnce = true
                        // fallback nhưng không schedule cooldown
                        openNativeFullFallbackNoSchedule();
                    }
                }
        );
    }

    /**
     * Mở native-full và sau khi hoàn tất sẽ gọi scheduleNextInter() như cũ.
     * Dùng khi interstitial đã được show và user đóng nó.
     */
    private void openNativeFullThenScheduleNext() {
        ActivityLoadNativeFullV2.open(MainActivity.this, getString(R.string.native_full_inter_finish), new ActivityFullCallback() {
            @Override
            public void onResultFromActivityFull() {
                scheduleNextInter();
            }
        });
    }

    /**
     * Mở native-full fallback **nhưng không** schedule cooldown khi fallback do interstitial fail.
     */
    private void openNativeFullFallbackNoSchedule() {
        ActivityLoadNativeFullV2.open(MainActivity.this, getString(R.string.native_full_inter_finish), new ActivityFullCallback() {
            @Override
            public void onResultFromActivityFull() {
                // intentionally do nothing: fallback UI completed but do NOT schedule next interstitial cooldown
            }
        });
    }

    private void scheduleNextInter() {
        if (SharePreferenceUtils.isOrganic(MainActivity.this)) return;

        nextInterAllowedAt = System.currentTimeMillis() + INTER_REPEAT_DELAY_MS;

        if (interRunnable != null) {
            interHandler.removeCallbacks(interRunnable);
        }

        interRunnable = new Runnable() {
            @Override
            public void run() {
                interRunnable = null;
                nextInterAllowedAt = 0L;
            }
        };

        interHandler.postDelayed(interRunnable, INTER_REPEAT_DELAY_MS);
    }

    private void cancelScheduledInter() {
        if (interRunnable != null) {
            interHandler.removeCallbacks(interRunnable);
            interRunnable = null;
        }
        nextInterAllowedAt = 0L;
    }

    private void loadAdsBanner() {
        if(!SharePreferenceUtils.isOrganic(MainActivity.this)) {
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_banner_home), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(MainActivity.this).inflate(R.layout.ad_native_admob_banner_1, null);
                    frAdsBanner.setVisibility(View.VISIBLE);
                    frAdsBanner.removeAllViews();
                    frAdsBanner.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    frAdsBanner.setVisibility(View.GONE);
                }
            });

        } else {
            frAdsBanner.removeAllViews();
            frAdsBanner.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelScheduledInter();
        interHandler.removeCallbacksAndMessages(null);
    }

    private void hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and later
            Window window = getWindow();
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

}
