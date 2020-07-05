package com.cubenama.cubingcompanion.competitionui;

import java.util.ArrayList;
import java.util.List;

public class CompetitionEvent {
    public String eventId;
    public String eventName;
    public String resultCalcMethod;
    public long solveCount;
    public List<EventRound> eventRounds;

    public CompetitionEvent(String eventId, String eventName, long solveCount, String resultCalcMethod)
    {
        this.eventId = eventId;
        this.eventName = eventName;
        this.solveCount = solveCount;
        this.resultCalcMethod = resultCalcMethod;
        eventRounds = new ArrayList<>();
    }
}
