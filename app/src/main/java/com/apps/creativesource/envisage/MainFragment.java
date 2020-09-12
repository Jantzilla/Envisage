package com.apps.creativesource.envisage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainFragment extends Fragment {

    private TabAdapter tabAdapter;
    @BindView(R.id.tl_tab_layout) TabLayout tabLayout;
    @BindView(R.id.vp_view_pager) ViewPager viewPager;

    private boolean first = true;
    private BroadcastReceiver updateUIReciver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        if(getActivity().getIntent().hasExtra("orientation")) {
            first = false;
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean("first", first);

        tabAdapter = new TabAdapter(getActivity().getSupportFragmentManager());

        TrendsFragment TrendsFragment = new TrendsFragment();
        TrendsFragment.setArguments(bundle);

        EventsFragment eventsFragment = new EventsFragment();
        eventsFragment.setArguments(bundle);

        tabAdapter.addFragment(eventsFragment, getString(R.string.events));
        tabAdapter.addFragment(TrendsFragment, getString(R.string.trends));

        viewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(MainActivity.tabIndex);

        IntentFilter filter = new IntentFilter();

        filter.addAction("main.update.action");

        updateUIReciver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                viewPager.setCurrentItem(0);
                eventsFragment.getAllPosts();
            }
        };

        getActivity().registerReceiver(updateUIReciver,filter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.setCurrentItem(MainActivity.tabIndex, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(updateUIReciver);
    }
}
