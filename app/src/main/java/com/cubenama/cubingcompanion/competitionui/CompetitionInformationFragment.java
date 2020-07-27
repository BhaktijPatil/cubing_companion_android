package com.cubenama.cubingcompanion.competitionui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cubenama.cubingcompanion.BetterListAdapter;
import com.cubenama.cubingcompanion.CompetitionDetailActivity;
import com.cubenama.cubingcompanion.DateTimeFormat;
import com.cubenama.cubingcompanion.LiveRoundsActivity;
import com.cubenama.cubingcompanion.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CompetitionInformationFragment extends Fragment {

    // Database reference
    private FirebaseFirestore db;

    private SharedPreferences userDetailsSharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_competition_information, container, false);

        // Show loading screen
        ((CompetitionDetailActivity)requireActivity()).loadingScreenController.showLoadingScreen(getString(R.string.loading_screen_msg_2));

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Create reference to cuber details
        userDetailsSharedPreferences = requireActivity().getSharedPreferences("user_details", Context.MODE_PRIVATE);

        // Adapter for instructions list view
        BetterListAdapter podiumDetailsAdapter = new BetterListAdapter(requireActivity(), getResources().getStringArray(R.array.competition_instructions_podium_details));
        BetterListAdapter howToCompeteAdapter = new BetterListAdapter(requireActivity(), getResources().getStringArray(R.array.competition_instructions_how_to_compete_details));
        BetterListAdapter howToUseTimerAdapter = new BetterListAdapter(requireActivity(), getResources().getStringArray(R.array.competition_instructions_using_the_timer_details));
        ListView howToCompeteInstructionsListView = root.findViewById(R.id.howToCompeteListView);
        ListView howToUseTimerListView = root.findViewById(R.id.howToUseTimerListView);
        ListView podiumDetailsListView = root.findViewById(R.id.podiumDetailsListView);
        podiumDetailsListView.setAdapter(podiumDetailsAdapter);
        howToCompeteInstructionsListView.setAdapter(howToCompeteAdapter);
        howToUseTimerListView.setAdapter(howToUseTimerAdapter);

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
        DocumentReference compDetailsReference = db.collection(getString(R.string.db_field_name_comp_details)).document(comp_id);
        CollectionReference competitorsReference = compDetailsReference.collection(getString(R.string.db_field_value_competitors));
        compDetailsReference.get().addOnCompleteListener(compDetailsTask -> {

            Timestamp registrationStartTime = compDetailsTask.getResult().getTimestamp(getString(R.string.db_field_name_reg_start_time));
            Timestamp registrationEndTime = compDetailsTask.getResult().getTimestamp(getString(R.string.db_field_name_reg_end_time));
            Timestamp compStartTime = compDetailsTask.getResult().getTimestamp(getString(R.string.db_field_name_start_time));
            Timestamp compEndTime = compDetailsTask.getResult().getTimestamp(getString(R.string.db_field_name_end_time));

            long competitor_limit =  compDetailsTask.getResult().getLong(getString(R.string.db_field_name_competitor_limit));

            competitionNameTextView.setText(compDetailsTask.getResult().getString(getString(R.string.db_field_name_name)));
            competitionTimeTextView.setText("From\t\t" + new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy hh:mm a", compStartTime) + "\nUntil\t\t" + new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy hh:mm a", compEndTime));
            registrationBeginTextView.setText("Registration opens on " + new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy hh:mm a", registrationStartTime) + " & closes on " + new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy hh:mm a", registrationEndTime));

            // Get current time
            Calendar calendar = Calendar.getInstance();
            long currTime = calendar.getTimeInMillis()/1000;

            // Participate Button logic
            // Competition is live
            if(currTime > compStartTime.getSeconds() && currTime < compEndTime.getSeconds())
            {
                competitorsReference.document(userDetailsSharedPreferences.getString("uid", "")).get().addOnCompleteListener(checkCompetitorTask ->{

                    // Dismiss loading screen
                    ((CompetitionDetailActivity)requireActivity()).loadingScreenController.dismissLoadingScreen();

                    // user has registered
                    if(checkCompetitorTask.getResult().exists())
                    {
                        participateButton.setCardBackgroundColor(requireActivity().getResources().getColor(R.color.colorPrimary, null));
                        participateButton.setOnClickListener(v-> {
                            // Check is automatic date & time zone are enabled
                            if (Settings.Global.getInt(requireActivity().getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1 || Settings.Global.getInt(requireActivity().getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0) != 1)
                                Toast.makeText(requireContext(), "Enable automatic date and time-zone from settings to continue", Toast.LENGTH_LONG).show();
                            // Show user the rounds that are live
                            else
                            {
                                Intent liveRoundsIntent = new Intent(requireActivity(), LiveRoundsActivity.class);
                                liveRoundsIntent.putExtra("comp_id", comp_id);
                                startActivity(liveRoundsIntent);
                            }
                        });
                    }
                    // user hasn't registered
                    else
                        participateButton.setOnClickListener(v-> Toast.makeText(requireContext(), "You have not registered for the competition.", Toast.LENGTH_LONG).show());
                });
            }
            // Competition is not live
            else {
                // Dismiss loading screen
                ((CompetitionDetailActivity)requireActivity()).loadingScreenController.dismissLoadingScreen();
                participateButton.setOnClickListener(v -> Toast.makeText(requireContext(), "Competition is not yet live.", Toast.LENGTH_LONG).show());
            }
            // Registration button logic
            // Registration is live
            if(currTime > registrationStartTime.getSeconds() && currTime < registrationEndTime.getSeconds())
            {
                competitorsReference.addSnapshotListener((snapshot, e) -> {
                    // Within competitor limit
                    if(snapshot.size() < competitor_limit) {
                        competitorsReference.document(userDetailsSharedPreferences.getString("uid", "")).addSnapshotListener((documentSnapshot, e1) -> {
                            // User has registered
                            if (documentSnapshot.exists())
                                registerButton.setOnClickListener(v -> Toast.makeText(requireContext(), "You have already registered.", Toast.LENGTH_SHORT).show());
                            // User hasn't registered
                            else {
                                registerButton.setCardBackgroundColor(requireActivity().getResources().getColor(R.color.colorPrimary, null));
                                registerButton.setOnClickListener(v ->
                                {
                                    // Check is automatic date & time zone are enabled
                                    if (Settings.Global.getInt(requireActivity().getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1 || Settings.Global.getInt(requireActivity().getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0) != 1) {
                                        Toast.makeText(requireContext(), "Enable automatic date and time-zone from settings to continue", Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    db.collection(getString(R.string.db_field_name_user_details)).document(userDetailsSharedPreferences.getString("uid", "")).get().addOnCompleteListener(userDetailsTask ->
                                    {
                                        Map<String, Object> userDetails = new HashMap<>();
                                        userDetails.put(getString(R.string.db_field_name_wca_id), userDetailsTask.getResult().getString(getString(R.string.db_field_name_wca_id)));
                                        userDetails.put(getString(R.string.db_field_name_name), userDetailsTask.getResult().getString(getString(R.string.db_field_name_name)));
                                        competitorsReference.document(userDetailsSharedPreferences.getString("uid", "")).set(userDetails).addOnCompleteListener(uploadTask ->
                                        {
                                            Toast.makeText(requireContext(), "Registration successful.", Toast.LENGTH_SHORT).show();
                                            registerButton.setCardBackgroundColor(requireActivity().getResources().getColor(R.color.colorTextSecondary, null));
                                            // Refresh activity
                                            requireActivity().finish();
                                            requireActivity().overridePendingTransition(0, 0);
                                            requireActivity().startActivity(requireActivity().getIntent());
                                            requireActivity().overridePendingTransition(0, 0);
                                        });
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
            // Registration is not live
            else
                registerButton.setOnClickListener(v-> Toast.makeText(requireContext(), "Registration is not live at the moment.", Toast.LENGTH_SHORT).show());
        });

        // Get event list (ordered by name)
        CollectionReference events_info = compDetailsReference.collection(getString(R.string.db_field_name_events));
        events_info.orderBy(getString(R.string.db_field_name_name)).get().addOnCompleteListener(eventNamesTask ->
        {
            String events = "";
            for(QueryDocumentSnapshot event : eventNamesTask.getResult())
            {
                events += event.getString(getString(R.string.db_field_name_name)) + "\n";
            }
            competitionEventsTextView.setText(events.substring(0,events.length()-1));
        });

        return root;
    }
}
