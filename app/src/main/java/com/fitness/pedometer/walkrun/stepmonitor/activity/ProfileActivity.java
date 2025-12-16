package com.fitness.pedometer.walkrun.stepmonitor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.fragment.ProfileGenderFragment;
import com.fitness.pedometer.walkrun.stepmonitor.fragment.ProfileInfoFragment;
import com.fitness.pedometer.walkrun.stepmonitor.utils.ProfileDataManager;
import com.fitness.pedometer.walkrun.stepmonitor.utils.SharePreferenceUtils;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;

public class ProfileActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ProfilePagerAdapter adapter;

    public boolean isEditMode() {
        return isEditMode;
    }

    private boolean isEditMode = false;
    private FrameLayout frAds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        isEditMode = getIntent().getBooleanExtra("edit_mode", false);

        if (!isEditMode) {
            if (ProfileDataManager.isProfileCompleted(this)) {
                goToMain();
                return;
            }
        }

        viewPager = findViewById(R.id.viewPager);
        adapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(isEditMode);
        frAds = findViewById(R.id.frAds);


        if (isEditMode) {
            viewPager.setCurrentItem(1, false);
        }

        viewPager.post(() -> {
            setupFragments();
            viewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    setupFragments();
                }
            });
        });
        loadAds();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewPager != null) {
            viewPager.post(() -> setupFragments());
        }
    }

    private void setupFragments() {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        
        for (Fragment fragment : fragments) {
            if (fragment instanceof ProfileGenderFragment && !fragment.isDetached()) {
                ProfileGenderFragment genderFragment = (ProfileGenderFragment) fragment;
                genderFragment.setOnGenderSelectedListener(new ProfileGenderFragment.OnGenderSelectedListener() {
                    @Override
                    public void onGenderSelected(String gender) {
                        handleGenderSelected(gender);
                    }

                    @Override
                    public void onSkip() {
                        handleGenderSkipped();
                    }
                });
            } else if (fragment instanceof ProfileInfoFragment && !fragment.isDetached()) {
                ProfileInfoFragment infoFragment = (ProfileInfoFragment) fragment;
                infoFragment.setOnInfoCompletedListener(new ProfileInfoFragment.OnInfoCompletedListener() {
                    @Override
                    public void onInfoCompleted() {
                        handleInfoCompleted();
                    }

                    @Override
                    public void onSkip() {
                        handleInfoSkipped();
                    }
                });
            }
        }
    }

    public void handleGenderSelected(String gender) {
        viewPager.setCurrentItem(1, true);
    }

    public void handleGenderSkipped() {
        ProfileDataManager.setProfileCompleted(this, true);
        goToMain();
    }

    public void handleInfoCompleted() {
        ProfileDataManager.setProfileCompleted(this, true);
        if (isEditMode) {
            finish();
        } else {
            goToMain();
        }
    }

    public void handleInfoSkipped() {
        ProfileDataManager.setProfileCompleted(this, true);
        if (isEditMode) {
            finish();
        } else {
            goToMain();
        }
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private static class ProfilePagerAdapter extends FragmentStateAdapter {

        public ProfilePagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new ProfileGenderFragment();
            } else {
                return new ProfileInfoFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
    private void loadAds() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_profile), new NativeCallback() {

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = new NativeAdView(ProfileActivity.this);
                if (!SharePreferenceUtils.isOrganic(ProfileActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(ProfileActivity.this).inflate(R.layout.layout_native_btn_top, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(ProfileActivity.this).inflate(R.layout.layout_native_btn_bottom, null);
                }
                frAds.removeAllViews();
                frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }


            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                frAds.setVisibility(View.GONE);
            }
        });
    }
}

