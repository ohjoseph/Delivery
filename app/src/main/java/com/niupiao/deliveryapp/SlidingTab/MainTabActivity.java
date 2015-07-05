package com.niupiao.deliveryapp.SlidingTab;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.astuetz.PagerSlidingTabStrip;
import com.niupiao.deliveryapp.Deliveries.Delivery;
import com.niupiao.deliveryapp.Login.LoginActivity;
import com.niupiao.deliveryapp.R;
import com.niupiao.deliveryapp.Tabs.InProgressFragment;
import com.niupiao.deliveryapp.Tabs.ListingsFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by ohjoseph on 6/22/2015.
 *
 * Main Activity of the application. Handles the three primary fragments
 * in swipeabe tab format.
 */
public class MainTabActivity extends ActionBarActivity {

    ViewPager mViewPager; // Viewpager for hosted tabs
    ViewPagerAdapter mAdapter; // Adapter for viewpager
    PagerSlidingTabStrip tabs; // Tab layout
    CharSequence titles[] = {"Listings", "Current", "Map"}; // Titles of tabs
    final int numTabs = 3;
    ArrayList<Delivery> mCurrentList; // List of currently selected tab
    ArrayAdapter<Delivery> mCurAdapter;
    Fragment curFragment; // Currently selected fragment

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        getSupportActionBar().setElevation(6);
        getSupportActionBar().setTitle("Delivery");
        // Initialize variables
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, numTabs);
        mViewPager = (ViewPager) findViewById(R.id.delivery_list_viewPager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(3);
        // Define parameters for sliding tab strip
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setIndicatorColor(getResources().getColor(R.color.ColorPrimarySlightlyDark));
        tabs.setShouldExpand(true);
        tabs.setUnderlineHeight(1);
        tabs.setUnderlineColor(getResources().getColor(R.color.ColorPrimarySlightlyDark));
        tabs.setTextColor(getResources().getColor(android.R.color.white));
        tabs.setDividerColor(getResources().getColor(R.color.material_blue_grey_800));
        tabs.setViewPager(mViewPager);
        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // Show and hide nav bar on different tabs
                curFragment = getSupportFragmentManager().findFragmentByTag("android:switcher:"
                        + R.id.delivery_list_viewPager + ":" + mViewPager.getCurrentItem());
                if (position == 2) {
                    getSupportActionBar().hide();
                } else {
                    // Update data on page change
                    getSupportActionBar().show();
                    if (position == 0) {
                        ((ListingsFragment) curFragment).updateListings(false);
                    } else if (position == 1) {
                        ((InProgressFragment) curFragment).updateMyDeliveries(false);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    // Handle callbacks from child Activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ListingsFragment.DELIVERY_DETAILS) { // if Delivery was opened from listings
            if (resultCode == RESULT_OK) {
                // Refresh listings
                mViewPager.setCurrentItem(1);
                ((InProgressFragment) curFragment).updateMyDeliveries(false);
            }
        } else if (requestCode == InProgressFragment.PROGRESS_DELIVERY) { // if Delivery was opened from Claimed
            if (resultCode == RESULT_OK) {
                mViewPager.setCurrentItem(1); // Return to claimed list
            }
        }
    }

    // Inflate the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_tab, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Handles menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_sort_distance: // if distance sort
                Collections.sort(mCurrentList, new Comparator<Delivery>() {
                    public int compare(Delivery d1, Delivery d2) {
                        if (d1.getDistance() < d2.getDistance())
                            return -1;
                        if (d1.getDistance() > d2.getDistance())
                            return 1;
                        else
                            return 0;
                    }
                });
                mCurAdapter.notifyDataSetChanged(); // Update list
                return true;

            case R.id.menu_sort_wage: // if wage sort
                Collections.sort(mCurrentList, new Comparator<Delivery>() {
                    public int compare(Delivery d1, Delivery d2) {
                        if (d1.getWage() < d2.getWage())
                            return 1;
                        if (d1.getWage() > d2.getWage())
                            return -1;
                        else
                            return 0;
                    }
                });
                mCurAdapter.notifyDataSetChanged();
                return true;

            case R.id.menu_sort_time: // if time sort
                Collections.sort(mCurrentList, new Comparator<Delivery>() {
                    public int compare(Delivery d1, Delivery d2) {
                        if (d1.getPickupTime() < d2.getPickupTime())
                            return -1;
                        if (d1.getPickupTime() > d2.getPickupTime())
                            return 1;
                        else
                            return 0;
                    }
                });
                mCurAdapter.notifyDataSetChanged(); // Update list
                return true;
            case R.id.log_out:
                // Logs the user out
                Intent i = new Intent(this, LoginActivity.class);
                i.putExtra("Logged Out", true);
                startActivity(i);
                finish();
            default:
                return false;
        }
    }

    // Sets the current list
    public void setCurrentList(ArrayAdapter<Delivery> curAdapter, ArrayList<Delivery> curList) {
        this.mCurAdapter = curAdapter;
        this.mCurrentList = curList;
    }
}

/*
in oncreate()

        final FloatingActionMenu sortMenu = (FloatingActionMenu) findViewById(R.id.menu_sort);
        sortMenu.setClosedOnTouchOutside(true);

        final FloatingActionButton bountySort = (FloatingActionButton) findViewById(R.id.menu_item_sort_bounty);
        bountySort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(mCurrentList, new Comparator<Delivery>() {
                    public int compare(Delivery d1, Delivery d2) {
                        if (d1.getWage() < d2.getWage())
                            return 1;
                        if (d1.getWage() > d2.getWage())
                            return -1;
                        else
                            return 0;
                    }
                });

                mCurAdapter.notifyDataSetChanged();
                sortMenu.close(true);
            }
        });

        final FloatingActionButton timeSort = (FloatingActionButton) findViewById(R.id.menu_item_sort_time);
        timeSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sortMenu.close(true);
            }
        });

        final FloatingActionButton distanceSort = (FloatingActionButton) findViewById(R.id.menu_item_sort_distance);
        distanceSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sortMenu.close(true);
            }
        });
        */
