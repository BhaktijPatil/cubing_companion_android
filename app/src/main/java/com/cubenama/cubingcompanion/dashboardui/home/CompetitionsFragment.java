package com.cubenama.cubingcompanion.dashboardui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cubenama.cubingcompanion.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CompetitionsFragment  extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_competitions, container, false);

        // Create database instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Setup progress bar & placeholder text
        ProgressBar upcomingCompetitionsProgressBar = root.findViewById(R.id.upcomingCompetitionsProgressBar);
        ProgressBar pastCompetitionsProgressBar = root.findViewById(R.id.pastCompetitionsProgressBar);

        TextView upcomingCompetitionPlaceholderTextView = root.findViewById(R.id.upcomingCompetitionPlaceholderTextView);
        TextView pastCompetitionPlaceholderTextView = root.findViewById(R.id.pastCompetitionPlaceholderTextView);

        // Setup recycler views and adapters
        List<CompetitionDetails> upcomingCompetitionDetailsList = new ArrayList<>();
        List<CompetitionDetails> pastCompetitionDetailsList = new ArrayList<>();

        RecyclerView upcomingCompetitionsRecyclerView = root.findViewById(R.id.upcomingCompetitionsRecyclerView);
        RecyclerView pastCompetitionsRecyclerView = root.findViewById(R.id.pastCompetitionsRecyclerView);

        CompetitionDetailsAdapter competitionDetailsAdapter1 = new CompetitionDetailsAdapter(upcomingCompetitionDetailsList, position -> Toast.makeText(getContext(), upcomingCompetitionDetailsList.get(position).name, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager upcomingCompetitionsLayoutManager = new LinearLayoutManager(getContext());

        CompetitionDetailsAdapter competitionDetailsAdapter2 = new CompetitionDetailsAdapter(pastCompetitionDetailsList, position -> Toast.makeText(getContext(), pastCompetitionDetailsList.get(position).name, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager pastCompetitionsLayoutManager = new LinearLayoutManager(getContext());


        upcomingCompetitionsRecyclerView.setLayoutManager(upcomingCompetitionsLayoutManager);
        upcomingCompetitionsRecyclerView.setAdapter(competitionDetailsAdapter1);

        pastCompetitionsRecyclerView.setLayoutManager(pastCompetitionsLayoutManager);
        pastCompetitionsRecyclerView.setAdapter(competitionDetailsAdapter2);

        // Load data from firebase into UI
        CollectionReference competition_details = db.collection(getString(R.string.db_field_name_comp_details));
        competition_details.orderBy(getString(R.string.db_field_name_start_time), Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
            upcomingCompetitionDetailsList.clear();
            pastCompetitionDetailsList.clear();
            for(QueryDocumentSnapshot competition : task.getResult())
            {
                Log.d("CC_COMPETITION", competition.getString(getString(R.string.db_field_name_name)));
                CompetitionDetails competitionDetails = new CompetitionDetails(competition.getId(), competition.getString(getString(R.string.db_field_name_name)), competition.getString(getString(R.string.db_field_name_organizer)), competition.getTimestamp(getString(R.string.db_field_name_start_time)), competition.getTimestamp(getString(R.string.db_field_name_end_time)));
                // Check if competition has been conducted
                if (Calendar.getInstance().getTimeInMillis()/1000 < competition.getTimestamp(getString(R.string.db_field_name_end_time)).getSeconds())
                    upcomingCompetitionDetailsList.add(competitionDetails);
                else
                    pastCompetitionDetailsList.add(competitionDetails);
            }
            competitionDetailsAdapter1.notifyDataSetChanged();
            competitionDetailsAdapter2.notifyDataSetChanged();

            // Disable progress bars
            upcomingCompetitionsProgressBar.setVisibility(View.GONE);
            pastCompetitionsProgressBar.setVisibility(View.GONE);

            // Enable placeholder textviews
            if(upcomingCompetitionDetailsList.isEmpty())
                upcomingCompetitionPlaceholderTextView.setVisibility(View.VISIBLE);
            if(pastCompetitionDetailsList.isEmpty())
                pastCompetitionPlaceholderTextView.setVisibility(View.VISIBLE);

        });

        return root;
    }

    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}
