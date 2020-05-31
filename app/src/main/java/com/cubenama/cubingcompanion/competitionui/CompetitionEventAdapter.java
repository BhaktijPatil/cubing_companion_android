package com.cubenama.cubingcompanion.competitionui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cubenama.cubingcompanion.R;

import java.lang.ref.WeakReference;
import java.util.List;

public class CompetitionEventAdapter extends RecyclerView.Adapter<CompetitionEventAdapter.MyViewHolder>{

    private List<CompetitionEvent> competitionEventsList;
    private CompetitionScheduleFragment.ClickListener clickListener;
    private Context mContext;



    @NonNull
    @Override
    public CompetitionEventAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_schedule_event, parent, false);

        mContext = parent.getContext();
        return new CompetitionEventAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull CompetitionEventAdapter.MyViewHolder holder, int position) {
        CompetitionEvent competitionEvent = competitionEventsList.get(position);

        // Assign values to list row
        holder.eventNameTextView.setText(competitionEvent.eventName);
        holder.formatTextView.setText("Best of " + competitionEvent.solveCount);

        // Setup inner recycler view
        EventRoundAdapter eventRoundAdapter = new EventRoundAdapter(competitionEvent.eventRounds, innerPosition -> Toast.makeText(mContext, Integer.toString(innerPosition + 1), Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager eventRoundLayoutManager = new LinearLayoutManager(mContext);

        holder.competitionEventRecyclerView.setLayoutManager(eventRoundLayoutManager);
        holder.competitionEventRecyclerView.setAdapter(eventRoundAdapter);

        eventRoundAdapter.notifyDataSetChanged();

    }



    @Override
    public int getItemCount() {
        return competitionEventsList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView eventNameTextView;
        TextView formatTextView;

        RecyclerView competitionEventRecyclerView = itemView.findViewById(R.id.eventRoundRecyclerView);

        private WeakReference<CompetitionScheduleFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, CompetitionScheduleFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            formatTextView = itemView.findViewById(R.id.eventFormatTextView);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    CompetitionEventAdapter(List<CompetitionEvent> competitionEventsList, CompetitionScheduleFragment.ClickListener clickListener){
        this.competitionEventsList = competitionEventsList;
        this.clickListener = clickListener;
    }
}
