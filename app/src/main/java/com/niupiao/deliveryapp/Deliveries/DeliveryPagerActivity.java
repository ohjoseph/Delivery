package com.niupiao.deliveryapp.Deliveries;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import com.niupiao.deliveryapp.R;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Joseph on 6/22/15.
 *
 * Host Activity of DeliveryFragment
 * Enables swiping through Delivery views
 */
public class DeliveryPagerActivity extends ActionBarActivity {
    private ViewPager mViewPager;
    private ArrayList<Delivery> mDeliveries;
    private ArrayList<Delivery> mInProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_viewpager);
        // Initialize variables
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mDeliveries = DataSource.get(this).getDeliveries();
        mInProgress = DataSource.get(this).getInProgress();
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        // Set the adapter
        mViewPager.setAdapter(new FragmentPagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) { // Return selected DeliveryFragment
                Delivery d = mDeliveries.get(position);
                return DeliveryFragment.newInstance(getIntent().getExtras());
            }

            @Override
            public int getCount() {
                return mDeliveries.size();
            }
        });

        boolean goOn = true;
        UUID id = (UUID) getIntent().getSerializableExtra(DeliveryFragment.EXTRA_DELIVERY_ID);
        // Set the currently displayed item to be the selected item
        for (int i = 0; i < mInProgress.size(); i++) {
            if (mInProgress.get(i).getId().equals(id)) {
                mViewPager.setCurrentItem(i);
                goOn = false;
                break;
            }
        }
        if (goOn) { // Not found in first pass
            for (int i = 0; i < mDeliveries.size(); i++) {
                if (mDeliveries.get(i).getId().equals(id)) {
                    mViewPager.setCurrentItem(i);
                    break;
                }
            }
        }

        // Handles page changes
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Delivery d = mDeliveries.get(position);
                if (d.mName != null)
                    setTitle(d.mName);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }
}
