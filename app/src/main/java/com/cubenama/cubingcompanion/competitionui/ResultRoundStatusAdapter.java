package com.cubenama.cubingcompanion.competitionui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.Fragment;

import com.cubenama.cubingcompanion.R;
import com.cubenama.cubingcompanion.RoundResultActivity;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;

public class ResultRoundStatusAdapter extends RecyclerView.Adapter<com.cubenama.cubingcompanion.competitionui.ResultRoundStatusAdapter.MyViewHolder>{

    private CompetitionEvent event;
    private CompetitionResultsFragment.ClickListener clickListener;

    private Context context;

    @NonNull
    @Override
    public com.cubenama.cubingcompanion.competitionui.ResultRoundStatusAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_result_event_round, parent, false);
        context = parent.getContext();
        return new com.cubenama.cubingcompanion.competitionui.ResultRoundStatusAdapter.MyViewHolder(itemView, clickListener);
    }



    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull com.cubenama.cubingcompanion.competitionui.ResultRoundStatusAdapter.MyViewHolder holder, int position) {
        EventRound eventRound = event.eventRounds.get(position);

        // Assign values to list row
        holder.roundIdTextView.setText("Round " + (position + 1));

        // Assign result status
        if(Calendar.getInstance().getTimeInMillis() > eventRound.startTimestamp.getSeconds()*1000)
        {
            // Start new activity to load results
            holder.resultHolderCardView.setOnClickListener(v -> {
                Intent resultIntent = new Intent(context, RoundResultActivity.class);
                resultIntent.putExtra("event_id", event.eventId);
                resultIntent.putExtra("result_calc_method", event.resultCalcMethod);
                resultIntent.putExtra("comp_id", ((Activity)context).getIntent().getStringExtra("comp_id"));
                resultIntent.putExtra("event_name", event.eventName);
                resultIntent.putExtra("round_id", eventRound.roundId);
                resultIntent.putExtra("round_name", String.valueOf(position + 1));
                // Send qualification criteria for next round
                if(position + 1 < event.eventRounds.size())
                    resultIntent.putExtra("qualification_criteria", event.eventRounds.get(position+1).qualificationCriteria);
                // Set qualification criteria to 3 (podiums) if final round
                else
                    resultIntent.putExtra("qualification_criteria", 3);
                context.startActivity(resultIntent);
            });

            if(Calendar.getInstance().getTimeInMillis() < eventRound.endTimestamp.getSeconds()*1000) {
                // Round is Live
                holder.resultStatusTextView.setText("Live");
                holder.resultStatusTextView.setTextColor(context.getColor(R.color.colorAccent));
            }
            else {
                // Round has ended
                holder.resultStatusTextView.setText("Done");
                holder.resultStatusTextView.setTextColor(context.getColor(R.color.colorPrimary));
            }
        }
        else {
            // Round hasn't started
            holder.resultStatusTextView.setText("NA");
            holder.resultStatusTextView.setTextColor(context.getColor(R.color.colorTextSecondaryLight));
        }
    }



    @Override
    public int getItemCount() {
        return event.eventRounds.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView roundIdTextView;
        TextView resultStatusTextView;
        CardView resultHolderCardView;

        private WeakReference<CompetitionResultsFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, CompetitionResultsFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            roundIdTextView = itemView.findViewById(R.id.roundIdTextView);
            resultStatusTextView = itemView.findViewById(R.id.resultStatusTextView);
            resultHolderCardView = itemView.findViewById(R.id.resultHolderCardView);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    ResultRoundStatusAdapter(CompetitionEvent event, CompetitionResultsFragment.ClickListener clickListener){
        this.event = event;
        this.clickListener = clickListener;
    }
}
