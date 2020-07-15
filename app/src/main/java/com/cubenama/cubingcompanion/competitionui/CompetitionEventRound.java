package com.cubenama.cubingcompanion.competitionui;

import com.google.firebase.Timestamp;

public class CompetitionEventRound {

    public String roundId;
    public long roundNo;
    public String eventName;
    public long qualificationCriteria;
    public Timestamp startTimestamp;
    public Timestamp endTimestamp;

    public CompetitionEventRound(String eventName, long roundNo, String roundId, long qualificationCriteria, Timestamp startTimestamp, Timestamp endTimestamp)
    {
        this.eventName = eventName;
        this.roundNo = roundNo;
        this.roundId = roundId;
        this.qualificationCriteria = qualificationCriteria;
        this.startTimestamp = startTimestamp;
        this.endTimestamp= endTimestamp;
    }
}
