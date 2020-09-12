package com.apps.creativesource.envisage;

import android.text.format.DateUtils;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;

public class ClusteringTest {
    float timeTotal, durationTotal;
    long startTime1, startTime2, startTime3, endTime1, endTime2, endTime3;
    ArrayList<Event> historyArg, eventArg, actualEvents, expectedEvents;
    Calendar calendar;

    @Test
    public void testCentroidCalculation() {
        // Set test time
        startTime1 = System.currentTimeMillis();
        startTime2 = startTime1 + DateUtils.MINUTE_IN_MILLIS * 15;
        startTime3 = startTime1 + DateUtils.MINUTE_IN_MILLIS * 45;

        endTime1 = startTime1 + DateUtils.HOUR_IN_MILLIS;
        endTime2 = startTime1 + DateUtils.HOUR_IN_MILLIS * 2;
        endTime3 = startTime1 + DateUtils.HOUR_IN_MILLIS / 2;

        // Utility method arguments
        historyArg = new ArrayList<>();
        historyArg.add(new Event(1, "past event", startTime1, endTime1, 1, 0, 0));
        historyArg.add(new Event(1, "past event", startTime2, endTime2, 1, 0, 0));
        historyArg.add(new Event(1, "past event", startTime3, endTime3, 1, 0, 0));

        eventArg = new ArrayList<>();
        eventArg.add(new Event(1, "reference event", startTime1, endTime1, 1, 0, 3));

        // Sum time and duration for average cluster distance calculation
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(historyArg.get(0).startTime);
        timeTotal += calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f;
        durationTotal += (float) historyArg.get(0).endTime / (DateUtils.HOUR_IN_MILLIS);
        calendar.setTimeInMillis(historyArg.get(1).startTime);
        timeTotal += calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f;
        durationTotal += (float) historyArg.get(1).endTime / (DateUtils.HOUR_IN_MILLIS);
        calendar.setTimeInMillis(historyArg.get(2).startTime);
        timeTotal += calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f;
        durationTotal += (float) historyArg.get(2).endTime / (DateUtils.HOUR_IN_MILLIS);

        // Expected Events list result
        expectedEvents = new ArrayList<>();
        expectedEvents.add(eventArg.get(0));
        calendar.setTimeInMillis(eventArg.get(0).startTime);
        calendar.set(Calendar.HOUR_OF_DAY, (int) (timeTotal / 3));
        expectedEvents.get(0).startTime = calendar.getTimeInMillis();
        expectedEvents.get(0).endTime = (long) (expectedEvents.get(0).startTime + DateUtils.HOUR_IN_MILLIS * (durationTotal / 3));

        // Actual Events list result
        actualEvents = ClusteringUtils.getCentroids(historyArg, eventArg);

        // Test for accurate cluster centroid calculation
        assertEquals(expectedEvents.get(0).startTime, actualEvents.get(0).startTime);
        assertEquals(expectedEvents.get(0).endTime, actualEvents.get(0).endTime);
    }

}