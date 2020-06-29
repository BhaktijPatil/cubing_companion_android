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
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cubenama.cubingcompanion.R;
import com.cubenama.cubingcompanion.competitionui.CompetitionEvent;
import com.cubenama.cubingcompanion.competitionui.CompetitionEventAdapter;
import com.cubenama.cubingcompanion.competitionui.EventRound;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_round);

        final long UPCOMING_ROUND_OFFSET = 18000;

        // Get competition ID
        Intent intent = getIntent();
        String comp_id = intent.getStringExtra("comp_id");

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
        TextView roundFormatTextView = findViewById(R.id.roundFormatTextView);
        TextView qualificationCriteriaTextView = findViewById(R.id.qualificationCriteriaTextView);
        TextView roundTimeTextView = findViewById(R.id.roundTimeTextView);
        TextView noRoundLiveTextView = findViewById(R.id.noRoundLiveTextView);

        CardView eventDetailsCardView = findViewById(R.id.eventDetailsCardView);

        Button beginButton = findViewById(R.id.beginButton);

        // Setup recycler views and adapters
        List<EventRound> upcomingRoundList = new ArrayList<>();

        RecyclerView upcomingRoundRecyclerView = findViewById(R.id.upcomingRoundsRecyclerView);
        UpcomingRoundAdapter upcomingRoundAdapter = new UpcomingRoundAdapter(upcomingRoundList, position -> Toast.makeText(this, upcomingRoundList.get(position).eventName, Toast.LENGTH_SHORT).show());

        RecyclerView.LayoutManager upcomingRoundsLayoutManager = new LinearLayoutManager(this);

        upcomingRoundRecyclerView.setLayoutManager(upcomingRoundsLayoutManager);
        upcomingRoundRecyclerView.setAdapter(upcomingRoundAdapter);



        // Get competition details
        CollectionReference schedule = db.collection("competition_details").document(comp_id).collection("schedule");
        schedule.get().addOnCompleteListener(task -> {
            for (QueryDocumentSnapshot event : task.getResult())
            {
                schedule.document(event.getId()).collection("rounds").get().addOnCompleteListener(roundTask -> {
                    for(QueryDocumentSnapshot round : roundTask.getResult())
                    {
                        EventRound eventRound = new EventRound(event.getString("name"), round.getLong("participant_count"), round.getTimestamp("start_time"), round.getTimestamp("end_time"));
                        // Set live round details
                        if(currTime > eventRound.startTimestamp.getSeconds() && currTime < eventRound.endTimestamp.getSeconds())
                        {
                            noRoundLiveTextView.setVisibility(View.GONE);
                            eventDetailsCardView.setVisibility(View.VISIBLE);
                            eventNameTextView.setVisibility(View.VISIBLE);

                            // Update live round info
                            eventNameTextView.setText(event.getString("name"));
                            roundNameTextView.setText("Round : " + round.getLong("round_id"));
                            roundFormatTextView.setText("Best of " + event.getLong("solve_count"));
                            roundTimeTextView.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  HH:mm", round.getTimestamp("start_time")) + " - " + new DateTimeFormat().firebaseTimestampToDate("HH:mm", round.getTimestamp("end_time")));
                            qualificationCriteriaTextView.setText("Qualification criteria : Top " + round.getLong("participant_count"));


                            beginButton.setOnClickListener(v->{
                                // Button press on time
                                if(Calendar.getInstance().getTimeInMillis()/1000 < eventRound.endTimestamp.getSeconds())
                                {
                                    // Confirmation dialog
                                    final Dialog confirmationDialog = new Dialog(this);
                                    confirmationDialog.setContentView(R.layout.dialog_box_confirm_begin);
                                    confirmationDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


                                    Button cancelButton = confirmationDialog.findViewById(R.id.cancelButton);
                                    Button beginConfirmButton = confirmationDialog.findViewById(R.id.confirmButton);


                                    cancelButton.setOnClickListener(view -> confirmationDialog.dismiss());
                                    // Begin solves
                                    beginConfirmButton.setOnClickListener(view1 -> {
                                        // Retrieve solve ID
                                        schedule.document(event.getId()).collection("rounds").document(round.getId()).collection("results").document(userDetailsSharedPreferences.getString("uid", "")).get().addOnCompleteListener(task1 -> {
                                            // Some solves are done
                                            if(task1.getResult().exists())
                                            {
                                                ArrayList<Long> timeList = (ArrayList<Long>) task1.getResult().get("time_list");
                                                int i;
                                                for(i = 0; i < timeList.size(); i ++)
                                                {
                                                    Log.d("CC_PREV_TIMES", String.valueOf(timeList.get(i)));
                                                    // Find current solve ID
                                                    if(timeList.get(i).equals(ResultCodes.DNS_CODE))
                                                    {
                                                        Intent timerIntent = new Intent(this, TimerActivity.class);
                                                        timerIntent.putExtra("comp_id", comp_id);
                                                        timerIntent.putExtra("event_id", event.getId());
                                                        timerIntent.putExtra("round_id", round.getId());
                                                        timerIntent.putExtra("solve_id", i);
                                                        confirmationDialog.dismiss();
                                                        startActivity(timerIntent);
                                                        break;
                                                    }
                                                }
                                                // Participant has already participated
                                                if(i == timeList.size())
                                                    Toast.makeText(this, "You have already finished your solves.", Toast.LENGTH_SHORT).show();
                                            }
                                            // Participants first solve
                                            else {
                                                // Make solves DNS
                                                ArrayList<Long> timeList = new ArrayList<>();
                                                for (int i = 0; i < event.getLong("solve_count"); i++)
                                                    timeList.add(ResultCodes.DNS_CODE);
                                                db.collection("cuber_details").document(userDetailsSharedPreferences.getString("uid", "")).get().addOnCompleteListener(task2 -> {

                                                    Map<String, Object> resultDetails = new HashMap<>();
                                                    resultDetails.put("wca_id", task2.getResult().getString("wca_id"));
                                                    resultDetails.put("name", task2.getResult().getString("name"));
                                                    resultDetails.put("time_list", timeList);

                                                    schedule.document(event.getId()).collection("rounds").document(round.getId()).collection("results").document(userDetailsSharedPreferences.getString("uid", "")).set(resultDetails).addOnCompleteListener(task3 -> {
                                                        Intent timerIntent = new Intent(this, TimerActivity.class);
                                                        timerIntent.putExtra("comp_id", comp_id);
                                                        timerIntent.putExtra("event_id", event.getId());
                                                        timerIntent.putExtra("round_id", round.getId());
                                                        timerIntent.putExtra("solve_id", 0);
                                                        confirmationDialog.dismiss();
                                                        startActivity(timerIntent);
                                                    });
                                                });
                                                }
                                        });

                                    });

                                    confirmationDialog.show();
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
                            });
                        }
                        // Add upcoming rounds
                        else if(currTime >= eventRound.startTimestamp.getSeconds() - UPCOMING_ROUND_OFFSET && currTime < eventRound.endTimestamp.getSeconds())
                            upcomingRoundList.add(eventRound);
                        Log.d("CC_DA", String.valueOf(upcomingRoundList.size()));
                        // Sort Rounds by start time
                        Collections.sort(upcomingRoundList, (round1, round2) -> Long.compare(round1.startTimestamp.getSeconds(), round2.startTimestamp.getSeconds()));
                        upcomingRoundAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

    }
}
