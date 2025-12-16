package com.fitness.pedometer.walkrun.stepmonitor.utils;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.base.BaseActivity;
import com.fitness.pedometer.walkrun.stepmonitor.databinding.ActivityNativeFullBinding;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SystemConfiguration;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;


public class LoadNativeFullNew extends BaseActivity {
    ActivityNativeFullBinding binding;
    public static final String EXTRA_NATIVE_AD_ID = "extra_native_ad_id";
    private CountDownTimer countDownTimer;
    private ValueAnimator animator;
    private boolean isAdClicked = false;
    private boolean isTimerFinished = false;

    @Override
    public void bind() {
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityNativeFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String adId = "";

        if (getIntent() != null && getIntent().hasExtra(EXTRA_NATIVE_AD_ID)) {
            adId = getIntent().getStringExtra(EXTRA_NATIVE_AD_ID);
            if (adId == null) adId = "";
        } else {
            // safe default: no ad id provided
            adId = "";
        }

        // If no ad id provided, nothing to load -> finish quickly
        if (adId.isEmpty()) {
            binding.frAdsFull.setVisibility(View.GONE);
            finish();
            return;
        }

        loadNativeFull(adId);
    }

    private void loadNativeFull(String adId) {
        // defensive: ensure adId not null/empty
        if (adId == null || adId.trim().isEmpty()) {
            binding.frAdsFull.setVisibility(View.GONE);
            finish();
            return;
        }

        Admob.getInstance().loadNativeAds(this, adId, 1, new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                // show/hide container consistently then finish
                if (binding != null && binding.frAdsFull != null) {
                    binding.frAdsFull.setVisibility(View.GONE);
                }
                finish();
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                if (binding == null) {
                    if (callbackExists()) finish();
                    return;
                }

                NativeAdView adView = (NativeAdView) LayoutInflater.from(LoadNativeFullNew.this)
                        .inflate(R.layout.layout_native_full_new, null);
                ImageView closeButton = adView.findViewById(R.id.close);
                @SuppressLint("CutPasteId") TextView tvCountdown = adView.findViewById(R.id.tvCountdown);
                @SuppressLint("CutPasteId") CountdownView progressStroke = adView.findViewById(R.id.progressStroke);
                MediaView mediaView = adView.findViewById(R.id.ad_media);
                FrameLayout frCountDown = adView.findViewById(R.id.frCountdown);

                // default visibility
                frCountDown.setVisibility(View.GONE);
                closeButton.setVisibility(View.VISIBLE);

                isAdClicked = false;
                isTimerFinished = false;

                View.OnClickListener adClickListener = v -> {
                    isAdClicked = true;
                    stopCountdown();
                };

                mediaView.setOnClickListener(adClickListener);

                // start countdown to allow close after 5s (same behavior as original)
                countDownTimer = new CountDownTimer(5000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        // can update UI if needed using tvCountdown/progressStroke
                    }

                    public void onFinish() {
                        isTimerFinished = true;
                    }
                };
                countDownTimer.start();

                closeButton.setOnClickListener(v -> {
                    if (!isTimerFinished && !isAdClicked) {
                        // force click ad if user tries to close too early
                        mediaView.performClick();
                    } else {
                        finish();
                    }
                });

                binding.frAdsFull.removeAllViews();
                binding.frAdsFull.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
    }

    private boolean callbackExists() {
        // original class had no callback field; keep compatibility
        // if you used a static callback in other implementations, handle here
        return false;
    }

    private void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if (animator != null) {
            animator.end();
            animator = null;
        }
    }

    private int isLoadNativeFullNew = 0;

    @Override
    protected void onResume() {
        super.onResume();
        isLoadNativeFullNew++;
        if (isLoadNativeFullNew >= 2) {
            finish();
        }
    }
}
