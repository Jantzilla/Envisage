package com.apps.creativesource.envisage;

import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;

class ClusteringUtils {

    public static ArrayList<Event> getCentroids(ArrayList<Event> historyArrayList, ArrayList<Event> eventArrayList) {
        Calendar calendar = Calendar.getInstance();
        float time;

        for(int i = 0; i < eventArrayList.size(); i++) {
            int points = 0;
            float timeTotal = 0;
            float durationTotal = 0;

            if(i == 6)
                break;

            for(int j = 0; j < historyArrayList.size(); j++) {

                if(historyArrayList.get(j).eventId == eventArrayList.get(i).eventId) {
                    calendar.setTimeInMillis(historyArrayList.get(j).startTime);
                    time = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f;

                    points++;
                    timeTotal += time;
                    durationTotal += (float) historyArrayList.get(j).endTime / (DateUtils.HOUR_IN_MILLIS);
                }

            }
            calendar.setTimeInMillis(eventArrayList.get(i).startTime);
            calendar.set(Calendar.HOUR_OF_DAY, (int) (timeTotal/points));
            eventArrayList.get(i).startTime = calendar.getTimeInMillis();

            eventArrayList.get(i).endTime = (long) (eventArrayList.get(i).startTime + DateUtils.HOUR_IN_MILLIS * (durationTotal/points));
        }

        return eventArrayList;
    }
}
