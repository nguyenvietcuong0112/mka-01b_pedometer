package com.fitness.pedometer.walkrun.stepmonitor.notiSpecial;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.mallegan.ads.callback.AdCallback;
import com.mallegan.ads.util.AppOpenManager;
import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.activity.nativefull.ActivityLoadNativeFullV3;
import com.fitness.pedometer.walkrun.stepmonitor.base.BaseActivity;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SystemConfiguration;


public class FullScreenAdActivity extends BaseActivity {

    LinearLayout loading;

    @Override
    public void bind() {
        AppOpenManager.getInstance().disableAppResumeWithActivity(FullScreenAdActivity.class);
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        AppOpenManager.getInstance().disableAppResume();
        setContentView(R.layout.activity_full_screen);
//        FullScreenAdManager adManager = FullScreenAdManager.getInstance();
        loading = findViewById(R.id.loadingOverlay);
//
//        if (!adManager.isAdReady() && !adManager.isLoading()) {
//            Log.d("minh", "Requesting new ad preload...");
//            adManager.loadAd(this);
//            FirebaseAnalytics.getInstance(this).logEvent("event_click_full_screen_final", null);
//        } else {
//            Log.d("minh", "Skip preload, ad ready or loading...");
//        }

        View rootView = findViewById(android.R.id.content);
        rootView.setOnClickListener(v -> {
            rootView.setEnabled(false);
            loading.setVisibility(View.VISIBLE);
            Log.d("minh", "Click full screen ad area");
            AppOpenManager.getInstance().loadOpenAppAdSplash(
                    this,
                    getString(R.string.open_splash_noti_full_screen),
                    0,
                    60000,
                    true,
                    new AdCallback() {
                        @Override
                        public void onNextAction() {
                            try {
                                super.onNextAction();
                                loadNativeFull();
                                loading.setVisibility(View.GONE);
                            } catch (Exception e) {
                                loadNativeFull();
                                loading.setVisibility(View.GONE);
                            }
                        }
                    }
            );

//            if (adManager.isAdReady()) {
//                adManager.showAd(this, new InterCallback() {
//                    @Override
//                    public void onAdClosedByUser() {
//                        ActivityLoadNativeFullV3.open(
//                                FullScreenAdActivity.this,
//                                getString(R.string.native_full_splash_noti_full_screen_high),
//                                getString(R.string.native_full_splash_noti_full_screen),
//                                () -> processActivity());
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(LoadAdError i) {
//                        processActivity();
//                    }
//                });
//            } else {
//                Admob.getInstance().loadAndShowInter(this, getString(R.string.inter_splash_noti_full_screen_high), 0,30000, new InterCallback() {
//                    @Override
//                    public void onAdClosed() {
//                        super.onAdClosed();
//                        loadNativeFull();
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(LoadAdError i) {
//                        super.onAdFailedToLoad(i);
//                        Admob.getInstance().loadAndShowInter(FullScreenAdActivity.this, getString(R.string.inter_splash_noti_full_screen), 0, 30000, new InterCallback() {
//                            @Override
//                            public void onAdClosed() {
//                                super.onAdClosed();
//                                loadNativeFull();
//                            }
//
//                            @Override
//                            public void onAdFailedToLoad(LoadAdError i) {
//                                super.onAdFailedToLoad(i);
//                                processActivity();
//                            }
//                        });
//                    }
//                });
//            }

            rootView.postDelayed(() -> rootView.setEnabled(true), 2000);
        });
    }

    private void loadNativeFull() {
        ActivityLoadNativeFullV3.open(
                FullScreenAdActivity.this,
                getString(R.string.native_full_splash_noti_full_screen_high),
                getString(R.string.native_full_splash_noti_full_screen),
                this::processActivity);
    }

    private void processActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
