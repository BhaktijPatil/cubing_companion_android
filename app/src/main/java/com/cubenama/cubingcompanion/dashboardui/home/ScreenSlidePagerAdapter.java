package com.cubenama.cubingcompanion.dashboardui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

    private final int TAB_COUNT = 2;



    public ScreenSlidePagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }



    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0: return new CompetitionsFragment();
            case 1: return new VersusModeFragment();
        }
        return null;
    }



    @Override
    public int getCount() {
        return TAB_COUNT;
    }
}
