package com.cubenama.cubingcompanion.competitionui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cubenama.cubingcompanion.BetterListAdapter;
import com.cubenama.cubingcompanion.DateTimeFormat;
import com.cubenama.cubingcompanion.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompetitionInformationFragment extends Fragment {

    // Database reference
    private FirebaseFirestore db;

    private SharedPreferences userDetailsSharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_competition_information, container, false);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Create reference to cuber details
        userDetailsSharedPreferences = requireActivity().getSharedPreferences("user_details", Context.MODE_PRIVATE);

        // Adapter for instructions list view
        BetterListAdapter podiumDetailsAdapter = new BetterListAdapter(requireActivity(), getResources().getStringArray(R.array.competition_instructions_podium_details));
        BetterListAdapter howToCompeteAdapter = new BetterListAdapter(requireActivity(), getResources().getStringArray(R.array.competition_instructions_how_to_compete_details));
        ListView howToCompeteInstructionsListView = root.findViewById(R.id.howToCompeteListView);
        ListView podiumDetailsListView = root.findViewById(R.id.podiumDetailsListView);
        podiumDetailsListView.setAdapter(podiumDetailsAdapter);
        howToCompeteInstructionsListView.setAdapter(howToCompeteAdapter);

        // Get competition ID
        Intent intent = requireActivity().getIntent();
        String comp_id = intent.getStringExtra("comp_id");

        TextView competitionNameTextView = root.findViewById(R.id.competitionNameTextView);
        TextView competitionTimeTextView = root.findViewById(R.id.competitionTimeValueTextView);
        TextView competitionEventsTextView = root.findViewById(R.id.competitionEventsValueTextView);
        TextView registrationBeginTextView = root.findViewById(R.id.registrationStartTextView);

        CardView registerButton = root.findViewById(R.id.registerCardView);
        CardView participateButton = root.findViewById(R.id.participateCardView);

        // Get competition details
        DocumentReference competition_info = db.collection("competition_details").document(comp_id);
        competition_info.get().addOnCompleteListener(task -> {

            Timestamp registrationStartTime = task.getResult().getTimestamp("registration_start");
            Timestamp compStartTime = task.getResult().getTimestamp("start_time");
            Timestamp compEndTime = task.getResult().getTimestamp("end_time");

            long competitor_limit =  task.getResult().getLong("competitor_limit");

            competitionNameTextView.setText(task.getResult().getString("name"));
            competitionTimeTextView.setText("From\t\t" + new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy hh:mm a", compStartTime) + "\nUntil\t\t" + new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy hh:mm a", compEndTime));
            registrationBeginTextView.setText("Registration opens on " + new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy hh:mm a", registrationStartTime));

            // Get current time
            Calendar calendar = Calendar.getInstance();
            long currTime = calendar.getTimeInMillis()/1000;

            // Registration is open
            if(currTime > registrationStartTime.getSeconds() && currTime < compStartTime.getSeconds())
            {
                db.collection("competition_details").document(comp_id).collection("competitors").addSnapshotListener((snapshot, e) -> {
                    // Check for exception
                    if (e != null) {
                        Log.w("CC_PROFILE_READ", "Unable to listen for data.", e);
                        return;
                    }
                    // Within competitor limit
                    if(snapshot.size() < competitor_limit)
                    {
                        registerButton.setOnClickListener(v->
                        {
                            boolean isRegistered = false;
                            for (QueryDocumentSnapshot competitor : snapshot)
                            {
                                // Check if user has already registered
                                if(competitor.getId().equals(userDetailsSharedPreferences.getString("uid", "")))
                                {
                                    Toast.makeText(requireContext(), "You have already registered.", Toast.LENGTH_SHORT).show();
                                    isRegistered = true;
                                    break;
                                }
                            }
                            // Register user for the event if he is eligible
                            if (!isRegistered)
                            {
                                registerButton.setCardBackgroundColor(requireActivity().getResources().getColor(R.color.colorPrimary, null));
                                db.collection("cuber_details").document(userDetailsSharedPreferences.getString("uid", "")).get().addOnCompleteListener(userDetailsTask->
                                {
                                    Map<String, Object> userDetails = new HashMap<>();
                                    userDetails.put("wca_id", userDetailsTask.getResult().getString("wca_id"));
                                    userDetails.put("name", userDetailsTask.getResult().getString("name"));
                                    db.collection("competition_details").document(comp_id).collection("competitors").document(userDetailsSharedPreferences.getString("uid", "")).set(userDetails).addOnCompleteListener(uploadTask->
                                    {
                                        Toast.makeText(requireContext(), "Registration successful.", Toast.LENGTH_SHORT).show();
                                        registerButton.setCardBackgroundColor(requireActivity().getResources().getColor(R.color.colorTextSecondary, null));
                                    });
                                });
                            }
                        });

                    }
                    // Competitor limit exceeded
                    else
                        registerButton.setOnClickListener(v-> Toast.makeText(requireContext(), "Competitor limit exceeded.", Toast.LENGTH_SHORT).show());
                });
            }
            // Registration time has expired
            else
                registerButton.setOnClickListener(v-> Toast.makeText(requireContext(), "Registration window has expired", Toast.LENGTH_SHORT).show());



        });

        // Get event list
        CollectionReference events_info = db.collection("competition_details").document(comp_id).collection("schedule");
        events_info.get().addOnCompleteListener( task ->
        {
            String events = "";
            Log.d("CC_", String.valueOf(task.getResult().size()));
            for(QueryDocumentSnapshot event : task.getResult())
            {
                events += event.getId() + "\n";
            }
            competitionEventsTextView.setText(events.substring(0,events.length()-1));
        });

        return root;
    }
}
