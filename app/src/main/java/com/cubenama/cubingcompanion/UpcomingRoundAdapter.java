package com.cubenama.cubingcompanion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cubenama.cubingcompanion.competitionui.CompetitionScheduleFragment;
import com.cubenama.cubingcompanion.competitionui.EventRound;

import java.lang.ref.WeakReference;
import java.util.List;

public class UpcomingRoundAdapter extends RecyclerView.Adapter<UpcomingRoundAdapter.MyViewHolder>{

    private List<EventRound> eventRoundsList;
    private CompetitionScheduleFragment.ClickListener clickListener;



    @NonNull
    @Override
    public UpcomingRoundAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_upcoming_round, parent, false);

        return new UpcomingRoundAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull UpcomingRoundAdapter.MyViewHolder holder, int position) {
        EventRound eventRound = eventRoundsList.get(position);

        // Assign values to list row
        holder.roundIdTextView.setText("Round : " + (position + 1));
        // Qualifying criteria for rounds
        if(position == 0)
            holder.participantCountTextView.setText("NA");
        else
            holder.participantCountTextView.setText("Top " + eventRound.participantCount);
        holder.eventNameTextView.setText(eventRound.eventName);
        holder.roundTimeTextView.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  HH:mm", eventRound.startTimestamp) + " - " + new DateTimeFormat().firebaseTimestampToDate("HH:mm", eventRound.endTimestamp));
    }



    @Override
    public int getItemCount() {
        return eventRoundsList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView roundIdTextView;
        TextView participantCountTextView;
        TextView roundTimeTextView;
        TextView eventNameTextView;

        private WeakReference<CompetitionScheduleFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, CompetitionScheduleFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            roundIdTextView = itemView.findViewById(R.id.roundNameTextView);

            participantCountTextView = itemView.findViewById(R.id.participantCountTextView);
            roundTimeTextView = itemView.findViewById(R.id.roundTimeTextView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    UpcomingRoundAdapter(List<EventRound> eventRoundsList, CompetitionScheduleFragment.ClickListener clickListener){
        this.eventRoundsList = eventRoundsList;
        this.clickListener = clickListener;
    }
}