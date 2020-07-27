package com.cubenama.cubingcompanion;

import java.util.ArrayList;

public class RoundResult {
    String name;
    String wcaId;
    long result;
    long single;
    boolean isVerified;
    ArrayList<Long> timeList;

    RoundResult(String name, String wcaId, long result, long single, ArrayList<Long> timeList, boolean isVerified)
    {
        this.name = name;
        this.wcaId = wcaId;
        this.result = result;
        this.single = single;
        this.timeList = timeList;
        this.isVerified = isVerified;
    }
}
