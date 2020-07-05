package com.cubenama.cubingcompanion.competitionui;

import com.google.firebase.Timestamp;

public class EventRound {

    public String roundId;
    public String eventName;
    public long qualificationCriteria;
    public Timestamp startTimestamp;
    public Timestamp endTimestamp;

    public EventRound(long qualificationCriteria, Timestamp startTimestamp, Timestamp endTimestamp)
    {
        this.qualificationCriteria = qualificationCriteria;
        this.startTimestamp = startTimestamp;
        this.endTimestamp= endTimestamp;
    }

    public EventRound(String roundId, long qualificationCriteria, Timestamp startTimestamp, Timestamp endTimestamp)
    {
        this.roundId = roundId;
        this.qualificationCriteria = qualificationCriteria;
        this.startTimestamp = startTimestamp;
        this.endTimestamp= endTimestamp;
    }

    public EventRound(String eventName, String roundId, long qualificationCriteria, Timestamp startTimestamp, Timestamp endTimestamp)
    {
        this.eventName= eventName;
        this.roundId = roundId;
        this.qualificationCriteria = qualificationCriteria;
        this.startTimestamp = startTimestamp;
        this.endTimestamp= endTimestamp;
    }
}
