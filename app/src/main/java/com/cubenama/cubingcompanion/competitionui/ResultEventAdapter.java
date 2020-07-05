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

public class ResultEventAdapter extends RecyclerView.Adapter<ResultEventAdapter.MyViewHolder>{

    private List<CompetitionEvent> competitionEventsList;
    private CompetitionResultsFragment.ClickListener clickListener;
    private Context mContext;



    @NonNull
    @Override
    public ResultEventAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_result_event, parent, false);

        mContext = parent.getContext();
        return new ResultEventAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull ResultEventAdapter.MyViewHolder holder, int position) {
        CompetitionEvent competitionEvent = competitionEventsList.get(position);

        // Assign values to list row
        holder.eventNameTextView.setText(competitionEvent.eventName);

        // Setup inner recycler view
        ResultRoundStatusAdapter resultRoundStatusAdapter = new ResultRoundStatusAdapter(competitionEvent, innerPosition -> Toast.makeText(mContext, Integer.toString(innerPosition + 1), Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager resultRoundStatusLayoutManager = new LinearLayoutManager(mContext);

        holder.resultRoundStatusRecyclerView.setLayoutManager(resultRoundStatusLayoutManager);
        holder.resultRoundStatusRecyclerView.setAdapter(resultRoundStatusAdapter);

        resultRoundStatusAdapter.notifyDataSetChanged();

    }



    @Override
    public int getItemCount() {
        return competitionEventsList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView eventNameTextView;
        RecyclerView resultRoundStatusRecyclerView;

        private WeakReference<CompetitionResultsFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, CompetitionResultsFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            eventNameTextView = itemView.findViewById(R.id.resultEventNameTextView);
            resultRoundStatusRecyclerView = itemView.findViewById(R.id.resultEventRoundRecyclerView);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    ResultEventAdapter(List<CompetitionEvent> competitionEventsList, CompetitionResultsFragment.ClickListener clickListener){
        this.competitionEventsList = competitionEventsList;
        this.clickListener = clickListener;
    }
}
