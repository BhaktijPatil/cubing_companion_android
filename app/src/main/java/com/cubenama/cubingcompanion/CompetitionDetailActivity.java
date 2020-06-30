package com.cubenama.cubingcompanion;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.cubenama.cubingcompanion.competitionui.CompetitionCompetitorsFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionInformationFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionResultsFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionScheduleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CompetitionDetailActivity extends AppCompatActivity {

    public LoadingScreenController loadingScreenController;

    // Initialize all fragments
    final Fragment infoFragment = new CompetitionInformationFragment();
    final Fragment scheduleFragment = new CompetitionScheduleFragment();
    final Fragment competitorsFragment = new CompetitionCompetitorsFragment();
    final Fragment resultsFragment = new CompetitionResultsFragment();

    Fragment currFragment = infoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition_detail);

        // Initialize loading screen
        loadingScreenController = new LoadingScreenController(this);

        // Initialize fragment manager
        final FragmentManager fm = getSupportFragmentManager();

        fm.beginTransaction().add(R.id.fragmentContainerLayout, scheduleFragment, "2").hide(scheduleFragment).commit();
        fm.beginTransaction().add(R.id.fragmentContainerLayout, competitorsFragment, "3").hide(competitorsFragment).commit();
        fm.beginTransaction().add(R.id.fragmentContainerLayout, resultsFragment, "4").hide(resultsFragment).commit();
        fm.beginTransaction().add(R.id.fragmentContainerLayout, infoFragment, "1").commit();

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
}
