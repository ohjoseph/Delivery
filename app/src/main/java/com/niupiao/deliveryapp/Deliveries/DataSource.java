package com.niupiao.deliveryapp.Deliveries;

import android.content.Context;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Joseph on 6/22/15.
 *
 * Singleton class to hold data for the lifetime of the application.
 */
public class DataSource {
    private static DataSource sDataSource;
    private Context mAppContext;
    public static String USER_KEY; // Unique User key

    private ArrayList<Delivery> mDeliveries; // List of available deliveries
    private ArrayList<Delivery> mInProgress; // List of claimed deliveries

    private DataSource(Context appContext) {
        mAppContext = appContext;
        mInProgress = new ArrayList<Delivery>();
        mDeliveries = new ArrayList<Delivery>();
    }

    public static DataSource get(Context c) {
        if (sDataSource == null) { // Create new instance
            sDataSource = new DataSource(c.getApplicationContext());
        }
        return sDataSource;
    }

    public ArrayList<Delivery> getDeliveries() {
        return mDeliveries;
    }

    public ArrayList<Delivery> getInProgress() {
        return mInProgress;
    }

    // Find and returns a delivery
    public Delivery getDelivery(UUID id) {
        for (Delivery d : mInProgress) {
            if (d.getId().equals(id))
                return d;
        }
        for (Delivery d : mDeliveries) {
            if (d.getId().equals(id))
                return d;
        }
        return null;
    }

    // Update deliveries list
    public void setDeliveries(ArrayList<Delivery> deliveries) {
        mDeliveries = deliveries;
    }

    // Update claimed list
    public void setInProgress(ArrayList<Delivery> inProgress) {
        mInProgress = inProgress;
    }
}
