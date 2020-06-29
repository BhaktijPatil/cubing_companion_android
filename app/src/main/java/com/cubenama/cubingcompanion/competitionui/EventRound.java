package com.cubenama.cubingcompanion.competitionui;

import com.google.firebase.Timestamp;

public class EventRound {

    public String eventName;
    public long participantCount;
    public Timestamp startTimestamp;
    public Timestamp endTimestamp;

    public EventRound(String eventName, long participantCount, Timestamp startTimestamp, Timestamp endTimestamp)
    {
        this.eventName = eventName;
        this.participantCount = participantCount;
        this.startTimestamp = startTimestamp;
        this.endTimestamp= endTimestamp;
    }

    public EventRound(long participantCount, Timestamp startTimestamp, Timestamp endTimestamp)
    {
        this.participantCount = participantCount;
        this.startTimestamp = startTimestamp;
        this.endTimestamp= endTimestamp;
    }
}
