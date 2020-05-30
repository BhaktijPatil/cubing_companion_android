package com.cubenama.cubingcompanion;

import android.text.format.DateFormat;

import com.google.firebase.Timestamp;

import java.util.Calendar;

public class DateTimeFormat {

    // Function to convert firebase timestamp to any Date format
    public String firebaseTimestampToDate(String format, Timestamp timestamp)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp.getSeconds()*1000);
        return DateFormat.format(format, cal).toString();
    }
}
