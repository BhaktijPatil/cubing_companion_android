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

public class CompetitionEventRoundAdapter extends RecyclerView.Adapter<CompetitionEventRoundAdapter.MyViewHolder>{

    private List<CompetitionEventRound> competitionEventRoundsList;
    private CompetitionScheduleFragment.ClickListener clickListener;
    private Context context;

    @NonNull
    @Override
    public CompetitionEventRoundAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_schedule_round, parent, false);

        context = parent.getContext();

        return new CompetitionEventRoundAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull CompetitionEventRoundAdapter.MyViewHolder holder, int position) {
        CompetitionEventRound competitionEventRound = competitionEventRoundsList.get(position);

        // Assign values to list row
        holder.roundIdTextView.setText("Round " + competitionEventRound.roundName);
        // Qualifying criteria for rounds
        if(competitionEventRound.roundName.equals("1"))
            holder.qualificationCriteriaTextView.setText(context.getString(R.string.qualification_criteria) + " : NA");
        else
            holder.qualificationCriteriaTextView.setText(context.getString(R.string.qualification_criteria) + " : Top " + competitionEventRound.qualificationCriteria);
        holder.roundTimeTextView.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  hh:mm aa", competitionEventRound.startTimestamp) + "  to  " + new DateTimeFormat().firebaseTimestampToDate("hh:mm aa", competitionEventRound.endTimestamp));
    }



    @Override
    public int getItemCount() {
        return competitionEventRoundsList.size();
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

    CompetitionEventRoundAdapter(List<CompetitionEventRound> competitionEventRoundsList, CompetitionScheduleFragment.ClickListener clickListener){
        this.competitionEventRoundsList = competitionEventRoundsList;
        this.clickListener = clickListener;
    }
}