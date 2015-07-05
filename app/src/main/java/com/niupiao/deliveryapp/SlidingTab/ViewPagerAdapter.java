package com.niupiao.deliveryapp.SlidingTab;

import android.support.v4.app.FragmentPagerAdapter;

import com.niupiao.deliveryapp.Map.MapFragment;
import com.niupiao.deliveryapp.Tabs.InProgressFragment;
import com.niupiao.deliveryapp.Tabs.ListingsFragment;

/**
 * Created by Inanity on 6/22/2015.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    public CharSequence Titles[];
    public int numTabs;

    public ViewPagerAdapter(android.support.v4.app.FragmentManager fm, CharSequence mTitles[], int mNumTabs) {
        super(fm);

        this.Titles = mTitles;
        this.numTabs = mNumTabs;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        if (position == 0) {
            ListingsFragment dF = new ListingsFragment();
            return dF;
        }
        if (position == 1) {
            InProgressFragment mDF = new InProgressFragment();
            return mDF;
        } else {
            MapFragment mF = new MapFragment();
            return mF;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
