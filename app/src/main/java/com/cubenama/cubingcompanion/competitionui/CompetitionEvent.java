package com.cubenama.cubingcompanion.competitionui;

import java.util.ArrayList;
import java.util.List;

class CompetitionEvent {
    String eventId;
    String eventName;
    long solveCount;
    List<EventRound> eventRounds;

    CompetitionEvent(String eventId, String eventName, long solveCount)
    {
        this.eventId = eventId;
        this.eventName = eventName;
        this.solveCount = solveCount;
        eventRounds = new ArrayList<>();
    }
}
