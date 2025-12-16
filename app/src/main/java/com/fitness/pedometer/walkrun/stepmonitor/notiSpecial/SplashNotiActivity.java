package com.fitness.pedometer.walkrun.stepmonitor.notiSpecial;

import android.content.Intent;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mallegan.ads.callback.AdCallback;
import com.mallegan.ads.util.AppOpenManager;
import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.activity.LanguageActivity;
import com.fitness.pedometer.walkrun.stepmonitor.activity.nativefull.ActivityLoadNativeFullV2;
import com.fitness.pedometer.walkrun.stepmonitor.base.BaseActivity;
import com.fitness.pedometer.walkrun.stepmonitor.databinding.SplashNotiActivityBinding;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SharePreferenceUtils;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SystemConfiguration;


public class SplashNotiActivity extends BaseActivity {

    private SplashNotiActivityBinding binding;


    @Override
    public void bind() {
        binding = SplashNotiActivityBinding.inflate(getLayoutInflater());
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        setContentView(binding.getRoot());

        loadAndShowOpenSplash();

        FirebaseAnalytics.getInstance(this)
                .logEvent("event_click_noti_new", null);
    }

    private void loadAndShowOpenSplash() {
        AppOpenManager.getInstance().loadOpenAppAdSplash(
                this,
                getString(R.string.open_splash_noti),
                3000,
                60000,
                true,
                new AdCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        if (!SharePreferenceUtils.isOrganic(SplashNotiActivity.this)) {
                            ActivityLoadNativeFullV2.open(SplashNotiActivity.this, getString(R.string.native_full_splash_noti), () -> {
                                proceedToNextActivity();
                            });
                        } else {
                            proceedToNextActivity();
                        }
                    }
                }
        );
    }

    private void proceedToNextActivity() {
        startActivity(new Intent(this, LanguageActivity.class));
        finish();
    }
}
