package com.apps.creativesource.envisage;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("onReceive()", "YES");

        String eventId, eventFreqId, startTime, endTime, usedCount, eventDescription;

        Bundle extras = intent.getExtras();
        String broadcastType = (String) extras.get("broadcastType");

        Log.d("Broadcast Type", broadcastType);

        switch (broadcastType) {
            case "dismiss":
                eventId = (String) extras.get("eventId");
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(Integer.parseInt(eventId));
                break;
            case "reschedule":
                eventFreqId = (String) extras.get("eventFreqId");
                startTime = (String) extras.get("startTime");
                endTime = (String) extras.get("endTime");
                usedCount = (String) extras.get("usedCount");
                eventId = (String) extras.get("eventId");
                eventDescription = (String) extras.get("eventDescription");

                manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(Integer.parseInt(eventId));

                Intent detailIntent = new Intent(context, DetailActivity.class);
                detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                detailIntent.putExtra("broadcastType", "reschedule");
                detailIntent.putExtra("eventId", eventId);
                detailIntent.putExtra("eventFreqId", eventFreqId);
                detailIntent.putExtra("startTime", startTime);
                detailIntent.putExtra("endTime", endTime);
                detailIntent.putExtra("usedCount", usedCount);
                detailIntent.putExtra("eventDescription", eventDescription);

                context.startActivity(detailIntent);
                break;
        }
    }
}
