package com.cubenama.cubingcompanion.competitionui;

import java.util.ArrayList;
import java.util.List;

class CompetitionEvent {
    String eventId;
    List<EventRound> eventRounds;

    CompetitionEvent(String eventId)
    {
        this.eventId = eventId;
        eventRounds = new ArrayList<>();
    }
}
