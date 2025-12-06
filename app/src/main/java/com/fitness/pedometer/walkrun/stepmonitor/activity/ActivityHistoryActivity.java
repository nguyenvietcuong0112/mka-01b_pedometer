package com.fitness.pedometer.walkrun.stepmonitor.activity;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.adapter.ActivityHistoryAdapter;
import com.fitness.pedometer.walkrun.stepmonitor.model.ActivityRecord;
import com.fitness.pedometer.walkrun.stepmonitor.model.DatabaseHelper;

import java.util.List;

public class ActivityHistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTotalSteps, tvTotalKcal, tvTotalMin, tvTotalKm;
    private RecyclerView recyclerView;
    private ActivityHistoryAdapter adapter;
    private DatabaseHelper databaseHelper;

    private FrameLayout frAdsBanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        setupRecyclerView();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalSteps = findViewById(R.id.tvTotalSteps);
        tvTotalKcal = findViewById(R.id.tvTotalKcal);
        tvTotalMin = findViewById(R.id.tvTotalMin);
        tvTotalKm = findViewById(R.id.tvTotalKm);
        recyclerView = findViewById(R.id.recyclerViewHistory);
        frAdsBanner = findViewById(R.id.fr_banner);

        btnBack.setOnClickListener(v -> finish());

        databaseHelper = new DatabaseHelper(this);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActivityHistoryAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        // Load danh sách các hoạt động từ database
        List<ActivityRecord> activities = databaseHelper.getAllActivities();

        if (activities != null && !activities.isEmpty()) {
            adapter.setData(activities);
            calculateTotals(activities);
        } else {
            // Hiển thị trạng thái rỗng
            tvTotalSteps.setText("--");
            tvTotalKcal.setText("--");
            tvTotalMin.setText("--");
            tvTotalKm.setText("--");
        }
    }

    private void calculateTotals(List<ActivityRecord> activities) {
        int totalSteps = 0;
        double totalKcal = 0;
        long totalMillis = 0;
        double totalKm = 0;

        for (ActivityRecord record : activities) {
            totalSteps += record.getSteps();
            totalKcal += record.getCalories();
            totalMillis += record.getDurationMillis();
            totalKm += record.getDistanceKm();
        }

        tvTotalSteps.setText(String.valueOf(totalSteps));
        tvTotalKcal.setText(String.format("%.2f", totalKcal));

        // Format time
        long totalMinutes = totalMillis / 60000;
        tvTotalMin.setText(formatTime(totalMillis));

        tvTotalKm.setText(String.format("%.2f", totalKm));
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;

        if (h > 0) {
            return String.format("%02d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Refresh data khi quay lại màn hình
        loadAdsBanner();
    }

    private void loadAdsBanner() {
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_banner_history), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityHistoryActivity.this).inflate(R.layout.ad_native_admob_banner_1, null);
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

    }
}