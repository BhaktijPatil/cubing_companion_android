package com.cubenama.cubingcompanion.competitionui;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cubenama.cubingcompanion.CompetitionDetailActivity;
import com.cubenama.cubingcompanion.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CompetitionCompetitorsFragment extends Fragment {

    // Database reference
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_competition_competitors, container, false);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Show loading screen
        ((CompetitionDetailActivity)getActivity()).loadingScreenController.showLoadingScreen("Almost there ...");

        // Setup recycler views and adapters
        List<CompetitionCompetitor> competitionCompetitorsList = new ArrayList<>();
        RecyclerView competitorCompetitorsRecyclerView = root.findViewById(R.id.competitionCompetitorsRecyclerView);

        CompetitionCompetitorAdapter competitionCompetitorAdapter = new CompetitionCompetitorAdapter(competitionCompetitorsList, position -> Toast.makeText(getContext(), competitionCompetitorsList.get(position).name, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager competitionCompetitorLayoutManager = new LinearLayoutManager(requireContext());

        competitorCompetitorsRecyclerView.setLayoutManager(competitionCompetitorLayoutManager);
        competitorCompetitorsRecyclerView.setAdapter(competitionCompetitorAdapter);

        // Get competition ID
        Intent intent = requireActivity().getIntent();
        String comp_id = intent.getStringExtra("comp_id");

        CardView competitorsHolderCardView =  root.findViewById(R.id.competitorsHolderCardView);
        TextView competitorCountTextView = root.findViewById(R.id.competitorCountTextView);

        CollectionReference competition_competitors = db.collection("competition_details").document(comp_id).collection("competitors");
        competition_competitors.orderBy("name", Query.Direction.ASCENDING).addSnapshotListener( (queryDocumentSnapshots, e) ->{
            competitionCompetitorsList.clear();

            // Set number of competitors
            db.collection("competition_details").document(comp_id).get().addOnCompleteListener(innerTask->{
                Log.d("CC_COMP_COMPETITORS", "Number of competitors : " + innerTask.getResult().getLong("competitor_limit"));
                competitorCountTextView.setText(queryDocumentSnapshots.size() + "/" +innerTask.getResult().getLong("competitor_limit"));
            });

            for (QueryDocumentSnapshot competitor : queryDocumentSnapshots)
            {
                competitionCompetitorsList.add(new CompetitionCompetitor(competitor.getString("wca_id"), competitor.getString("name")));
            }
            competitorsHolderCardView.setVisibility(View.VISIBLE);
            competitionCompetitorAdapter.notifyDataSetChanged();

            // Dismiss loading screen
            ((CompetitionDetailActivity)getActivity()).loadingScreenController.dismissLoadingScreen();
        });

        return root;
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}
