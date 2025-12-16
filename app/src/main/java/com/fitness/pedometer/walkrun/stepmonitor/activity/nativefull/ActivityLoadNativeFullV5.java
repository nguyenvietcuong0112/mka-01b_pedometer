package com.fitness.pedometer.walkrun.stepmonitor.activity.nativefull;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.base.BaseActivity;
import com.fitness.pedometer.walkrun.stepmonitor.databinding.ActivityNativeFullBinding;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SystemConfiguration;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;


import java.util.Random;

public class ActivityLoadNativeFullV5 extends BaseActivity {
    ActivityNativeFullBinding binding;
    public static final String NATIVE_FUll_AD_ID_HIGH = "native_full_ad_id_high";
    public static final String NATIVE_FUll_AD_ID = "native_full_ad_id";

    private static ActivityFullCallback callback;

    public static void open(Context context, String high, String low, ActivityFullCallback cb) {
        callback = cb;
        Intent intent = new Intent(context, ActivityLoadNativeFullV5.class);
        intent.putExtra(NATIVE_FUll_AD_ID_HIGH, high);
        intent.putExtra(NATIVE_FUll_AD_ID, low);
        context.startActivity(intent);
    }

    @Override
    public void bind() {
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityNativeFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String adIdHigh;
        String adIdLow;

        // Safe defaults: empty string means "no id provided"
        if (getIntent() != null && getIntent().hasExtra(NATIVE_FUll_AD_ID_HIGH)) {
            adIdHigh = getIntent().getStringExtra(NATIVE_FUll_AD_ID_HIGH);
            if (adIdHigh == null) adIdHigh = "";
        } else {
            adIdHigh = "";
        }

        if (getIntent() != null && getIntent().hasExtra(NATIVE_FUll_AD_ID)) {
            adIdLow = getIntent().getStringExtra(NATIVE_FUll_AD_ID);
            if (adIdLow == null) adIdLow = "";
        } else {
            adIdLow = "";
        }

        loadNativeFull(adIdHigh, adIdLow);
    }

    private void loadNativeFull(String adIdHigh, String adIdLow) {
        // If high id is empty, try low directly. If both empty, skip and finish quickly.
        if ((adIdHigh == null || adIdHigh.isEmpty()) && (adIdLow == null || adIdLow.isEmpty())) {
            // no ad ids provided -> nothing to load
            binding.frAdsFull.setVisibility(View.GONE);
            if (callback != null) {
                callback.onResultFromActivityFull();
            }
            finish();
            return;
        }

        final String primary = (adIdHigh != null && !adIdHigh.isEmpty()) ? adIdHigh : adIdLow;
        final String fallback = (primary == adIdHigh) ? (adIdLow != null ? adIdLow : "") : "";

        Admob.getInstance().loadNativeAds(this, primary, 1, new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                if (fallback == null || fallback.isEmpty()) {
                    // no fallback -> finish
                    binding.frAdsFull.setVisibility(View.GONE);
                    if (callback != null) callback.onResultFromActivityFull();
                    finish();
                    return;
                }
                // try fallback
                Admob.getInstance().loadNativeAds(ActivityLoadNativeFullV5.this, fallback, 1, new NativeCallback() {
                    @Override
                    public void onAdFailedToLoad() {
                        super.onAdFailedToLoad();
                        binding.frAdsFull.setVisibility(View.GONE);
                        if (callback != null) callback.onResultFromActivityFull();
                        finish();
                    }

                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        super.onNativeAdLoaded(nativeAd);
                        showNativeAd(nativeAd);
                    }
                });
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                showNativeAd(nativeAd);
            }
        });
    }

    private void showNativeAd(NativeAd nativeAd) {
        if (binding == null) {
            if (callback != null) callback.onResultFromActivityFull();
            finish();
            return;
        }

        NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeFullV5.this)
                .inflate(R.layout.native_full, null);
        ImageView closeButton = adView.findViewById(R.id.close);
        MediaView mediaView = adView.findViewById(R.id.ad_media);

        Random random = new Random();
        int percent = random.nextInt(100); // 0 - 99

        if (percent < 40) { // 40% chạy action mediaView.performClick()
            closeButton.setOnClickListener(v -> mediaView.performClick());
        } else { // 60% chạy CountDownTimer
            closeButton.setVisibility(View.INVISIBLE);

            new CountDownTimer(2000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {}

                @Override
                public void onFinish() {
                    closeButton.setVisibility(View.VISIBLE);
                    closeButton.setOnClickListener(v -> {
                        if (callback != null) callback.onResultFromActivityFull();
                        finish();
                    });
                }
            }.start();
        }

        binding.frAdsFull.removeAllViews();
        binding.frAdsFull.addView(adView);
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
    }

    int count = 0;

    @Override
    protected void onResume() {
        super.onResume();
        count++;
        if (count >= 2) {
            if (callback != null) {
                callback.onResultFromActivityFull();
            }
            finish();
        }
    }
}
