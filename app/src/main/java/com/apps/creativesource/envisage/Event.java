package com.apps.creativesource.envisage;

public class Event {

    public String eventDescription;
    public int eventFreqId, isSet, usedCount;
    public long eventId, startTime, endTime;

    public Event(long eventId, String eventDescription,  long startTime, long endTime, int eventFreqId, int isSet, int usedCount) {
        this.eventId = eventId;
        this.eventDescription = eventDescription;
        this.startTime = startTime;
        this.endTime = endTime;
        this.usedCount = usedCount;
        this.eventFreqId = eventFreqId;
        this.isSet = isSet;
    }

}
