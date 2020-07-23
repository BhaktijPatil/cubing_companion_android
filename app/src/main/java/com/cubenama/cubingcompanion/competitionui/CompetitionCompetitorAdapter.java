package com.cubenama.cubingcompanion.competitionui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cubenama.cubingcompanion.R;

import java.lang.ref.WeakReference;
import java.util.List;

public class CompetitionCompetitorAdapter extends RecyclerView.Adapter<CompetitionCompetitorAdapter.MyViewHolder>{
    private List<CompetitionCompetitor> competitionCompetitorsList;
    private CompetitionCompetitorsFragment.ClickListener clickListener;
    private Context context;

    @NonNull
    @Override
    public CompetitionCompetitorAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_competitor, parent, false);

        context = parent.getContext();
        return new CompetitionCompetitorAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull CompetitionCompetitorAdapter.MyViewHolder holder, int position) {
        CompetitionCompetitor competitionCompetitor = competitionCompetitorsList.get(position);

        // Assign values to list row
        holder.competitorNameTextView.setText(competitionCompetitor.name);
        holder.competitorWcaIdTextView.setText(competitionCompetitor.wcaId);
        holder.competitorWcaIdTextView.setOnClickListener(v-> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.worldcubeassociation.org/persons/" + competitionCompetitor.wcaId));
            context.startActivity(intent);
        });
    }



    @Override
    public int getItemCount() {
        return competitionCompetitorsList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView competitorNameTextView;
        TextView competitorWcaIdTextView;

        private WeakReference<CompetitionCompetitorsFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, CompetitionCompetitorsFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            competitorNameTextView = itemView.findViewById(R.id.competitorNameTextView);
            competitorWcaIdTextView = itemView.findViewById(R.id.competitorWcaIdTextView);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    CompetitionCompetitorAdapter(List<CompetitionCompetitor> competitionCompetitorsList, CompetitionCompetitorsFragment.ClickListener clickListener){
        this.competitionCompetitorsList = competitionCompetitorsList;
        this.clickListener = clickListener;
    }
}
