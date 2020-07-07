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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CompetitionCompetitorsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_competition_competitors, container, false);

        // Create database instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Show loading screen
        ((CompetitionDetailActivity)requireActivity()).loadingScreenController.showLoadingScreen(getString(R.string.loading_screen_msg_2));

        // Setup recycler views and adapters
        List<CompetitionCompetitor> competitionCompetitorsList = new ArrayList<>();
        RecyclerView competitorCompetitorsRecyclerView = root.findViewById(R.id.competitionCompetitorsRecyclerView);

        CompetitionCompetitorAdapter competitionCompetitorAdapter = new CompetitionCompetitorAdapter(competitionCompetitorsList, position -> Toast.makeText(getContext(), competitionCompetitorsList.get(position).name, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager competitionCompetitorLayoutManager = new LinearLayoutManager(requireContext());

        competitorCompetitorsRecyclerView.setLayoutManager(competitionCompetitorLayoutManager);
        competitorCompetitorsRecyclerView.setAdapter(competitionCompetitorAdapter);

        // Get competition ID
        Intent intent = requireActivity().getIntent();
        String compId = intent.getStringExtra("comp_id");

        CardView competitorsHolderCardView =  root.findViewById(R.id.competitorsHolderCardView);
        TextView competitorCountTextView = root.findViewById(R.id.competitorCountTextView);

        DocumentReference competitionDetailsReference = db.collection(getString(R.string.db_field_name_comp_details)).document(compId);
        CollectionReference competitorDetailsReference = competitionDetailsReference.collection(getString(R.string.db_field_value_competitors));
        competitorDetailsReference.orderBy(getString(R.string.db_field_name_name), Query.Direction.ASCENDING).addSnapshotListener( (queryDocumentSnapshots, e) ->{
            competitionCompetitorsList.clear();

            // Set number of competitors
            competitionDetailsReference.get().addOnCompleteListener(innerTask->{
                Log.d("CC_COMP_COMPETITORS", "Number of competitors : " + queryDocumentSnapshots.size());
                competitorCountTextView.setText(queryDocumentSnapshots.size() + "/" + innerTask.getResult().getLong(getString(R.string.db_field_name_competitor_limit)));
            });

            for (QueryDocumentSnapshot competitor : queryDocumentSnapshots)
            {
                competitionCompetitorsList.add(new CompetitionCompetitor(competitor.getString(getString(R.string.db_field_name_wca_id)), competitor.getString(getString(R.string.db_field_name_name))));
            }
            competitorsHolderCardView.setVisibility(View.VISIBLE);
            competitionCompetitorAdapter.notifyDataSetChanged();

            // Dismiss loading screen
            ((CompetitionDetailActivity)requireActivity()).loadingScreenController.dismissLoadingScreen();
        });

        return root;
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}
