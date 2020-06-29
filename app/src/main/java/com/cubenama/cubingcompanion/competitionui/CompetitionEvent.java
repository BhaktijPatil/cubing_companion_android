package com.cubenama.cubingcompanion.competitionui;

import java.util.ArrayList;
import java.util.List;

public class CompetitionEvent {
    public String eventId;
    public String eventName;
    public long solveCount;
    public List<EventRound> eventRounds;

    public CompetitionEvent(String eventId, String eventName, long solveCount)
    {
        this.eventId = eventId;
        this.eventName = eventName;
        this.solveCount = solveCount;
        eventRounds = new ArrayList<>();
    }
}
