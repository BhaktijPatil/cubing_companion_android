package com.cubenama.cubingcompanion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RoundResultActivity extends AppCompatActivity {

    // Database reference
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_result);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        TextView eventNameTextView = findViewById(R.id.eventNameTextView);
        TextView roundIdTextView = findViewById(R.id.roundIdTextView);
        TextView singleHeaderTextView = findViewById(R.id.singleHeaderTextView);
        TextView finalResultHeaderTextView = findViewById(R.id.finalResultHeaderTextView);

        if(getIntent().getStringExtra("result_calc_method").equals("Single"))
            singleHeaderTextView.setVisibility(View.INVISIBLE);
        finalResultHeaderTextView.setText(getIntent().getStringExtra("result_calc_method"));

        eventNameTextView.setText(getIntent().getStringExtra("event_name"));
        roundIdTextView.setText("Round " + getIntent().getStringExtra("round_name"));

        // Setup recycler views and adapters
        List<RoundResult> roundResultList = new ArrayList<>();
        RecyclerView roundResultRecyclerView = findViewById(R.id.roundResultRecyclerView);

        RoundResultAdapter roundResultAdapter = new RoundResultAdapter(roundResultList, position -> Toast.makeText(this, roundResultList.get(position).wcaId, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager roundResultLayoutManager = new LinearLayoutManager(this);

        roundResultRecyclerView.setLayoutManager(roundResultLayoutManager);
        roundResultRecyclerView.setAdapter(roundResultAdapter);

        // Show loading screen
        LoadingScreenController loadingScreenController = new LoadingScreenController(this);
        loadingScreenController.showLoadingScreen("Building up suspense ...");

        db.collection("competition_details")
                .document(getIntent().getStringExtra("comp_id"))
                .collection("schedule")
                .document(getIntent().getStringExtra("event_id"))
                .collection("rounds")
                .document(getIntent().getStringExtra("round_id"))
                .collection("results")
                .orderBy("result", Query.Direction.ASCENDING).addSnapshotListener((snapshot, e) -> {
                    roundResultList.clear();
                    for(QueryDocumentSnapshot result : snapshot)
                    {
                        RoundResult roundResult = new RoundResult(result.getString("name"), result.getString("wca_id"), result.getLong("result"), result.getLong("single"), (ArrayList<Long>) result.get("time_list"));
                        roundResultList.add(roundResult);
                    }
                    roundResultAdapter.notifyDataSetChanged();
                    // Dismiss loading screen
                    loadingScreenController.dismissLoadingScreen();
        });
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }



}
