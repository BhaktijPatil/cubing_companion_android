package com.cubenama.cubingcompanion.dashboardui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.cubenama.cubingcompanion.R;
import com.google.android.material.appbar.AppBarLayout;

public class HomeFragment extends Fragment {

    private static final int NUM_PAGES = 5;

    // Handles animation and transition between cards
    private ViewPager mPager;
    // Provides cards to view pager.
    private PagerAdapter pagerAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = root.findViewById(R.id.cardPager);
        pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT );
        mPager.setAdapter(pagerAdapter);

        return root;
    }
}
