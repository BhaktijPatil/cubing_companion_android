package com.cubenama.cubingcompanion.dashboardui.home;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cubenama.cubingcompanion.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ServerTimestamp;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CompetitionsFragment  extends Fragment {

    // Database reference
    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_competitions, container, false);

        // Create database instance
        db = FirebaseFirestore.getInstance();

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

        // Add listener to update UI when data changes
        CollectionReference competition_details = db.collection("competition_details");
        competition_details.get().addOnCompleteListener(task -> {
            upcomingCompetitionDetailsList.clear();
            pastCompetitionDetailsList.clear();
            for(QueryDocumentSnapshot competition : task.getResult())
            {
                Log.d("CC_COMPETITION", String.valueOf(competition.get("name")));
                CompetitionDetails competitionDetails = new CompetitionDetails(competition.getId(), competition.getString("name"), competition.getString("organizer"), competition.getTimestamp("start_time"), competition.getTimestamp("end_time"));
                // Check if competition has been conducted
                if (Calendar.getInstance().getTimeInMillis()/1000 < competition.getTimestamp("end_time").getSeconds())
                    upcomingCompetitionDetailsList.add(competitionDetails);
                else
                    pastCompetitionDetailsList.add(competitionDetails);
            }
            competitionDetailsAdapter1.notifyDataSetChanged();
            competitionDetailsAdapter2.notifyDataSetChanged();
        });

        return root;
    }

    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}
