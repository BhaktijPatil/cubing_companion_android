package com.cubenama.cubingcompanion.competitionui;

import java.util.ArrayList;
import java.util.List;

class CompetitionEvent {
    String eventId;
    String eventName;
    String resultCalcMethod;
    long solveCount;
    List<CompetitionEventRound> competitionEventRounds;

    CompetitionEvent(String eventId, String eventName, long solveCount, String resultCalcMethod)
    {
        this.eventId = eventId;
        this.eventName = eventName;
        this.solveCount = solveCount;
        this.resultCalcMethod = resultCalcMethod;
        competitionEventRounds = new ArrayList<>();
    }
}
