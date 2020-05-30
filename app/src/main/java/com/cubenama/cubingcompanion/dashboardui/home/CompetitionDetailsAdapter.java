package com.cubenama.cubingcompanion.dashboardui.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cubenama.cubingcompanion.CompetitionDetailActivity;
import com.cubenama.cubingcompanion.DateTimeFormat;
import com.cubenama.cubingcompanion.R;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class CompetitionDetailsAdapter extends RecyclerView.Adapter<CompetitionDetailsAdapter.MyViewHolder> {

    private List<CompetitionDetails> competitionDetailsList;
    private CompetitionsFragment.ClickListener clickListener;
    private Context mContext;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_competitions, parent, false);

        mContext = parent.getContext();

        return new MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CompetitionDetails competitionDetail = competitionDetailsList.get(position);

        // Assign values to list row
        holder.competitionNameTextView.setText(competitionDetail.name);
        holder.competitionDurationTextView.setText(DateTimeFormat.firebaseTimestampToDate("dd-MMM-yyyy hh:mm", competitionDetail.startTime) + "  to  " + DateTimeFormat.firebaseTimestampToDate("dd-MMM-yyyy hh:mm", competitionDetail.endTime));
        holder.competitionHolderLayout.setOnClickListener(v->{
            // Start competition detail activity
            Intent competitionDetailActivityIntent = new Intent(mContext, CompetitionDetailActivity.class);
            competitionDetailActivityIntent.putExtra("comp_id", competitionDetail.id);
            mContext.startActivity(competitionDetailActivityIntent);
        });

    }



    @Override
    public int getItemCount() {
        return competitionDetailsList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ConstraintLayout competitionHolderLayout;
        TextView competitionNameTextView;
        TextView competitionDurationTextView;

        private WeakReference<CompetitionsFragment.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, CompetitionsFragment.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            competitionNameTextView = itemView.findViewById(R.id.competitionNameTextView);
            competitionDurationTextView = itemView.findViewById(R.id.competitionDurationTextView);
            competitionHolderLayout = itemView.findViewById(R.id.competitionHolderLayout);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    CompetitionDetailsAdapter(List<CompetitionDetails> competitionDetailsList, CompetitionsFragment.ClickListener clickListener){
        this.competitionDetailsList = competitionDetailsList;
        this.clickListener = clickListener;
    }

}
