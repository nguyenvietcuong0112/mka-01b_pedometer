package com.fitness.pedometer.walkrun.stepmonitor.activity;

import android.annotation.SuppressLint;
import android.content.Intent;

import com.mallegan.ads.callback.AdCallback;
import com.mallegan.ads.util.AppOpenManager;
import com.mallegan.ads.util.ConsentHelper;
import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.activity.nativefull.ActivityLoadNativeFullV2;
import com.fitness.pedometer.walkrun.stepmonitor.base.BaseActivity;
import com.fitness.pedometer.walkrun.stepmonitor.databinding.ActivitySplashBinding;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SharePreferenceUtils;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SystemUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivityUninstall extends BaseActivity {
    ActivitySplashBinding activitySplashBinding;


    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        activitySplashBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(activitySplashBinding.getRoot());
        ConsentHelper consentHelper = ConsentHelper.getInstance(this);
        if (!consentHelper.canLoadAndShowAds()) {
            consentHelper.reset();
        }
        consentHelper.obtainConsentAndShow(this, this::openAppUninstall);
    }

    private void openAppUninstall() {
        AppOpenManager.getInstance().loadOpenAppAdSplash(
                SplashActivityUninstall.this,
                getString(R.string.open_splash_uninstall),
                0,
                30000,
                true,
                new AdCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        if (!SharePreferenceUtils.isOrganic(SplashActivityUninstall.this)) {
                            ActivityLoadNativeFullV2.open(SplashActivityUninstall.this, getString(R.string.native_full_splash_uninstall), () -> {
                                Intent intent = new Intent(SplashActivityUninstall.this, UninstallActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            Intent intent = new Intent(SplashActivityUninstall.this, UninstallActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
        );

    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}

