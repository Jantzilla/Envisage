package com.apps.creativesource.envisage;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateUtils;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import net.sqlcipher.database.SQLiteDatabase;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class EventService extends JobService {
    private final String CHANNEL_ID = "Envisage";
    private String selection;
    private long eventId;
    private int eventFreqId, usedCount;
    private long startTime, endTime;
    private String eventDescription;
    private String[] selectionArgs;
    private SQLiteDatabase db;
    private EnvisageDBHelper dbHelper;
    private int threshold = 30000; // time limit for job scheduler

    @Override
    public boolean onStartJob(JobParameters params) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel(this);

        eventId = params.getExtras().getLong("eventId");
        eventFreqId = params.getExtras().getInt("eventFreqId");
        startTime = params.getExtras().getLong("startTime");
        endTime = params.getExtras().getLong("endTime");
        usedCount = params.getExtras().getInt("usedCount");
        eventDescription = params.getExtras().getString("eventDescription");

        Intent contentIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingContentIntent = PendingIntent.getActivity(this, 0 /* Request code */, contentIntent,
                PendingIntent.FLAG_ONE_SHOT);

        // Dismiss Reminder Behavior
        Intent deleteIntent = new Intent(this, AlarmReceiver.class);
        deleteIntent.putExtra("broadcastType", "dismiss");
        deleteIntent.putExtra("eventId", String.valueOf(eventId));
        PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(this, 1 /* Request code */, deleteIntent,
                PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Action dismissAction =
                new NotificationCompat.Action.Builder(
                        0, "Dismiss", pendingDeleteIntent
                ).build();

        // Reschedule Reminder Behavior
        Intent rescheduleIntent = new Intent(this, AlarmReceiver.class);
        rescheduleIntent.putExtra("broadcastType", "reschedule");
        rescheduleIntent.putExtra("eventId", String.valueOf(eventId));
        rescheduleIntent.putExtra("eventFreqId", String.valueOf(eventFreqId));
        rescheduleIntent.putExtra("startTime", String.valueOf(startTime));
        rescheduleIntent.putExtra("endTime", String.valueOf(endTime));
        rescheduleIntent.putExtra("usedCount", String.valueOf(usedCount));
        rescheduleIntent.putExtra("eventDescription", eventDescription);
        PendingIntent pendingRescheduleIntent = PendingIntent.getBroadcast(this, 2 /* Request code */, rescheduleIntent,
                PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Action rescheduleAction =
                new NotificationCompat.Action.Builder(
                        0, "Reschedule", pendingRescheduleIntent
                ).build();

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Reminder")
                .setContentText(eventDescription)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingContentIntent)
                .setDeleteIntent(pendingDeleteIntent)
                .addAction(dismissAction)
                .addAction(rescheduleAction);

        notificationManager.notify((int) eventId /* ID of notification */, notificationBuilder.build());

        long interval = 0L;

        switch(eventFreqId) {
            case 0:
                stop(this);

                Intent local = new Intent();

                local.setAction("events.update.action");

                this.sendBroadcast(local);

                return true;
            case 1:
                interval = DateUtils.DAY_IN_MILLIS; // DAILY
                break;
            case 2:
                interval = DateUtils.WEEK_IN_MILLIS; // WEEKLY
                break;
            case 3:
                interval = DateUtils.WEEK_IN_MILLIS * 2; // BI-WEEKLY
                break;
            case 4:
                interval = DateUtils.YEAR_IN_MILLIS / 12; // MONTHLY
                break;
            case 5:
                interval = DateUtils.YEAR_IN_MILLIS; // YEARLY
                break;
        }

        SQLiteDatabase.loadLibs(this);
        dbHelper = new EnvisageDBHelper(this);
        db = dbHelper.getWritableDatabase(EnvisageDBHelper.DATABASE_PASSWORD);

        ContentValues values = new ContentValues();
        String selection = EnvisageContract.Events._ID + " = ?";
        String[] selectionArgs = new String[] {String.valueOf(eventId)};
        values.put(EnvisageContract.Events.COLUMN_DESCRIPTION, eventDescription);
        values.put(EnvisageContract.Events.COLUMN_START_TIME, startTime + interval);
        values.put(EnvisageContract.Events.COLUMN_TYPE, eventFreqId);
        db.update(
                EnvisageContract.Events.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        values = new ContentValues();
        values.put(EnvisageContract.Events.COLUMN_START_TIME, startTime);
        values.put(EnvisageContract.History.COLUMN_DURATION, endTime - startTime);
        values.put(EnvisageContract.History.COLUMN_EVENT, eventId);

        db.insert(EnvisageContract.History.TABLE_NAME, null, values);

        JobScheduler jobScheduler = (JobScheduler)getApplicationContext() .getSystemService(JOB_SCHEDULER_SERVICE);

        jobScheduler.cancel((int) eventId);

        ComponentName serviceComponent = new ComponentName(this, EventService.class);
        JobInfo.Builder builder = new JobInfo.Builder((int) eventId, serviceComponent);
        builder.setPersisted(true);
        builder.setMinimumLatency(startTime + interval - (System.currentTimeMillis())); // wait at least
        builder.setOverrideDeadline(startTime + interval - (System.currentTimeMillis() - threshold)); // maximum delay

        jobScheduler.schedule(builder.build());

        Intent local = new Intent();

        local.setAction("events.update.action");

        this.sendBroadcast(local);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        return true;
    }

    public void stop(Context context) {

        SQLiteDatabase.loadLibs(context);
        dbHelper = new EnvisageDBHelper(context);
        db = dbHelper.getWritableDatabase(EnvisageDBHelper.DATABASE_PASSWORD);

        selection = EnvisageContract.Events._ID + " = ?";
        selectionArgs = new String[] {String.valueOf(eventId)};

        ContentValues values = new ContentValues();
        values.put(EnvisageContract.Events.COLUMN_SET, 0);
        values.put(EnvisageContract.Events.COLUMN_USED_COUNT, usedCount++);
        db.update(
                EnvisageContract.Events.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        values = new ContentValues();
        values.put(EnvisageContract.Events.COLUMN_START_TIME, startTime);
        values.put(EnvisageContract.History.COLUMN_DURATION, endTime - startTime);
        values.put(EnvisageContract.History.COLUMN_EVENT, eventId);

        db.insert(EnvisageContract.History.TABLE_NAME, null, values);

        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel((int) eventId);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}