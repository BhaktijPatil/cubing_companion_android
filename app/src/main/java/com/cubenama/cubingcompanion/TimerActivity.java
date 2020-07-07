package com.cubenama.cubingcompanion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import static java.util.Collections.max;
import static java.util.Collections.min;

public class TimerActivity extends AppCompatActivity {

    DocumentReference eventDetailsReference;
    DocumentReference resultDetailsReference;

    // Shared preferences to store user details
    SharedPreferences userDetailsSharedPreferences;

    private TextView timerTextView;
    private TextView solveIdTextView;
    private TextView infoTextView;
    private CardView timerTouchArea;
    private ImageView infoButton;

    private Button dnfButton;
    private Button plusTwoButton;
    private Button showScrambleButton;
    private Button uploadSolveButton;

    private ArrayList<String> scrambles;
    private String solveTime;
    private int solveId;
    private long roundEndTime;

    private LoadingScreenController loadingScreenController;

    private DateFormat dateFormat;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        loadingScreenController = new LoadingScreenController(this);
        // Show loading screen for
        loadingScreenController.showLoadingScreen(getString(R.string.loading_screen_msg_4));

        dateFormat = new SimpleDateFormat("mm:ss.SS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));


        // Create database instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create reference to cuber details
        userDetailsSharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);

        timerTextView = findViewById(R.id.timeTextView);
        solveIdTextView = findViewById(R.id.solveIdTextView);
        infoTextView = findViewById(R.id.infoTextView);

        showScrambleButton = findViewById(R.id.showScrambleButton);
        plusTwoButton  = findViewById(R.id.plusTwoButton);
        dnfButton  = findViewById(R.id.dnfButton);
        uploadSolveButton = findViewById(R.id.uploadSolveButton);

        infoButton = findViewById(R.id.infoButton);

        // Get solve ID
        solveId = getIntent().getIntExtra("solve_id", 99);

        // Listener for info button
        infoButton.setOnClickListener(v -> showInfoPanel());

        timerTouchArea = findViewById(R.id.timerTouchArea);

        Log.d("CC_COMP_SOLVE", "Comp ID : " + getIntent().getStringExtra("comp_id") + " Event ID : " + getIntent().getStringExtra("event_id"));

        // Reference to eventDetailsReference
        eventDetailsReference = db.collection(getString(R.string.db_field_name_comp_details)).document(Objects.requireNonNull(getIntent().getStringExtra("comp_id"))).collection(getString(R.string.db_field_name_events)).document(Objects.requireNonNull(getIntent().getStringExtra("event_id")));
        resultDetailsReference = eventDetailsReference.collection(getString(R.string.db_field_name_rounds)).document(Objects.requireNonNull(getIntent().getStringExtra("round_id"))).collection(getString(R.string.db_field_name_results)).document(userDetailsSharedPreferences.getString("uid", ""));

        // Get scrambles
        eventDetailsReference.collection(getString(R.string.db_field_name_rounds)).document(Objects.requireNonNull(getIntent().getStringExtra("round_id"))).get().addOnCompleteListener(eventDetailsTask -> {
            // Get round details
            roundEndTime = eventDetailsTask.getResult().getTimestamp(getString(R.string.db_field_name_end_time)).getSeconds() * 1000;
            scrambles = (ArrayList<String>) eventDetailsTask.getResult().get(getString(R.string.db_field_name_scrambles));
            processSolve();
        });
    }



    // Function to regulate process for one solve
    private void processSolve()
    {
       if(solveId < scrambles.size()) {
            // DNF the current solve
            resultDetailsReference.get().addOnCompleteListener(retrieveTimesTask -> {
                ArrayList<Long> timeList = (ArrayList<Long>) retrieveTimesTask.getResult().get(getString(R.string.db_field_name_time_list));
                timeList.set(solveId, ResultCodes.DNF_CODE);
                resultDetailsReference.update(getString(R.string.db_field_name_time_list), timeList).addOnCompleteListener(uploadResultTask -> {
                    // Dimiss loading screen
                    loadingScreenController.dismissLoadingScreen();
                    // Reset timer
                    timerTextView.setText(R.string.default_time);
                    timerTextView.setTextColor(getColor(R.color.colorAccent));
                    // Make upload button invisible
                    uploadSolveButton.setVisibility(View.GONE);
                    // Set solve ID
                    solveIdTextView.setText("Solve " + (solveId + 1));
                    // Listener for +2
                    plusTwoButton.setOnClickListener(v3 -> Toast.makeText(this, R.string.penalty_unusable_warning, Toast.LENGTH_SHORT).show());
                    // Listener for DNF
                    dnfButton.setOnClickListener(v3 -> Toast.makeText(this, R.string.penalty_unusable_warning, Toast.LENGTH_SHORT).show());

                    // Show current scramble to user
                    createScramblePopup(scrambles.get(solveId), solveId);

                    // Code to measure time
                    Handler timerHandler = new Handler();

                    timerTouchArea.setOnLongClickListener(v -> {
                        timerTouchArea.setOnClickListener(v1 -> {
                            long startTime = System.nanoTime();
                            Runnable updateTimerThread = new Runnable() {
                                public void run() {
                                    long elapsedMilliseconds = (System.nanoTime() - startTime) / 1000000;
                                    updateTime(elapsedMilliseconds, timerTextView);
                                    timerHandler.postDelayed(this, 10);
                                }
                            };

                            timerTouchArea.setOnClickListener(v2 -> {
                                // Stop timer
                                timerHandler.removeCallbacks(updateTimerThread);
                                // Listener for +2
                                plusTwoButton.setOnClickListener(v3 -> plusTwoSolve());
                                // Listener for DNF
                                dnfButton.setOnClickListener(v3 -> dnfSolve());
                                // Listener for upload button
                                uploadSolveButton.setVisibility(View.VISIBLE);
                                uploadSolveButton.setOnClickListener(v3 -> uploadResult(roundEndTime));
                            });
                            timerHandler.postDelayed(updateTimerThread, 0);
                        });

                        timerTextView.setTextColor(getResources().getColor(R.color.colorTextPrimaryLight, null));
                        // Make the timer unclickable
                        timerTouchArea.setLongClickable(false);

                        return false;
                    });

                    // Listener for show scramble button
                    showScrambleButton.setOnClickListener(v -> createScramblePopup(scrambles.get(solveId), solveId));
                });
            });
        }
    }



    // Function to show final result
    private void showResult(long result)
    {
        // Set result into text view
        if(result != ResultCodes.DNF_CODE) {
            infoTextView.setText("Congratulations ! You have finished your solves with a " + getIntent().getStringExtra("result_calc_method") + " of");
            timerTextView.setText(dateFormat.format(result));
        }
        else {
            infoTextView.setText("Hard luck ! You have finished your solves with a " + getIntent().getStringExtra("result_calc_method") + " of");
            timerTextView.setText("DNF");
        }

        infoTextView.setVisibility(View.VISIBLE);

        // Convert upload button to back button
        uploadSolveButton.setText("Finish");
        uploadSolveButton.setOnClickListener(v->finish());
    }



    // Function to DNF the solve
    private void dnfSolve()
    {
        solveTime = timerTextView.getText().toString();
        timerTextView.setText("DNF");
        Toast.makeText(this, "DNF'd solve", Toast.LENGTH_SHORT).show();
        // Undo DNF
        dnfButton.setOnClickListener(v-> undoDNF());
    }



    // Function to undo DNF
    private void undoDNF()
    {
        // Restore previous time
        timerTextView.setText(solveTime);
        Toast.makeText(this, "Removed DNF.", Toast.LENGTH_SHORT).show();
        // Redo DNF
        dnfButton.setOnClickListener(v-> dnfSolve());
    }



    // Function to plus two a solve
    private void plusTwoSolve()
    {
        solveTime = timerTextView.getText().toString();
        timerTextView.setText(dateFormat.format(convertTime(solveTime) + 2000));
        Toast.makeText(this, "Added +2 Penalty.", Toast.LENGTH_SHORT).show();
        // Undo plus two
        plusTwoButton.setOnClickListener(v-> undoPlusTwo());
    }



    // Function to undo DNF
    private void undoPlusTwo()
    {
        // Restore previous time
        timerTextView.setText(solveTime);
        Toast.makeText(this, "Removed +2 Penalty.", Toast.LENGTH_SHORT).show();
        // Redo DNF
        plusTwoButton.setOnClickListener(v-> plusTwoSolve());
    }



    // Function to convert mm:ss.SS to milliseconds
    private long convertTime(String time)
    {
        long minutes = Long.parseLong(solveTime.substring(0,2));
        long seconds = Long.parseLong(solveTime.substring(3,5));
        long milliseconds = Long.parseLong(solveTime.substring(6,8)) * 10;
        // Calculate solve time in milliseconds
        return minutes * 60 * 1000 + seconds * 1000 + milliseconds;
    }


    // Function to upload results to firebase
    private void uploadResult(long roundEndTime)
    {
        // show loading screen
        loadingScreenController.showLoadingScreen(getString(R.string.loading_screen_msg_5));

        // Round has ended
        if(Calendar.getInstance().getTimeInMillis() > roundEndTime)
        {
            Toast.makeText(this, "Round has ended. Solve will be disregarded.", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Round still live
        else
        {
            solveTime = timerTextView.getText().toString();
            Log.d("CC_SOLVE_TIME", solveTime);

            // Upload time to DB
            resultDetailsReference.get().addOnCompleteListener(uploadResultTask -> {
                ArrayList<Long> timeList = (ArrayList<Long>) uploadResultTask.getResult().get(getString(R.string.db_field_name_time_list));
                timeList.set(solveId, solveTime.equals("DNF") ? ResultCodes.DNF_CODE : convertTime(solveTime));
                resultDetailsReference.update(getString(R.string.db_field_name_time_list), timeList).addOnCompleteListener(uploadFinalResultTask -> {
                    // Load next scramble
                    solveId += 1;
                    // Last solve
                    if(solveId == scrambles.size())
                    {
                        long result = calculateResult(timeList);
                        Map<String, Object> resultDetails = new HashMap<>();
                        resultDetails.put(getString(R.string.db_field_name_final_result), result);
                        resultDetails.put(getString(R.string.db_field_name_single), min(timeList));
                        eventDetailsReference.collection(getString(R.string.db_field_name_rounds)).document(Objects.requireNonNull(getIntent().getStringExtra("round_id"))).collection(getString(R.string.db_field_name_results)).document(userDetailsSharedPreferences.getString("uid", "")).update(resultDetails).addOnCompleteListener(task -> {
                            showResult(result);
                            loadingScreenController.dismissLoadingScreen();
                        });
                    }
                    else
                        processSolve();
                });
            });
        }
    }



    // Function to calculate final result
    private long calculateResult(ArrayList<Long> timeList) {
        switch (Objects.requireNonNull(getIntent().getStringExtra("result_calc_method")))
        {
            // Calculate average of solves
            case "Average" :
                ArrayList<Long> tempList = new ArrayList<>(timeList);
                // Remove max and min times
                tempList.remove(max(tempList));
                tempList.remove(min(tempList));
                if(tempList.contains(ResultCodes.DNF_CODE))
                    return ResultCodes.DNF_CODE;
                else
                    // Find mean of remaining solves
                    return mean(tempList);
            // Calculate mean of solves
            case "Mean" :
                if(timeList.contains(ResultCodes.DNF_CODE))
                    return ResultCodes.DNF_CODE;
                else
                    return mean(timeList);
            // Find best solve
            case "Single" :
                return min(timeList);
        }
        return ResultCodes.DNF_CODE;
    }



    // Function to find average of solves
    private long mean(ArrayList<Long> timeList)
    {
        long count = 0;
        long total = 0;
        for(long time : timeList)
        {
            total += time;
            count ++;
        }
        return total/count;
    }



    // Function to update time to text view
    private void updateTime(long updatedTime, TextView textView) {
        textView.setText(dateFormat.format(updatedTime));
    }



    // Function to create popup window for scramble
    private void createScramblePopup(String scramble , int solveId)
    {
        // Inflate the popup window layout (show scramble)
        View popupScrambleView = getLayoutInflater().inflate(R.layout.popup_scramble, null);
        // Setup popup window
        PopupWindow popupWindow = new PopupWindow(popupScrambleView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        // Show popup view at the center
        popupWindow.showAtLocation(popupScrambleView, Gravity.CENTER, 0, 0);
        // Exit popup window
        ImageButton exitButton = popupScrambleView.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> popupWindow.dismiss());
        // Set scramble
        TextView scrambleTitle = popupScrambleView.findViewById(R.id.scrambleTitleTextView);
        scrambleTitle.setText("Scramble " + (solveId + 1));
        TextView scrambleTextView = popupScrambleView.findViewById(R.id.scrambleTextView);
        scrambleTextView.setText(scramble);
    }



    // Function to show information panel
    private void showInfoPanel()
    {
        Toast.makeText(this, "Info panel coming soon.", Toast.LENGTH_SHORT).show();
    }



}
