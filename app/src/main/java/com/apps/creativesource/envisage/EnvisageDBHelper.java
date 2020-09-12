package com.apps.creativesource.envisage;

import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class EnvisageDBHelper extends SQLiteOpenHelper {
    public final Context context;
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Envisage.db";
    public static final String DATABASE_PASSWORD = "e1g2a3s4i5v6n7E";

    private static final String SQL_CREATE_EVENTS =
            "CREATE TABLE " + EnvisageContract.Events.TABLE_NAME + " (" +
                    EnvisageContract.Events._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    EnvisageContract.Events.COLUMN_DESCRIPTION + " TEXT," +
                    EnvisageContract.Events.COLUMN_START_TIME + " INTEGER DEFAULT 0," +
                    EnvisageContract.Events.COLUMN_END_TIME + " INTEGER DEFAULT 0," +
                    EnvisageContract.Events.COLUMN_TYPE + " INTEGER DEFAULT 0," +
                    EnvisageContract.Events.COLUMN_USED_COUNT + " INTEGER DEFAULT 0," +
                    EnvisageContract.Events.COLUMN_SET + " INTEGER DEFAULT 0)";

    private static final String SQL_CREATE_HISTORY =
            "CREATE TABLE " + EnvisageContract.History.TABLE_NAME + " (" +
                    EnvisageContract.History._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    EnvisageContract.Events.COLUMN_START_TIME + " INTEGER DEFAULT 0," +
                    EnvisageContract.History.COLUMN_DURATION + " INTEGER DEFAULT 0," +
                    EnvisageContract.History.COLUMN_EVENT + " INTEGER," +
                    " FOREIGN KEY ("+EnvisageContract.History.COLUMN_EVENT+") REFERENCES "
                    + EnvisageContract.Events.TABLE_NAME + "(" + EnvisageContract.Events._ID + "));";

    private static final String SQL_DELETE_EVENTS =
            "DROP TABLE IF EXISTS " + EnvisageContract.Events.TABLE_NAME;

    private static final String SQL_DELETE_HISTORY =
            "DROP TABLE IF EXISTS " + EnvisageContract.History.TABLE_NAME;

    public EnvisageDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EVENTS);
        db.execSQL(SQL_CREATE_HISTORY);
        addDefaultTrends(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_EVENTS);
        db.execSQL(SQL_DELETE_HISTORY);
        onCreate(db);
    }

    private void addDefaultTrends(SQLiteDatabase db){
        ArrayList<Event> defaultEvents = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        long endTime;

        // TODO immediate next occurrence of 5:30am
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 30);
        if(calendar.getTimeInMillis() <= System.currentTimeMillis())
            calendar.add(Calendar.DATE, 1);

        // Add default events with randomly generated durations
        endTime = calendar.getTimeInMillis() + ((DateUtils.MINUTE_IN_MILLIS * 15) * (int)(Math.random() * (5 - 1) + 1) + 1);
        defaultEvents.add(new Event(1, "Wake up!", calendar.getTimeInMillis(), endTime, 1, 0, 10));

        // TODO immediate next occurrence of Wednesday 2:45pm
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 45);
        if(calendar.getTimeInMillis() <= System.currentTimeMillis())
            calendar.add(Calendar.DATE, 7);

        // Add default events with randomly generated durations
        endTime = calendar.getTimeInMillis() + DateUtils.HOUR_IN_MILLIS * 2 + ((DateUtils.MINUTE_IN_MILLIS * 15) * (int)(Math.random() * (5 - 1) + 1) + 1);
        defaultEvents.add(new Event(2, "Go to gym", calendar.getTimeInMillis(), endTime, 2, 0, 10));

        // TODO immediate next occurrence of Sunday 9:30am
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 30);
        if(calendar.getTimeInMillis() <= System.currentTimeMillis())
            calendar.add(Calendar.DATE, 7);

        // Add default events with randomly generated durations
        endTime = calendar.getTimeInMillis() + DateUtils.HOUR_IN_MILLIS / 2 + ((DateUtils.MINUTE_IN_MILLIS * 15) * (int)(Math.random() * (5 - 1) + 1) + 1);
        defaultEvents.add(new Event(3, "Go to grocery store", calendar.getTimeInMillis(), endTime, 2, 0, 10));

        // TODO immediate next occurrence of Saturday 12:30pm
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 30);
        if(calendar.getTimeInMillis() <= System.currentTimeMillis())
            calendar.add(Calendar.DATE, 7);

        // Add default events with randomly generated durations
        endTime = calendar.getTimeInMillis() + DateUtils.HOUR_IN_MILLIS + ((DateUtils.MINUTE_IN_MILLIS * 15) * (int)(Math.random() * (5 - 1) + 1) + 1);
        defaultEvents.add(new Event(4, "Get hair cut", calendar.getTimeInMillis(), endTime, 3, 0, 10));

        // TODO immediate next occurrence of Monday 5:30pm
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 30);
        if(calendar.getTimeInMillis() <= System.currentTimeMillis())
            calendar.add(Calendar.DATE, 7);

        // Add default events with randomly generated durations
        endTime = calendar.getTimeInMillis() + DateUtils.HOUR_IN_MILLIS * 2 + ((DateUtils.MINUTE_IN_MILLIS * 15) * (int)(Math.random() * (5 - 1) + 1) + 1);
        defaultEvents.add(new Event(5, "Get oil change", calendar.getTimeInMillis(), endTime, 4, 0, 10));

        // TODO immediate next occurrence of 2/15 8:00pm
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
        calendar.set(Calendar.DATE, 15);
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        if(calendar.getTimeInMillis() <= System.currentTimeMillis())
            calendar.add(Calendar.YEAR, 1);

        // Add default events with randomly generated durations
        endTime = calendar.getTimeInMillis() + DateUtils.HOUR_IN_MILLIS * 3 + ((DateUtils.MINUTE_IN_MILLIS * 15) * (int)(Math.random() * (5 - 1) + 1) + 1);
        defaultEvents.add(new Event(6, "Do taxes", calendar.getTimeInMillis(), endTime, 5, 0, 10));

        for(int i = 0; i < defaultEvents.size(); i++) {
            ContentValues values = new ContentValues();
            long startDifference = 0;
            long endDifference = 0;

            values.put(EnvisageContract.Events.COLUMN_DESCRIPTION, defaultEvents.get(i).eventDescription);
            values.put(EnvisageContract.Events.COLUMN_START_TIME, defaultEvents.get(i).startTime);
            values.put(EnvisageContract.Events.COLUMN_END_TIME, defaultEvents.get(i).endTime);
            values.put(EnvisageContract.Events.COLUMN_TYPE, defaultEvents.get(i).eventFreqId);
            values.put(EnvisageContract.Events.COLUMN_USED_COUNT, defaultEvents.get(i).usedCount);
            values.put(EnvisageContract.Events.COLUMN_SET, defaultEvents.get(i).isSet);

            db.insert(EnvisageContract.Events.TABLE_NAME, null, values);

            for(int j = 0; j < 10; j++) {
                values = new ContentValues();

                values.put(EnvisageContract.Events.COLUMN_START_TIME, defaultEvents.get(i).startTime + startDifference);
                values.put(EnvisageContract.History.COLUMN_DURATION, defaultEvents.get(i).endTime + endDifference - defaultEvents.get(i).startTime + startDifference);
                values.put(EnvisageContract.History.COLUMN_EVENT, defaultEvents.get(i).eventId);

                startDifference = (DateUtils.MINUTE_IN_MILLIS * 15) * (int)(Math.random() * (10 - 1) + 1) + 1;
                endDifference = startDifference + (DateUtils.MINUTE_IN_MILLIS * 15) * (int)(Math.random() * (5 - 1) + 1) + 1;

                db.insert(EnvisageContract.History.TABLE_NAME, null, values);
            }
        }
    }
}
