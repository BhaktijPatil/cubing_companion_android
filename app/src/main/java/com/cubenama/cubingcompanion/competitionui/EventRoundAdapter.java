package com.cubenama.cubingcompanion.competitionui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cubenama.cubingcompanion.DateTimeFormat;
import com.cubenama.cubingcompanion.R;

import java.lang.ref.WeakReference;
import java.util.List;

public class EventRoundAdapter extends RecyclerView.Adapter<EventRoundAdapter.MyViewHolder>{

    private List<EventRound> eventRoundsList;
    private CompetitionScheduleFragment.ClickListener clickListener;
    private Context context;

    @NonNull
    @Override
    public EventRoundAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_schedule_round, parent, false);

        context = parent.getContext();

        return new EventRoundAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull EventRoundAdapter.MyViewHolder holder, int position) {
        EventRound eventRound = eventRoundsList.get(position);

        // Assign values to list row
        holder.roundIdTextView.setText("Round : " + (position + 1));
        // Qualifying criteria for rounds
        if(position == 0)
            holder.qualificationCriteriaTextView.setText(context.getString(R.string.qualificaiton_criteria) + " : NA");
        else
            holder.qualificationCriteriaTextView.setText(context.getString(R.string.qualificaiton_criteria) + " : Top " + eventRound.qualificationCriteria);
        holder.roundTimeTextView.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  hh:mm aa", eventRound.startTimestamp) + "  to  " + new DateTimeFormat().firebaseTimestampToDate("hh:mm aa", eventRound.endTimestamp));
    }



    @Override
    public int getItemCount() {
        return eventRoundsList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView roundIdTextView;
        TextView qualificationCriteriaTextView;
        TextView roundTimeTextView;


        private WeakReference<CompetitionScheduleFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, CompetitionScheduleFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            roundIdTextView = itemView.findViewById(R.id.roundIdTextView);
            qualificationCriteriaTextView = itemView.findViewById(R.id.qualificationCriteriaTextView);
            roundTimeTextView = itemView.findViewById(R.id.roundTimeTextView);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    EventRoundAdapter(List<EventRound> eventRoundsList, CompetitionScheduleFragment.ClickListener clickListener){
        this.eventRoundsList = eventRoundsList;
        this.clickListener = clickListener;
    }
}