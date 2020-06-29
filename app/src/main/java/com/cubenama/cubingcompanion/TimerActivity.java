package com.cubenama.cubingcompanion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class TimerActivity extends AppCompatActivity {

    // Database reference
    private FirebaseFirestore db;
    DocumentReference event;

    // Shared preferences to store user details
    SharedPreferences userDetailsSharedPreferences;

    private TextView timerTextView;
    private TextView solveIdTextView;
    private ConstraintLayout loaderLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        loaderLayout = findViewById(R.id.loaderLayout);

        // Show loading screen for
        showLoadingScreen("Decoding scrambles ...");

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Create reference to cuber details
        userDetailsSharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);

        timerTextView = findViewById(R.id.timeTextView);
        solveIdTextView = findViewById(R.id.solveIdTextView);

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

        // Reference to event
        event = db.collection("competition_details").document(getIntent().getStringExtra("comp_id")).collection("schedule").document(getIntent().getStringExtra("event_id"));
        event.get().addOnCompleteListener(task -> {
           // Get scrambles
           event.collection("rounds").document(getIntent().getStringExtra("round_id")).get().addOnCompleteListener(task1 -> {

               // Get round details
               roundEndTime = task1.getResult().getTimestamp("end_time").getSeconds() * 1000;
               scrambles = (ArrayList<String>) task1.getResult().get("scrambles");
               processSolve();
           });
        });
    }



    // Function to regulate process for one solve
    private void processSolve()
    {
        // DNF the current solve
        event.collection("rounds").document(getIntent().getStringExtra("round_id")).collection("results").document(userDetailsSharedPreferences.getString("uid", "")).get().addOnCompleteListener(task -> {
            ArrayList<Long> timeList = (ArrayList<Long>) task.getResult().get("time_list");
            timeList.set(solveId, ResultCodes.DNF_CODE);
            event.collection("rounds").document(getIntent().getStringExtra("round_id")).collection("results").document(userDetailsSharedPreferences.getString("uid", "")).update("time_list", timeList).addOnCompleteListener(task1 -> {
                // Dimiss loading screen
                dismissLoadingScreen();
                // Reset timer
                timerTextView.setText(R.string.default_time);
                // Make upload button invisible
                uploadSolveButton.setVisibility(View.GONE);
                // Set solve ID
                solveIdTextView.setText("Solve " + (solveId + 1));

                if(solveId < scrambles.size())
                {
                    // Listener for +2
                    plusTwoButton.setOnClickListener(v3 -> Toast.makeText(this, "Finish the solve to add a penalty.", Toast.LENGTH_SHORT).show());
                    // Listener for DNF
                    dnfButton.setOnClickListener(v3 -> Toast.makeText(this, "Finish the solve to add a penalty.", Toast.LENGTH_SHORT).show());

                    // Show current scramble to user
                    createScramblePopup(scrambles.get(solveId), solveId);

                    // Code to measure time
                    Handler timerHandler = new Handler();

                    timerTouchArea.setOnLongClickListener(v -> {
                        timerTextView.setTextColor(getResources().getColor(R.color.colorTextPrimaryLight, null));
                        timerTouchArea.setOnClickListener(v1 -> {
                            long startTime = System.nanoTime();
                            Runnable updateTimerThread = new Runnable() {
                                public void run() {
                                    long elapsedMilliseconds = (System.nanoTime() - startTime)/1000000;
                                    updateTime(elapsedMilliseconds, timerTextView);
                                    timerHandler.postDelayed(this, 10);
                                }
                            };
                            timerTouchArea.setOnClickListener(v2 -> {
                                // Stop timer
                                timerHandler.removeCallbacks(updateTimerThread);
                                // Make the timer unclickable
                                timerTouchArea.setLongClickable(false);
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
                        return false;
                    });

                    // Listener for show scramble button
                    showScrambleButton.setOnClickListener(v-> createScramblePopup(scrambles.get(solveId), solveId));
                }
            });
        });
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
        DateFormat dateFormat = new SimpleDateFormat("mm:ss.SS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
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
        showLoadingScreen("Uploading result ...");

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
            event.collection("rounds").document(getIntent().getStringExtra("round_id")).collection("results").document(userDetailsSharedPreferences.getString("uid", "")).get().addOnCompleteListener(task -> {
                ArrayList<Long> timeList = (ArrayList<Long>) task.getResult().get("time_list");
                timeList.set(solveId, solveTime.equals("DNF") ? ResultCodes.DNF_CODE : convertTime(solveTime));
                event.collection("rounds").document(getIntent().getStringExtra("round_id")).collection("results").document(userDetailsSharedPreferences.getString("uid", "")).update("time_list", timeList).addOnCompleteListener(task1 -> {
                    // Load next scramble
                    solveId += 1;
                    processSolve();
                });
            });
        }
    }


    // Function to update time to text view
    private void updateTime(long updatedTime, TextView textView) {

        DateFormat dateFormat = new SimpleDateFormat("mm:ss.SS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        textView.setText(dateFormat.format(updatedTime));
    }



    // Function to show loader
    private void showLoadingScreen(String loadingMessage)
    {
        loaderLayout.setVisibility(View.VISIBLE);
        // Load spinning cube GIF
        ImageView loadingGifView = findViewById(R.id.loadingGif);
        Glide.with(this).asGif().load(R.drawable.cube_loading_3).into(loadingGifView);
        // Set loader message
        TextView loadingMessageTextView = findViewById(R.id.loadingMessageTextView);
        loadingMessageTextView.setText(loadingMessage);
    }



    // Function to dismiss loader
    private void dismissLoadingScreen()
    {
        loaderLayout.setVisibility(View.GONE);
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
