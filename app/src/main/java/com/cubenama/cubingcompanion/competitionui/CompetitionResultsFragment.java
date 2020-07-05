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

    // Database reference
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_competition_results, container, false);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Show loading screen
        ((CompetitionDetailActivity)getActivity()).loadingScreenController.showLoadingScreen("Fetching result status ...");

        // Setup recycler views and adapters
        List<CompetitionEvent> competitionEventList = new ArrayList<>();
        RecyclerView competitionResultsRecyclerView = root.findViewById(R.id.competitionResultsRecyclerView);

        ResultEventAdapter resultEventAdapter = new ResultEventAdapter(competitionEventList, position -> Toast.makeText(getContext(), competitionEventList.get(position).eventId, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager resultEventLayoutManager = new LinearLayoutManager(requireContext());

        competitionResultsRecyclerView.setLayoutManager(resultEventLayoutManager);
        competitionResultsRecyclerView.setAdapter(resultEventAdapter);


        // Get competition ID
        Intent intent = requireActivity().getIntent();
        String comp_id = intent.getStringExtra("comp_id");

        TextView resultStatusTextView = root.findViewById(R.id.resultStatusTextView);

        DocumentReference competition = db.collection("competition_details").document(comp_id);
        competition.get().addOnCompleteListener(task -> {
            if (Calendar.getInstance().getTimeInMillis() > task.getResult().getTimestamp("start_time").getSeconds() * 1000)
            {
                // Competition is live
                if(Calendar.getInstance().getTimeInMillis() < task.getResult().getTimestamp("end_time").getSeconds() * 1000)
                {
                    resultStatusTextView.setText("Live");
                }
                // Competition is done
                else
                {
                    // Results verified
                    if(task.getResult().getBoolean("results_verified"))
                        resultStatusTextView.setText("Verified");
                    // Results not verified
                    else
                        resultStatusTextView.setText("Done (Verification pending)");
                }
            }
            // Competition hasn't started
            else
                resultStatusTextView.setText("To be declared ...");

            CollectionReference competition_schedule = competition.collection("schedule");
            competition_schedule.get().addOnCompleteListener(task1 -> {
                competitionEventList.clear();
                // Individual events are obtained here
                for(QueryDocumentSnapshot event : task1.getResult())
                {
                    CollectionReference event_rounds = db.collection("competition_details").document(comp_id).collection("schedule").document(event.getId()).collection("rounds");
                    event_rounds.orderBy("round_id").get().addOnCompleteListener(innerTask -> {

                        // Create a new event instance
                        CompetitionEvent compEvent = new CompetitionEvent(event.getId(), event.getString("name"), event.getLong("solve_count"), event.getString("result_calc_method"));
                        Log.d("CC_COMP_SCHEDULE", "Event ID : " + compEvent.eventId + " Round Count : " + innerTask.getResult().size());

                        // Individual rounds for each event are obtained here
                        for(QueryDocumentSnapshot round : innerTask.getResult())
                        {
                            EventRound eventRound = new EventRound(round.getId(), round.getLong("qualification_criteria"), round.getTimestamp("start_time"), round.getTimestamp("end_time"));
                            compEvent.eventRounds.add(eventRound);
                        }
                        competitionEventList.add(compEvent);

                        // Sort Events by name
                        Collections.sort(competitionEventList, (event1, event2) -> event1.eventName.compareTo(event2.eventName));
                        resultEventAdapter.notifyDataSetChanged();
                    });
                }
                // Dismiss loading screen
                ((CompetitionDetailActivity)getActivity()).loadingScreenController.dismissLoadingScreen();
            });
        });

        return root;
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}
