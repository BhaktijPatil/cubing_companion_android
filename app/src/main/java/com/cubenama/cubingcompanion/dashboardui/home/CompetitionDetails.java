package com.cubenama.cubingcompanion.dashboardui.home;

import com.google.firebase.Timestamp;

public class CompetitionDetails {

    public String id;
    public String name;
    private String organizer;
    Timestamp startTime;
    Timestamp endTime;

    CompetitionDetails(String id, String name, String organizer, Timestamp startTime, Timestamp endTime)
    {
        this.id = id;
        this.name = name;
        this.organizer = organizer;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
