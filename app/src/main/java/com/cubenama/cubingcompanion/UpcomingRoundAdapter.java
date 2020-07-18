package com.cubenama.cubingcompanion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cubenama.cubingcompanion.competitionui.CompetitionScheduleFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionEventRound;

import java.lang.ref.WeakReference;
import java.util.List;

public class UpcomingRoundAdapter extends RecyclerView.Adapter<UpcomingRoundAdapter.MyViewHolder>{

    private List<CompetitionEventRound> competitionEventRoundsList;
    private CompetitionScheduleFragment.ClickListener clickListener;
    private Context context;


    @NonNull
    @Override
    public UpcomingRoundAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_upcoming_round, parent, false);

        context = parent.getContext();

        return new UpcomingRoundAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull UpcomingRoundAdapter.MyViewHolder holder, int position) {
        CompetitionEventRound competitionEventRound = competitionEventRoundsList.get(position);

        // Assign values to list row
        holder.roundIdTextView.setText("Round " + competitionEventRound.roundNo);
        // Qualifying criteria for rounds
        if(competitionEventRound.roundNo == 0)
            holder.qualificationCriteriaTextView.setText(context.getString(R.string.qualification_criteria) + " : NA");
        else
            holder.qualificationCriteriaTextView.setText(context.getString(R.string.qualification_criteria) + " : Top " + competitionEventRound.qualificationCriteria);
        holder.eventNameTextView.setText(competitionEventRound.eventName);
        holder.roundTimeTextView.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  hh:mm aa", competitionEventRound.startTimestamp) + " - " + new DateTimeFormat().firebaseTimestampToDate("hh:mm aa", competitionEventRound.endTimestamp));
    }



    @Override
    public int getItemCount() {
        return competitionEventRoundsList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView roundIdTextView;
        TextView qualificationCriteriaTextView;
        TextView roundTimeTextView;
        TextView eventNameTextView;

        private WeakReference<CompetitionScheduleFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, CompetitionScheduleFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            roundIdTextView = itemView.findViewById(R.id.roundNameTextView);

            qualificationCriteriaTextView = itemView.findViewById(R.id.qualificationCriteriaTextView);
            roundTimeTextView = itemView.findViewById(R.id.roundTimeTextView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    UpcomingRoundAdapter(List<CompetitionEventRound> competitionEventRoundsList, CompetitionScheduleFragment.ClickListener clickListener){

        this.competitionEventRoundsList = competitionEventRoundsList;
        this.clickListener = clickListener;
    }
}