package com.apps.creativesource.envisage;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragmentArrayList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment) {
        fragmentArrayList.add(fragment);
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentArrayList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentArrayList.size();
    }
}