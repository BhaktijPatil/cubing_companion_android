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

import com.cubenama.cubingcompanion.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompetitionScheduleFragment extends Fragment {

    // Database reference
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_competition_schedule, container, false);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Setup recycler views and adapters
        List<CompetitionEvent> competitionEventList = new ArrayList<>();
        RecyclerView competitionEventRecyclerView = root.findViewById(R.id.competitionEventRecyclerView);

        CompetitionEventAdapter competitionEventAdapter = new CompetitionEventAdapter(competitionEventList, position -> Toast.makeText(getContext(), competitionEventList.get(position).eventId, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager competitionEventLayoutManager = new LinearLayoutManager(requireContext());

        competitionEventRecyclerView.setLayoutManager(competitionEventLayoutManager);
        competitionEventRecyclerView.setAdapter(competitionEventAdapter);


        // Get competition ID
        Intent intent = requireActivity().getIntent();
        String comp_id = intent.getStringExtra("comp_id");

        TextView eventCountTextView = root.findViewById(R.id.eventCountTextView);

        CollectionReference competition_schedule = db.collection("competition_details").document(comp_id).collection("schedule");
        competition_schedule.get().addOnCompleteListener(task -> {

            // Set number of events
            Log.d("CC_COMP_SCHEDULE", "Number of events : " + task.getResult().size());
            eventCountTextView.setText(task.getResult().size() + " Events");

            competitionEventList.clear();

            // Individual events are obtained here
            for(QueryDocumentSnapshot event : task.getResult())
            {
                CollectionReference event_rounds = db.collection("competition_details").document(comp_id).collection("schedule").document(event.getId()).collection("rounds");
                event_rounds.get().addOnCompleteListener(innerTask -> {

                    // Create a new event instance
                    CompetitionEvent compEvent = new CompetitionEvent(event.getId());
                    Log.d("CC_COMP_SCHEDULE", "Event ID : " + compEvent.eventId + " Round Count : " + innerTask.getResult().size());

                    // Individual rounds for each event are obtained here
                    for(QueryDocumentSnapshot round : innerTask.getResult())
                    {
                        EventRound eventRound = new EventRound(round.getLong("participant_count"), round.getTimestamp("start_time"), round.getTimestamp("end_time"));
                        compEvent.eventRounds.add(eventRound);
                    }
                    competitionEventList.add(compEvent);
                    competitionEventAdapter.notifyDataSetChanged();
                });
            }
        });

        return root;
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }

}
