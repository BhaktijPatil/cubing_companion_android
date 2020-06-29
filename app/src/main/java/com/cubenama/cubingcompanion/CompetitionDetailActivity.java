package com.cubenama.cubingcompanion;

import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cubenama.cubingcompanion.competitionui.CompetitionCompetitorsFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionInformationFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionResultsFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionScheduleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CompetitionDetailActivity extends AppCompatActivity {

    private ConstraintLayout loaderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition_detail);

        Intent intent = getIntent();

        loaderLayout = findViewById(R.id.loaderLayout);

        // loading the default fragment
        loadFragment(new CompetitionInformationFragment());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavView);
        bottomNavigationView.getMenu().getItem(0).setCheckable(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment currFragment = null;
            switch (item.getItemId()) {
                case R.id.nav_information:
                    item.setCheckable(true);
                    currFragment = new CompetitionInformationFragment();
                    break;
                case R.id.nav_schedule:
                    currFragment = new CompetitionScheduleFragment();
                    break;
                case R.id.nav_competitors:
                    currFragment = new CompetitionCompetitorsFragment();
                    break;
                case R.id.nav_results:
                    currFragment = new CompetitionResultsFragment();
                    break;
            }
            return loadFragment(currFragment);
        });
    }



    // Function to load fragment into container
    private boolean loadFragment(Fragment fragment) {
        // Switch fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.competition_nav_host_fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }



    // Function to show loader
    public void showLoadingScreen(String loadingMessage)
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
    public void dismissLoadingScreen()
    {
        loaderLayout.setVisibility(View.GONE);
    }
}
