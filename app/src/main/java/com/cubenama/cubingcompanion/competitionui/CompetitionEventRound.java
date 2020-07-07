package com.cubenama.cubingcompanion.competitionui;

import com.google.firebase.Timestamp;

public class CompetitionEventRound {

    public String roundId;
    public String roundName;
    public String eventName;
    public long qualificationCriteria;
    public Timestamp startTimestamp;
    public Timestamp endTimestamp;

    public CompetitionEventRound(String eventName, String roundName, String roundId, long qualificationCriteria, Timestamp startTimestamp, Timestamp endTimestamp)
    {
        this.eventName = eventName;
        this.roundName = roundName;
        this.roundId = roundId;
        this.qualificationCriteria = qualificationCriteria;
        this.startTimestamp = startTimestamp;
        this.endTimestamp= endTimestamp;
    }
}
