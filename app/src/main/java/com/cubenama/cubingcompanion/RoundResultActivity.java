package com.cubenama.cubingcompanion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
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
        TextView singleHeaderTextView = findViewById(R.id.bestSingleHeaderTextView);
        TextView finalResultHeaderTextView = findViewById(R.id.finalResultHeaderTextView);
        TextView noResultsTextView = findViewById(R.id.noResultsTextView);

        ImageView oopsImageView = findViewById(R.id.oopsImageView);
        ConstraintLayout resultDetailsHeaderLayout = findViewById(R.id.resultDetailsHeaderLayout);

        if(getIntent().getStringExtra("result_calc_method").equals("Single"))
            singleHeaderTextView.setVisibility(View.INVISIBLE);
        finalResultHeaderTextView.setText(getIntent().getStringExtra("result_calc_method"));

        eventNameTextView.setText(getIntent().getStringExtra("event_name"));
        roundIdTextView.setText("Round " + getIntent().getLongExtra("round_no", 1));

        // Setup recycler views and adapters
        List<RoundResult> roundResultList = new ArrayList<>();
        RecyclerView roundResultRecyclerView = findViewById(R.id.roundResultRecyclerView);

        RoundResultAdapter roundResultAdapter = new RoundResultAdapter(roundResultList, position -> Toast.makeText(this, roundResultList.get(position).wcaId, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager roundResultLayoutManager = new LinearLayoutManager(this);

        roundResultRecyclerView.setLayoutManager(roundResultLayoutManager);
        roundResultRecyclerView.setAdapter(roundResultAdapter);

        // Show loading screen
        LoadingScreenController loadingScreenController = new LoadingScreenController(this);
        loadingScreenController.showLoadingScreen(getString(R.string.loading_screen_msg_6));

        CollectionReference resultDetailsReference = db.collection(getString(R.string.db_field_name_comp_details))
                .document(getIntent().getStringExtra("comp_id"))
                .collection(getString(R.string.db_field_name_events))
                .document(getIntent().getStringExtra("event_id"))
                .collection(getString(R.string.db_field_name_rounds))
                .document(getIntent().getStringExtra("round_id"))
                .collection(getString(R.string.db_field_name_results));

        resultDetailsReference.orderBy(getString(R.string.db_field_name_final_result), Query.Direction.ASCENDING).addSnapshotListener((snapshot, e) -> {
                    // No results found
                    if(snapshot.isEmpty())
                    {
                        Log.d("CC_RESULTS", "No results found for the round.");
                        // Dismiss loading screen
                        loadingScreenController.dismissLoadingScreen();
                        resultDetailsHeaderLayout.setVisibility(View.GONE);
                        oopsImageView.setVisibility(View.VISIBLE);
                        noResultsTextView.setVisibility(View.VISIBLE);
                        return;
                    }
                    List<RoundResult> podiumResultList = new ArrayList<>();
                    roundResultList.clear();
                    long podiumCount = 3;
                    for(QueryDocumentSnapshot result : snapshot)
                    {
                        RoundResult roundResult = new RoundResult(result.getString(getString(R.string.db_field_name_name)), result.getString(getString(R.string.db_field_name_wca_id)), result.getLong(getString(R.string.db_field_name_final_result)), result.getLong(getString(R.string.db_field_name_single)), (ArrayList<Long>) result.get(getString(R.string.db_field_name_time_list)), result.getBoolean(getString(R.string.db_field_name_is_verified)));
                        // Add to separate list for top 3 verified results
                        if(roundResult.isVerified && podiumCount != 0)
                        {
                            podiumResultList.add(roundResult);
                            podiumCount --;
                        }
                        else
                            roundResultList.add(roundResult);
                    }
                    // Add verified results at the beginning
                    roundResultList.addAll(0, podiumResultList);
                    roundResultAdapter.notifyDataSetChanged();
                    // Dismiss loading screen
                    loadingScreenController.dismissLoadingScreen();
                    resultDetailsHeaderLayout.setVisibility(View.VISIBLE);
                    noResultsTextView.setVisibility(View.GONE);
                    oopsImageView.setVisibility(View.GONE);
        });
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }



}
