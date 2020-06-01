package com.cubenama.cubingcompanion;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.cubenama.cubingcompanion.competitionui.CompetitionCompetitorsFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionInformationFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionResultsFragment;
import com.cubenama.cubingcompanion.competitionui.CompetitionScheduleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CompetitionDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition_detail);

        Intent intent = getIntent();

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

}
