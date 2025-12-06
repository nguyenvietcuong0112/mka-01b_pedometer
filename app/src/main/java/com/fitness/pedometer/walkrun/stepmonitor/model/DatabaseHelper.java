package com.fitness.pedometer.walkrun.stepmonitor.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pedometer.db";
    private static final int DATABASE_VERSION = 3; // Tăng version lên 3
    private static final String TABLE_NAME = "steps";

    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_STEPS = "steps";
    private static final String COLUMN_GOAL = "goal";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_TIME = "time";

    // Table Activities
    private static final String TABLE_ACTIVITIES = "activities";
    private static final String COL_ACTIVITY_ID = "id";
    private static final String COL_ACTIVITY_TIMESTAMP = "timestamp";
    private static final String COL_ACTIVITY_STEPS = "steps";
    private static final String COL_ACTIVITY_CALORIES = "calories";
    private static final String COL_ACTIVITY_DURATION = "duration";
    private static final String COL_ACTIVITY_DISTANCE = "distance";
    private static final String COL_ACTIVITY_TYPE = "activity_type";

    private static final double KCAL_PER_STEP_DEFAULT = 0.04;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create steps table
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_DATE + " TEXT PRIMARY KEY, "
                + COLUMN_STEPS + " INTEGER, "
                + COLUMN_GOAL + " INTEGER, "
                + COLUMN_CALORIES + " REAL, "
                + COLUMN_DISTANCE + " REAL, "
                + COLUMN_TIME + " INTEGER);";
        db.execSQL(createTable);

        // Create activities table
        String createActivitiesTable = "CREATE TABLE " + TABLE_ACTIVITIES + "("
                + COL_ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_ACTIVITY_TIMESTAMP + " INTEGER,"
                + COL_ACTIVITY_STEPS + " INTEGER,"
                + COL_ACTIVITY_CALORIES + " REAL,"
                + COL_ACTIVITY_DURATION + " INTEGER,"
                + COL_ACTIVITY_DISTANCE + " REAL,"
                + COL_ACTIVITY_TYPE + " TEXT"
                + ")";
        db.execSQL(createActivitiesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Tạo table activities nếu chưa có
            String createActivitiesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_ACTIVITIES + "("
                    + COL_ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_ACTIVITY_TIMESTAMP + " INTEGER,"
                    + COL_ACTIVITY_STEPS + " INTEGER,"
                    + COL_ACTIVITY_CALORIES + " REAL,"
                    + COL_ACTIVITY_DURATION + " INTEGER,"
                    + COL_ACTIVITY_DISTANCE + " REAL,"
                    + COL_ACTIVITY_TYPE + " TEXT"
                    + ")";
            db.execSQL(createActivitiesTable);
        }
    }

    public void saveStepData(int steps, int goal, double calories, double distance, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_DATE, getCurrentDate());
        values.put(COLUMN_STEPS, steps);
        values.put(COLUMN_GOAL, goal);
        values.put(COLUMN_CALORIES, calories);
        values.put(COLUMN_DISTANCE, distance);
        values.put(COLUMN_TIME, time);

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void addToToday(int steps, double calories, double distance, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getCurrentDate();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_STEPS, COLUMN_GOAL, COLUMN_CALORIES, COLUMN_DISTANCE, COLUMN_TIME},
                COLUMN_DATE + "=?",
                new String[]{today},
                null, null, null);

        ContentValues values = new ContentValues();

        if (cursor != null && cursor.moveToFirst()) {
            int currentSteps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS));
            int currentGoal = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL));
            double currentCalories = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CALORIES));
            double currentDistance = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE));
            long currentTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME));

            values.put(COLUMN_STEPS, currentSteps + steps);
            values.put(COLUMN_GOAL, currentGoal);
            values.put(COLUMN_CALORIES, currentCalories + calories);
            values.put(COLUMN_DISTANCE, currentDistance + distance);
            values.put(COLUMN_TIME, currentTime + time);

            cursor.close();

            db.update(TABLE_NAME, values, COLUMN_DATE + "=?", new String[]{today});
        } else {
            if (cursor != null) {
                cursor.close();
            }

            values.put(COLUMN_DATE, today);
            values.put(COLUMN_STEPS, steps);
            values.put(COLUMN_GOAL, 6000);
            values.put(COLUMN_CALORIES, calories);
            values.put(COLUMN_DISTANCE, distance);
            values.put(COLUMN_TIME, time);

            db.insert(TABLE_NAME, null, values);
        }

        db.close();
    }

    public void updateTodayStepsFromSensor(int stepsFromSensor) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getCurrentDate();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_STEPS, COLUMN_GOAL, COLUMN_CALORIES, COLUMN_DISTANCE, COLUMN_TIME},
                COLUMN_DATE + "=?",
                new String[]{today},
                null, null, null);

        ContentValues values = new ContentValues();
        double caloriesFromSensor = stepsFromSensor * KCAL_PER_STEP_DEFAULT;

        if (cursor != null && cursor.moveToFirst()) {
            int currentGoal = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL));
            double currentDistance = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE));
            long currentTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME));

            values.put(COLUMN_STEPS, stepsFromSensor);
            values.put(COLUMN_GOAL, currentGoal);
            values.put(COLUMN_CALORIES, caloriesFromSensor);
            values.put(COLUMN_DISTANCE, currentDistance);
            values.put(COLUMN_TIME, currentTime);

            cursor.close();
            db.update(TABLE_NAME, values, COLUMN_DATE + "=?", new String[]{today});
        } else {
            if (cursor != null) cursor.close();

            values.put(COLUMN_DATE, today);
            values.put(COLUMN_STEPS, stepsFromSensor);
            values.put(COLUMN_GOAL, 6000);
            values.put(COLUMN_CALORIES, caloriesFromSensor);
            values.put(COLUMN_DISTANCE, 0);
            values.put(COLUMN_TIME, 0);

            db.insert(TABLE_NAME, null, values);
        }

        db.close();
    }

    public StepData getStepDataForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        StepData stepData = new StepData();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_STEPS, COLUMN_GOAL, COLUMN_CALORIES, COLUMN_DISTANCE, COLUMN_TIME},
                COLUMN_DATE + "=?",
                new String[]{date},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            stepData.steps = cursor.getInt(0);
            stepData.goal = cursor.getInt(1);
            stepData.calories = cursor.getDouble(2);
            stepData.distance = cursor.getDouble(3);
            stepData.time = cursor.getLong(4);
            cursor.close();
        }

        db.close();
        return stepData;
    }

    public StepData getTodayStepData() {
        return getStepDataForDate(getCurrentDate());
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public MonthlyStepData getMonthlyStepData() {
        SQLiteDatabase db = this.getReadableDatabase();
        MonthlyStepData monthlyData = new MonthlyStepData();

        String query = "SELECT SUM(steps) as total_steps, SUM(calories) as total_calories, " +
                "SUM(distance) as total_distance, SUM(time) as total_time " +
                "FROM steps WHERE date >= date('now', 'start of month')";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            int totalStepsIndex = cursor.getColumnIndex("total_steps");
            int totalCaloriesIndex = cursor.getColumnIndex("total_calories");
            int totalDistanceIndex = cursor.getColumnIndex("total_distance");
            int totalTimeIndex = cursor.getColumnIndex("total_time");

            if (totalStepsIndex != -1 && totalCaloriesIndex != -1 &&
                    totalDistanceIndex != -1 && totalTimeIndex != -1) {
                monthlyData.totalSteps = cursor.getInt(totalStepsIndex);
                monthlyData.totalCalories = cursor.getDouble(totalCaloriesIndex);
                monthlyData.totalDistance = cursor.getDouble(totalDistanceIndex);
                monthlyData.totalTime = cursor.getLong(totalTimeIndex);
            }
        }
        cursor.close();
        return monthlyData;
    }

    public List<DailyStepData> getLast30DaysData() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<DailyStepData> dailyData = new ArrayList<>();

        String query = "SELECT date, steps, calories, distance FROM steps WHERE date >= date('now', '-30 days') ORDER BY date ASC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            int dateColumnIndex = cursor.getColumnIndex("date");
            int stepsColumnIndex = cursor.getColumnIndex("steps");
            int caloriesColumnIndex = cursor.getColumnIndex("calories");
            int distanceColumnIndex = cursor.getColumnIndex("distance");

            if (dateColumnIndex != -1 && stepsColumnIndex != -1 &&
                    caloriesColumnIndex != -1 && distanceColumnIndex != -1) {
                do {
                    DailyStepData data = new DailyStepData();
                    data.date = cursor.getString(dateColumnIndex);
                    data.steps = cursor.getInt(stepsColumnIndex);
                    data.calories = cursor.getFloat(caloriesColumnIndex);
                    data.distance = cursor.getFloat(distanceColumnIndex);
                    dailyData.add(data);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        return dailyData;
    }

    public List<DailyStepData> getMonthData(int monthIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<DailyStepData> dailyData = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        Calendar startCal = Calendar.getInstance();
        startCal.set(currentYear, monthIndex, 1, 0, 0, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(currentYear, monthIndex, startCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(startCal.getTime());
        String endDate = sdf.format(endCal.getTime());

        String query = "SELECT date, steps, calories, distance FROM " + TABLE_NAME +
                " WHERE date >= ? AND date <= ? ORDER BY date ASC";

        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
        if (cursor.moveToFirst()) {
            int dateColumnIndex = cursor.getColumnIndex("date");
            int stepsColumnIndex = cursor.getColumnIndex("steps");
            int caloriesColumnIndex = cursor.getColumnIndex("calories");
            int distanceColumnIndex = cursor.getColumnIndex("distance");

            if (dateColumnIndex != -1 && stepsColumnIndex != -1 &&
                    caloriesColumnIndex != -1 && distanceColumnIndex != -1) {
                do {
                    DailyStepData data = new DailyStepData();
                    data.date = cursor.getString(dateColumnIndex);
                    data.steps = cursor.getInt(stepsColumnIndex);
                    data.calories = cursor.getFloat(caloriesColumnIndex);
                    data.distance = cursor.getFloat(distanceColumnIndex);
                    dailyData.add(data);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        return dailyData;
    }

    public MonthlyStepData getMonthlyStatData(int monthIndex, int statisticType) {
        SQLiteDatabase db = this.getReadableDatabase();
        MonthlyStepData monthlyData = new MonthlyStepData();

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        Calendar startCal = Calendar.getInstance();
        startCal.set(currentYear, monthIndex, 1, 0, 0, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(currentYear, monthIndex, startCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(startCal.getTime());
        String endDate = sdf.format(endCal.getTime());

        String query = "SELECT SUM(steps) as total_steps, SUM(calories) as total_calories, " +
                "SUM(distance) as total_distance, SUM(time) as total_time " +
                "FROM " + TABLE_NAME + " WHERE date >= ? AND date <= ?";

        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
        if (cursor.moveToFirst()) {
            int totalStepsIndex = cursor.getColumnIndex("total_steps");
            int totalCaloriesIndex = cursor.getColumnIndex("total_calories");
            int totalDistanceIndex = cursor.getColumnIndex("total_distance");
            int totalTimeIndex = cursor.getColumnIndex("total_time");

            if (totalStepsIndex != -1 && totalCaloriesIndex != -1 &&
                    totalDistanceIndex != -1 && totalTimeIndex != -1) {
                monthlyData.totalSteps = cursor.getInt(totalStepsIndex);
                monthlyData.totalCalories = cursor.getDouble(totalCaloriesIndex);
                monthlyData.totalDistance = cursor.getDouble(totalDistanceIndex);
                monthlyData.totalTime = cursor.getLong(totalTimeIndex);
            }
        }
        cursor.close();
        return monthlyData;
    }

    // ==================== ACTIVITY HISTORY METHODS ====================

    /**
     * Lưu một activity mới vào database
     * @param steps Số bước
     * @param calories Lượng calories
     * @param durationMillis Thời gian (milliseconds)
     * @param distanceMeters Khoảng cách (meters)
     * @return ID của activity vừa lưu
     */
    public long saveActivity(int steps, double calories, long durationMillis, double distanceMeters) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_ACTIVITY_TIMESTAMP, System.currentTimeMillis());
        values.put(COL_ACTIVITY_STEPS, steps);
        values.put(COL_ACTIVITY_CALORIES, calories);
        values.put(COL_ACTIVITY_DURATION, durationMillis);
        values.put(COL_ACTIVITY_DISTANCE, distanceMeters / 1000.0); // Convert to km
        values.put(COL_ACTIVITY_TYPE, "Biking");

        long id = db.insert(TABLE_ACTIVITIES, null, values);
        db.close();
        return id;
    }

    /**
     * Lấy tất cả activities (sắp xếp mới nhất trước)
     * @return Danh sách ActivityRecord
     */
    public List<ActivityRecord> getAllActivities() {
        List<ActivityRecord> activities = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_ACTIVITIES
                + " ORDER BY " + COL_ACTIVITY_TIMESTAMP + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                ActivityRecord record = new ActivityRecord();
                record.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ACTIVITY_ID)));
                record.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ACTIVITY_TIMESTAMP)));
                record.setSteps(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ACTIVITY_STEPS)));
                record.setCalories(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ACTIVITY_CALORIES)));
                record.setDurationMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ACTIVITY_DURATION)));
                record.setDistanceKm(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ACTIVITY_DISTANCE)));
                record.setActivityType(cursor.getString(cursor.getColumnIndexOrThrow(COL_ACTIVITY_TYPE)));

                activities.add(record);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return activities;
    }

    /**
     * Xóa một activity theo ID
     * @param id ID của activity cần xóa
     */
    public void deleteActivity(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACTIVITIES, COL_ACTIVITY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * Lấy số lượng activities
     * @return Tổng số activities
     */
    public int getActivityCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_ACTIVITIES;
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // ==================== INNER CLASSES ====================

    public static class StepData {
        public int steps = 0;
        public int goal = 6000;
        public double calories = 0;
        public double distance = 0;
        public long time = 0;
    }

    public static class MonthlyStepData {
        public int totalSteps;
        public double totalCalories;
        public double totalDistance;
        public long totalTime;
    }

    public static class DailyStepData {
        public String date;
        public int steps;
        public float calories;
        public float distance;
    }
}