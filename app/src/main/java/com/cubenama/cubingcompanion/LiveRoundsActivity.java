package com.cubenama.cubingcompanion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cubenama.cubingcompanion.competitionui.CompetitionEventRound;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveRoundsActivity extends AppCompatActivity {

    // Database reference
    private FirebaseFirestore db;

    // Shared preferences to store user details
    SharedPreferences userDetailsSharedPreferences;

    CollectionReference eventsReference;
    CollectionReference resultsReference;

    String compId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_round);

        final long UPCOMING_ROUND_OFFSET = 36000;

        // Initialize loading screen
        LoadingScreenController loadingScreenController = new LoadingScreenController(this);
        loadingScreenController.showLoadingScreen(getString(R.string.loading_screen_msg_1));

        // Get competition ID
        Intent intent = getIntent();
        compId = intent.getStringExtra("comp_id");

        // Get current time
        Calendar calendar = Calendar.getInstance();
        long currTime = calendar.getTimeInMillis()/1000;

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Create reference to cuber details
        userDetailsSharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);

        // Initialize live round
        TextView eventNameTextView = findViewById(R.id.eventNameTextView);
        TextView roundNameTextView = findViewById(R.id.roundNameTextView);
        TextView formatTextView = findViewById(R.id.formatTextView);
        TextView qualificationCriteriaTextView = findViewById(R.id.qualificationCriteriaTextView);
        TextView roundTimeTextView = findViewById(R.id.roundTimeTextView);
        TextView noRoundLiveTextView = findViewById(R.id.noRoundLiveTextView);
        TextView noUpcomingRoundTextView = findViewById(R.id.noUpcomingRoundsTextView);

        CardView eventDetailsCardView = findViewById(R.id.eventDetailsCardView);

        Button beginButton = findViewById(R.id.beginButton);

        // Setup recycler views and adapters
        List<CompetitionEventRound> upcomingRoundList = new ArrayList<>();

        RecyclerView upcomingRoundRecyclerView = findViewById(R.id.upcomingRoundsRecyclerView);
        UpcomingRoundAdapter upcomingRoundAdapter = new UpcomingRoundAdapter(upcomingRoundList, position -> Toast.makeText(this, upcomingRoundList.get(position).eventName, Toast.LENGTH_SHORT).show());

        RecyclerView.LayoutManager upcomingRoundsLayoutManager = new LinearLayoutManager(this);

        upcomingRoundRecyclerView.setLayoutManager(upcomingRoundsLayoutManager);
        upcomingRoundRecyclerView.setAdapter(upcomingRoundAdapter);

        // Get competition details
        eventsReference = db.collection(getString(R.string.db_field_name_comp_details)).document(compId).collection(getString(R.string.db_field_name_events));
        eventsReference.get().addOnCompleteListener(eventTask -> {
            for (QueryDocumentSnapshot event : eventTask.getResult())
            {
                eventsReference.document(event.getId()).collection(getString(R.string.db_field_name_rounds)).get().addOnCompleteListener(roundTask -> {
                    for(QueryDocumentSnapshot round : roundTask.getResult())
                    {
                        CompetitionEventRound competitionEventRound = new CompetitionEventRound(event.getString(getString(R.string.db_field_name_name)), round.getLong(getString(R.string.db_field_name_id)), round.getId(), round.getLong(getString(R.string.db_field_name_qualification_criteria)), round.getTimestamp(getString(R.string.db_field_name_start_time)), round.getTimestamp(getString(R.string.db_field_name_end_time)));
                        // Set live round details
                        if(currTime > competitionEventRound.startTimestamp.getSeconds() && currTime < competitionEventRound.endTimestamp.getSeconds())
                        {
                            // Make live round visible
                            noRoundLiveTextView.setVisibility(View.GONE);
                            eventDetailsCardView.setVisibility(View.VISIBLE);
                            eventNameTextView.setVisibility(View.VISIBLE);

                            // Update live round info
                            eventNameTextView.setText(event.getString(getString(R.string.db_field_name_name)));
                            roundNameTextView.setText("Round " + round.getLong(getString(R.string.db_field_name_id)));
                            roundTimeTextView.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  hh:mm aa", round.getTimestamp(getString(R.string.db_field_name_start_time))) + " - " + new DateTimeFormat().firebaseTimestampToDate("hh:mm aa", round.getTimestamp(getString(R.string.db_field_name_end_time))));

                            // Set event format
                            switch (event.getString(getString(R.string.db_field_name_result_calc_method)))
                            {
                                case "Average" : formatTextView.setText("Average of " + event.getLong(getString(R.string.db_field_name_solve_count)));
                                    break;
                                case "Mean" : formatTextView.setText("Mean of " + event.getLong(getString(R.string.db_field_name_solve_count)));
                                    break;
                                case "Single" : formatTextView.setText("Single from " + event.getLong(getString(R.string.db_field_name_solve_count)));
                                    break;
                            }
                            
                            // Qualifying criteria for rounds
                            if(round.getLong(getString(R.string.db_field_name_id)) == 1)
                                qualificationCriteriaTextView.setText("Qualification Criteria : NA");
                            else
                                qualificationCriteriaTextView.setText("Qualification Criteria : Top " + competitionEventRound.qualificationCriteria);

                            // Confirmation dialog setup
                            final Dialog confirmationDialog = new Dialog(this);
                            confirmationDialog.setContentView(R.layout.dialog_box_confirm_begin);
                            confirmationDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                            Button cancelButton = confirmationDialog.findViewById(R.id.cancelButton);
                            Button beginConfirmButton = confirmationDialog.findViewById(R.id.confirmButton);

                            cancelButton.setOnClickListener(cancelButtonView -> confirmationDialog.dismiss());

                            beginButton.setOnClickListener(v -> {

                                // Check is automatic date & time zone are enabled
                                if (Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1 || Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0) != 1)
                                    Toast.makeText(this, "Enable automatic date and time-zone from settings to continue", Toast.LENGTH_LONG).show();
                                else
                                {
                                    // Button press on time
                                    if(Calendar.getInstance().getTimeInMillis()/1000 < competitionEventRound.endTimestamp.getSeconds()) {
                                        resultsReference = eventsReference.document(event.getId()).collection(getString(R.string.db_field_name_rounds)).document(round.getId()).collection(getString(R.string.db_field_name_results));
                                        // All competitors are eligible for round 1
                                        if(round.getLong(getString(R.string.db_field_name_id)) == 1)
                                            showConfirmationDialog(event, round);
                                        else
                                        {
                                            // Check if user has qualified for the round
                                            resultsReference.orderBy(getString(R.string.db_field_name_final_result), Query.Direction.ASCENDING).limit(competitionEventRound.qualificationCriteria).get().addOnCompleteListener(competitorTask -> {
                                                boolean isQualified = false;
                                                for(QueryDocumentSnapshot competitor : competitorTask.getResult())
                                                {
                                                    // Eligible for round
                                                    if(competitor.getId().equals(userDetailsSharedPreferences.getString("uid", ""))) {
                                                        showConfirmationDialog(event, round);
                                                        isQualified = true;
                                                        break;
                                                    }
                                                }
                                                // Not eligible
                                                if(!isQualified)
                                                    Toast.makeText(this, "You have not qualified for this round.", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    }
                                    // Button press after event has ended
                                    else
                                    {
                                        Toast.makeText(this, "Oops, too late !",Toast.LENGTH_LONG).show();
                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);
                                    }
                                }
                            });
                        }
                        // Add upcoming rounds
                        else if(currTime >= competitionEventRound.startTimestamp.getSeconds() - UPCOMING_ROUND_OFFSET && currTime < competitionEventRound.endTimestamp.getSeconds())
                            upcomingRoundList.add(competitionEventRound);

                        if(upcomingRoundList.isEmpty())
                        {
                            Log.d("CC_UPCOMING_ROUNDS", "No upcoming rounds found.");
                            noUpcomingRoundTextView.setVisibility(View.VISIBLE);
                        }

                        // Sort Rounds by start time
                        Collections.sort(upcomingRoundList, (round1, round2) -> Long.compare(round1.startTimestamp.getSeconds(), round2.startTimestamp.getSeconds()));
                        upcomingRoundAdapter.notifyDataSetChanged();
                    }
                    loadingScreenController.dismissLoadingScreen();
                });
            }
        });

    }



    private void showConfirmationDialog(QueryDocumentSnapshot event, QueryDocumentSnapshot round)
    {
        // Confirmation dialog
        final Dialog confirmationDialog = new Dialog(this);
        confirmationDialog.setContentView(R.layout.dialog_box_confirm_begin);
        confirmationDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button cancelButton = confirmationDialog.findViewById(R.id.cancelButton);
        Button beginConfirmButton = confirmationDialog.findViewById(R.id.confirmButton);

        cancelButton.setOnClickListener(cancelButtonView -> confirmationDialog.dismiss());
        // Begin solves
        beginConfirmButton.setOnClickListener(beginButtonView -> {
            // Retrieve solve ID
            resultsReference.document(userDetailsSharedPreferences.getString("uid", "")).get().addOnCompleteListener(resultDetailsTask -> {
                // Some solves are done
                if(resultDetailsTask.getResult().exists())
                {
                    ArrayList<Long> timeList = (ArrayList<Long>) resultDetailsTask.getResult().get(getString(R.string.db_field_name_time_list));
                    int i;
                    for(i = 0; i < timeList.size(); i ++)
                    {
                        Log.d("CC_PREV_TIMES", String.valueOf(timeList.get(i)));
                        // Find current solve ID
                        if(timeList.get(i).equals(ResultCodes.DNS_CODE))
                        {
                            beginTimerActivity(event, round, i);
                            confirmationDialog.dismiss();
                            break;
                        }
                    }
                    // Participant has already participated
                    if(i == timeList.size())
                        Toast.makeText(this, "You have already finished your solves.", Toast.LENGTH_SHORT).show();
                }
                // Participants first solve
                else {
                    // Warning for time
                    Toast.makeText(this, "We'll redirect you shortly.", Toast.LENGTH_SHORT).show();
                    // Make solves DNS
                    ArrayList<Long> timeList = new ArrayList<>();
                    for (int i = 0; i < event.getLong(getString(R.string.db_field_name_solve_count)); i++)
                        timeList.add(ResultCodes.DNS_CODE);
                    db.collection(getString(R.string.db_field_name_user_details)).document(userDetailsSharedPreferences.getString("uid", "")).get().addOnCompleteListener(userDetailsTask -> {

                        Map<String, Object> resultDetails = new HashMap<>();
                        resultDetails.put(getString(R.string.db_field_name_wca_id), userDetailsTask.getResult().getString(getString(R.string.db_field_name_wca_id)));
                        resultDetails.put(getString(R.string.db_field_name_name), userDetailsTask.getResult().getString(getString(R.string.db_field_name_name)));
                        resultDetails.put(getString(R.string.db_field_name_time_list), timeList);
                        resultDetails.put(getString(R.string.db_field_name_final_result), ResultCodes.DNS_CODE);
                        resultDetails.put(getString(R.string.db_field_name_single), ResultCodes.DNS_CODE);
                        resultDetails.put("isVerified", false);

                        eventsReference.document(event.getId()).collection(getString(R.string.db_field_name_rounds)).document(round.getId()).collection(getString(R.string.db_field_name_results)).document(userDetailsSharedPreferences.getString("uid", "")).set(resultDetails).addOnCompleteListener(newResultTask -> {
                            beginTimerActivity(event, round, 0);
                            confirmationDialog.dismiss();
                        });
                    });
                }
            });
        });
        confirmationDialog.show();
    }



    private void beginTimerActivity(QueryDocumentSnapshot event, QueryDocumentSnapshot round, int solveNo) {
            Intent timerIntent = new Intent(this, TimerActivity.class);

            timerIntent.putExtra("comp_id", compId);
            timerIntent.putExtra("event_id", event.getId());
            timerIntent.putExtra("round_id", round.getId());
            timerIntent.putExtra("solve_id", solveNo);
            timerIntent.putExtra(getString(R.string.db_field_name_result_calc_method), event.getString(getString(R.string.db_field_name_result_calc_method)));

            startActivity(timerIntent);
    }
}
