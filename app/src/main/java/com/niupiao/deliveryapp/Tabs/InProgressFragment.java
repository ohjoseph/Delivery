package com.niupiao.deliveryapp.Tabs;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.niupiao.deliveryapp.Deliveries.DataSource;
import com.niupiao.deliveryapp.Deliveries.Delivery;
import com.niupiao.deliveryapp.Deliveries.DeliveryFragment;
import com.niupiao.deliveryapp.Deliveries.DeliveryPagerActivity;
import com.niupiao.deliveryapp.Map.MapFragment;
import com.niupiao.deliveryapp.R;
import com.niupiao.deliveryapp.SlidingTab.MainTabActivity;
import com.niupiao.deliveryapp.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ohjoseph on 6/22/2015.
 * <p/>
 * Fragment that handles the list of claimed deliveries
 */
public class InProgressFragment extends ListFragment {
    // Request code to identify claimed list
    public static final int PROGRESS_DELIVERY = 240;

    public ArrayList<Delivery> mInProgress;
    public DeliveryAdapter mAdapter;
    AsyncTask asyncTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Initialize the data
        mInProgress = DataSource.get(getActivity()).getInProgress();
        updateArray();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_in_progress, container, false);
        // Set refresh on swipe down
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_in_progress);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Fetch new data here
                updateMyDeliveries(true);
            }
        });

        // Fetch new data from server
        updateMyDeliveries(false);

        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Delivery d = ((DeliveryAdapter) getListAdapter()).getItem(position);
        // Start new Activity with the selected Delivery's information
        Intent i = new Intent(getActivity(), DeliveryPagerActivity.class);
        i.putExtra(DeliveryFragment.EXTRA_DELIVERY_ID, d.getId());
        getActivity().startActivityForResult(i, PROGRESS_DELIVERY);
    }

    // Inner class to handle Delivery List View items
    private class DeliveryAdapter extends ArrayAdapter<Delivery> {

        public DeliveryAdapter(ArrayList<Delivery> deliveries) {
            super(getActivity(), 0, deliveries);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_view_current, null);
            }

            Delivery d = getItem(position);

            TextView puWage = (TextView) convertView.findViewById(R.id.dropoff_wage);
            puWage.setText("$" + d.getWage());

            TextView puAddress = (TextView) convertView.findViewById(R.id.pickup_address);
            puAddress.setText(d.getPickupAddress());

            TextView puTime = (TextView) convertView.findViewById(R.id.pickup_time);
            puTime.setText(d.getPickupTime() + " - " + (d.getPickupTime() + 3) + " AM");

            TextView puName = (TextView) convertView.findViewById(R.id.pickup_name);
            TextView puPhone = (TextView) convertView.findViewById(R.id.pickup_phone);

            TextView doDist = (TextView) convertView.findViewById(R.id.dropoff_distance);
            TextView doAddress = (TextView) convertView.findViewById(R.id.dropoff_address);
            doAddress.setText(d.getDropoffAddress());

            TextView doTime = (TextView) convertView.findViewById(R.id.dropoff_time);
            doTime.setText(d.getDropoffTime() + " - " + (d.getDropoffTime() + 3) + " PM");

            TextView doName = (TextView) convertView.findViewById(R.id.dropoff_name);
            TextView doPhone = (TextView) convertView.findViewById(R.id.dropoff_phone);

            // initialize list item view

            return convertView;
        }
    }

    // Refresh the local data to display
    private void updateArray() {
        DataSource.get(getActivity()).setInProgress(mInProgress);
        mAdapter = new DeliveryAdapter(mInProgress);
        setListAdapter(mAdapter);
        ((DeliveryAdapter) getListAdapter()).notifyDataSetChanged();
        ((MainTabActivity) getActivity()).setCurrentList(mAdapter, mInProgress);
    }

    // Fetch data from server
    public void updateMyDeliveries(boolean swiped) {
        String url = "https://niupiaomarket.herokuapp.com/delivery/claimed?format=json&key=" + DataSource.USER_KEY;
        JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                // Set returned data as new local data
                mInProgress = new ArrayList<Delivery>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject jObj = jsonArray.getJSONObject(i);
                        Delivery d = new Delivery(jObj);
                        mInProgress.add(d);
                    } catch (JSONException e) {
                        Log.e("JSON Object error: ", e.toString());
                        return;
                    }
                }
                // Update
                updateArray();
                ((SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_in_progress)).setRefreshing(false);
                // Automatically add new markers on the map
                asyncTask = ((MapFragment) getActivity().getSupportFragmentManager().
                        findFragmentByTag("android:switcher:" + R.id.delivery_list_viewPager + ":2"))
                        .updateMarkers();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), volleyError.toString(), Toast.LENGTH_LONG).show();
                return;
            }
        });
        // Time out request
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 3000, 1, 1.0f));
        VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (asyncTask != null)
            asyncTask.cancel(true);
    }
}
