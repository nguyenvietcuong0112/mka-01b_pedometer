package com.fitness.pedometer.walkrun.stepmonitor.notiSpecial;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.util.Admob;
import com.fitness.pedometer.walkrun.stepmonitor.R;

public class FullScreenAdManager {

    private static FullScreenAdManager instance;
    private InterstitialAd interstitialAd;
    private boolean isLoading;

    private FullScreenAdManager() {}

    public static synchronized FullScreenAdManager getInstance() {
        if (instance == null) {
            instance = new FullScreenAdManager();
        }
        return instance;
    }

    public boolean isAdReady() {
        return interstitialAd != null;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void loadAd(Context context) {
        if (isLoading || interstitialAd != null) {
            Log.d("minh", "Skip load: already loading or has ad");
            return;
        }

        isLoading = true;
        Log.d("minh", "Loading InterstitialAd...");

        Admob.getInstance().loadInterAds(context,
                context.getString(R.string.inter_home),
                new InterCallback() {
                    @Override
                    public void onInterstitialLoad(InterstitialAd ad) {
                        super.onInterstitialLoad(ad);
                        interstitialAd = ad;
                        isLoading = false;
//                        FirebaseAnalytics.getInstance(context).logEvent("event_show_noti_full_screen", null);
                        Log.d("minh", "Inter HIGH loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        super.onAdFailedToLoad(error);
                        Log.d("minh", "HIGH failed, try normal");
                        loadBackupInter(context);
                    }
                });
    }

    private void loadBackupInter(Context context) {
        Admob.getInstance().loadInterAds(context,
                context.getString(R.string.inter_home),
                new InterCallback() {
                    @Override
                    public void onInterstitialLoad(InterstitialAd ad) {
                        super.onInterstitialLoad(ad);
                        interstitialAd = ad;
                        isLoading = false;
                        Log.d("minh", "Inter NORMAL loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        super.onAdFailedToLoad(error);
                        interstitialAd = null;
                        isLoading = false;
                        Log.d("minh", "All inter load failed");
                    }
                });
    }

    public void showAd(Context context, InterCallback callback) {
        if (interstitialAd == null) {
            Log.d("minh", "showAd: interstitialAd null");
            if (callback != null) callback.onAdFailedToLoad(null);
            return;
        }
        FirebaseAnalytics.getInstance(context).logEvent("event_show_full_screen_final", null);

        Admob.getInstance().showInterAds(context, interstitialAd, new InterCallback() {
            @Override
            public void onAdClosedByUser() {
                interstitialAd = null;
                isLoading = false;
                if (callback != null) callback.onAdClosedByUser();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                interstitialAd = null;
                isLoading = false;
                if (callback != null) callback.onAdFailedToLoad(error);
            }
        });
    }
}
