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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompetitionScheduleFragment extends Fragment {

    // Database reference
    private FirebaseFirestore db;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_competition_schedule, container, false);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Show loading screen
        ((CompetitionDetailActivity)requireActivity()).loadingScreenController.showLoadingScreen(getString(R.string.loading_screen_msg_2));

        // Setup recycler views and adapters
        List<CompetitionEvent> competitionEventList = new ArrayList<>();
        RecyclerView competitionEventRecyclerView = root.findViewById(R.id.competitionEventRecyclerView);

        CompetitionEventAdapter competitionEventAdapter = new CompetitionEventAdapter(competitionEventList, position -> Toast.makeText(getContext(), competitionEventList.get(position).eventId, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager competitionEventLayoutManager = new LinearLayoutManager(requireContext());

        competitionEventRecyclerView.setLayoutManager(competitionEventLayoutManager);
        competitionEventRecyclerView.setAdapter(competitionEventAdapter);

        // Get competition ID
        Intent intent = requireActivity().getIntent();
        String compId = intent.getStringExtra("comp_id");

        TextView eventCountTextView = root.findViewById(R.id.eventCountTextView);

        CollectionReference eventsReference = db.collection(getString(R.string.db_field_name_comp_details)).document(compId).collection(getString(R.string.db_field_name_events));
        eventsReference.get().addOnCompleteListener(eventDetailsTask -> {
            // Set number of events
            Log.d("CC_COMP_SCHEDULE", "Number of events : " + eventDetailsTask.getResult().size());
            eventCountTextView.setText(eventDetailsTask.getResult().size() + " Events");

            competitionEventList.clear();
            // Individual events are obtained here
            for(QueryDocumentSnapshot event : eventDetailsTask.getResult())
            {
                CollectionReference eventRoundsReference = eventsReference.document(event.getId()).collection(getString(R.string.db_field_name_rounds));
                eventRoundsReference.orderBy(getString(R.string.db_field_name_name)).get().addOnCompleteListener(innerTask -> {
                    // Create a new event instance
                    CompetitionEvent compEvent = new CompetitionEvent(event.getId(), event.getString(getString(R.string.db_field_name_name)), event.getLong(getString(R.string.db_field_name_solve_count)), event.getString(getString(R.string.db_field_name_result_calc_method)));
                    Log.d("CC_COMP_SCHEDULE", "Event ID : " + compEvent.eventId + " Round Count : " + innerTask.getResult().size());

                    // Individual rounds for each event are obtained here
                    for(QueryDocumentSnapshot round : innerTask.getResult())
                    {
                        CompetitionEventRound competitionEventRound = new CompetitionEventRound(event.getString(getString(R.string.db_field_name_name)), round.getString(getString(R.string.db_field_name_name)), round.getId(), round.getLong(getString(R.string.qualification_criteria)), round.getTimestamp(getString(R.string.db_field_name_start_time)), round.getTimestamp(getString(R.string.db_field_name_end_time)));
                        compEvent.competitionEventRounds.add(competitionEventRound);
                    }
                    competitionEventList.add(compEvent);

                    // Sort Events by name
                    Collections.sort(competitionEventList, (event1, event2) -> event1.eventName.compareTo(event2.eventName));
                    competitionEventAdapter.notifyDataSetChanged();
                });
            }
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
