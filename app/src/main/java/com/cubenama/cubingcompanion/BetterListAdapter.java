package com.cubenama.cubingcompanion;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BetterListAdapter extends ArrayAdapter {

    private final String[] textArray;
    private final Activity activity;



    public BetterListAdapter(Activity activity, String[] text) {
        super(activity, R.layout.list_row_better_item, text);
        this.activity = activity;
        this.textArray = text;
    }



    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_row_better_item, null,true);

        TextView idTextView = rowView.findViewById(R.id.itemIdTextView);
        TextView contentTextView = rowView.findViewById(R.id.listItemTextView);

        contentTextView.setText(textArray[position]);
        idTextView.setText((position + 1) + ".");

        return rowView;
    }
}
