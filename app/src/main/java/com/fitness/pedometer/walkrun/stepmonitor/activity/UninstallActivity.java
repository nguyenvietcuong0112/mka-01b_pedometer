package com.fitness.pedometer.walkrun.stepmonitor.activity;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.base.BaseActivity;
import com.fitness.pedometer.walkrun.stepmonitor.databinding.ActivityUninstallBinding;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SharePreferenceUtils;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SystemConfiguration;


public class UninstallActivity extends BaseActivity {

    private ActivityUninstallBinding binding;

    @Override
    public void bind() {
        binding = ActivityUninstallBinding.inflate(getLayoutInflater());
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> {
            startActivity(new Intent(UninstallActivity.this, MainActivity.class));
            finish();
        });
        binding.TryAgain.setOnClickListener(v -> {
            startActivity(new Intent(UninstallActivity.this, MainActivity.class));
            finish();
        });
        binding.Explore.setOnClickListener(v -> {
            startActivity(new Intent(UninstallActivity.this, MainActivity.class));
            finish();
        });
        binding.dont.setOnClickListener(v -> {
            startActivity(new Intent(UninstallActivity.this, MainActivity.class));
            finish();
        });
        binding.still.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(UninstallActivity.this, MainActivity.class));
                finish();
            }
        });
        loadAds();
    }


    private void loadAds() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_keep_user), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView;
                if (!SharePreferenceUtils.isOrganic(UninstallActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(UninstallActivity.this).inflate(R.layout.layout_native_language_non_organic, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(UninstallActivity.this).inflate(R.layout.layout_native_language, null);

                }

                binding.llAds.removeAllViews();
                binding.llAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                binding.llAds.removeAllViews();
            }
        });
    }

    private boolean hasAllPermissions() {
        return checkCameraPermission();

    }


    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0;
    }
}
