package com.cubenama.cubingcompanion.competitionui;

import com.google.firebase.Timestamp;

class EventRound {

    long participantCount;
    Timestamp startTimestamp;
    Timestamp endTimestamp;

    EventRound(long participantCount, Timestamp startTimestamp, Timestamp endTimestamp)
    {
        this.participantCount = participantCount;
        this.startTimestamp = startTimestamp;
        this.endTimestamp= endTimestamp;
    }
}
