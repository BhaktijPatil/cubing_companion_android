package com.cubenama.cubingcompanion;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;

import com.cubenama.cubingcompanion.competitionui.CompetitionCompetitorsFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionInformationFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionResultsFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionScheduleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class CompetitionDetailActivity extends AppCompatActivity {

    public LoadingScreenController loadingScreenController;

    // Initialize all fragments
    private final Fragment infoFragment = new CompetitionInformationFragment();
    private final Fragment scheduleFragment = new CompetitionScheduleFragment();
    private final Fragment competitorsFragment = new CompetitionCompetitorsFragment();
    private final Fragment resultsFragment = new CompetitionResultsFragment();

    private FragmentManager fm;

    Fragment currFragment = infoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition_detail);

        // Initialize loading screen
        loadingScreenController = new LoadingScreenController(this);
        // Initialize fragment manager
        fm = getSupportFragmentManager();

        fm.beginTransaction().add(R.id.fragmentContainerLayout, infoFragment, "Information").commit();
        fm.beginTransaction().add(R.id.fragmentContainerLayout, scheduleFragment, "Schedule").hide(scheduleFragment).commit();
        fm.beginTransaction().add(R.id.fragmentContainerLayout, competitorsFragment, "Competitors").hide(competitorsFragment).commit();
        fm.beginTransaction().add(R.id.fragmentContainerLayout, resultsFragment, "Results").hide(resultsFragment).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavView);
        bottomNavigationView.getMenu().getItem(0).setCheckable(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_information:
                    item.setCheckable(true);
                    fm.beginTransaction().hide(currFragment).show(infoFragment).commit();
                    currFragment = infoFragment;
                    return true;

                case R.id.nav_schedule:
                    fm.beginTransaction().hide(currFragment).show(scheduleFragment).commit();
                    currFragment = scheduleFragment;
                    return true;

                case R.id.nav_competitors:
                    fm.beginTransaction().hide(currFragment).show(competitorsFragment).commit();
                    currFragment = competitorsFragment;
                    return true;

                case R.id.nav_results:
                    fm.beginTransaction().hide(currFragment).show(resultsFragment).commit();
                    currFragment = resultsFragment;
                    return true;
            }
            return false;
        });
    }



    @Override
    protected void onResume(){
        super.onResume();
        Log.d("CC_COMP_DETAILS" , "Visible fragment : " + currFragment.getTag());
    }
}
