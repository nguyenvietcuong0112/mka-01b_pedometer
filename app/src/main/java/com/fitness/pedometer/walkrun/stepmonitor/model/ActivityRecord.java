package com.fitness.pedometer.walkrun.stepmonitor.model;

public class ActivityRecord {
    private long id;
    private long timestamp;
    private int steps;
    private double calories;
    private long durationMillis;
    private double distanceKm;
    private String activityType;

    public ActivityRecord() {
    }

    public ActivityRecord(long timestamp, int steps, double calories, long durationMillis, double distanceKm) {
        this.timestamp = timestamp;
        this.steps = steps;
        this.calories = calories;
        this.durationMillis = durationMillis;
        this.distanceKm = distanceKm;
        this.activityType = "Biking";
    }

    // Getters
    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getSteps() {
        return steps;
    }

    public double getCalories() {
        return calories;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public String getActivityType() {
        return activityType;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
}