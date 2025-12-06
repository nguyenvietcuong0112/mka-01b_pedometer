package com.fitness.pedometer.walkrun.stepmonitor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.pedometer.walkrun.stepmonitor.R;
import com.fitness.pedometer.walkrun.stepmonitor.model.ActivityRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityHistoryAdapter extends RecyclerView.Adapter<ActivityHistoryAdapter.ViewHolder> {

    private Context context;
    private List<ActivityRecord> activities = new ArrayList<>();

    public ActivityHistoryAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<ActivityRecord> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityRecord record = activities.get(position);

        // Format date: "06:06 - Nov 21,2025"
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - MMM dd,yyyy", Locale.getDefault());
        String dateStr = dateFormat.format(new Date(record.getTimestamp()));

        holder.tvDateTime.setText(dateStr);
        holder.tvSteps.setText(String.valueOf(record.getSteps()));
        holder.tvKcal.setText(String.format(Locale.getDefault(), "%.2f", record.getCalories()));
        holder.tvDuration.setText(formatTime(record.getDurationMillis()));
        holder.tvDistance.setText(String.format(Locale.getDefault(), "%.2f", record.getDistanceKm()));
    }

    @Override
    public int getItemCount() {
        return activities.size();
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateTime, tvSteps, tvKcal, tvDuration, tvDistance;

        ViewHolder(View itemView) {
            super(itemView);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvSteps = itemView.findViewById(R.id.tvSteps);
            tvKcal = itemView.findViewById(R.id.tvKcal);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }
}