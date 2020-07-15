package com.cubenama.cubingcompanion.competitionui;

import android.content.Intent;
import android.os.Bundle;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class CompetitionResultsFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_competition_results, container, false);

        // Create database instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Show loading screen
        ((CompetitionDetailActivity)requireActivity()).loadingScreenController.showLoadingScreen(getString(R.string.loading_screen_msg_3));

        // Setup recycler views and adapters
        List<CompetitionEvent> competitionEventList = new ArrayList<>();
        RecyclerView competitionResultsRecyclerView = root.findViewById(R.id.competitionResultsRecyclerView);

        ResultEventAdapter resultEventAdapter = new ResultEventAdapter(competitionEventList, position -> Toast.makeText(getContext(), competitionEventList.get(position).eventId, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager resultEventLayoutManager = new LinearLayoutManager(requireContext());

        competitionResultsRecyclerView.setLayoutManager(resultEventLayoutManager);
        competitionResultsRecyclerView.setAdapter(resultEventAdapter);


        // Get competition ID
        Intent intent = requireActivity().getIntent();
        String compId = intent.getStringExtra("comp_id");

        TextView resultStatusTextView = root.findViewById(R.id.resultStatusTextView);

        DocumentReference competitionDetailsReference = db.collection(getString(R.string.db_field_name_comp_details)).document(compId);
        competitionDetailsReference.get().addOnCompleteListener(compDetailsTask -> {
            // Set overall result status
            if (Calendar.getInstance().getTimeInMillis() > compDetailsTask.getResult().getTimestamp(getString(R.string.db_field_name_start_time)).getSeconds() * 1000)
                // Competition is live
                if(Calendar.getInstance().getTimeInMillis() < compDetailsTask.getResult().getTimestamp(getString(R.string.db_field_name_end_time)).getSeconds() * 1000)
                    resultStatusTextView.setText("Live");
                // Competition is done
                else
                    // Results verified
                    if(compDetailsTask.getResult().getBoolean(getString(R.string.db_field_name_results_verified)))
                        resultStatusTextView.setText("Verified");
                    // Results not verified
                    else
                        resultStatusTextView.setText("Done (Verification pending)");
            // Competition hasn't started
            else
                resultStatusTextView.setText("To be declared ...");

            CollectionReference eventDetailsReference = competitionDetailsReference.collection(getString(R.string.db_field_name_events));
            eventDetailsReference.get().addOnCompleteListener(eventDetailsTask -> {
                competitionEventList.clear();
                // Individual events are obtained here
                for(QueryDocumentSnapshot event : eventDetailsTask.getResult())
                {
                    CollectionReference roundDetailsReference = eventDetailsReference.document(event.getId()).collection(getString(R.string.db_field_name_rounds));
                    roundDetailsReference.orderBy(getString(R.string.db_field_name_id)).get().addOnCompleteListener(roundDetailsTask -> {

                        // Create a new event instance
                        CompetitionEvent competitionEvent = new CompetitionEvent(event.getId(), event.getString(getString(R.string.db_field_name_name)), event.getLong(getString(R.string.db_field_name_solve_count)), event.getString(getString(R.string.db_field_name_result_calc_method)));
                        Log.d("CC_COMP_SCHEDULE", "Event ID : " + competitionEvent.eventId + " Round Count : " + roundDetailsTask.getResult().size());

                        // Individual rounds for each event are obtained here
                        for(QueryDocumentSnapshot round : roundDetailsTask.getResult())
                        {
                            CompetitionEventRound competitionEventRound = new CompetitionEventRound(competitionEvent.eventName, round.getLong(getString(R.string.db_field_name_id)), round.getId(), round.getLong(getString(R.string.db_field_name_qualification_criteria)), round.getTimestamp(getString(R.string.db_field_name_start_time)), round.getTimestamp(getString(R.string.db_field_name_end_time)));
                            competitionEvent.competitionEventRounds.add(competitionEventRound);
                        }
                        competitionEventList.add(competitionEvent);

                        // Sort Events by name
                        Collections.sort(competitionEventList, (event1, event2) -> event1.eventName.compareTo(event2.eventName));
                        resultEventAdapter.notifyDataSetChanged();
                    });
                }
                // Dismiss loading screen
                ((CompetitionDetailActivity)requireActivity()).loadingScreenController.dismissLoadingScreen();
            });
        });

        return root;
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}
