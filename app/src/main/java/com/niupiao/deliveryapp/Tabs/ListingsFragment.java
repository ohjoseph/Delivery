package com.niupiao.deliveryapp.Tabs;

import android.content.Intent;
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
import com.niupiao.deliveryapp.R;
import com.niupiao.deliveryapp.SlidingTab.MainTabActivity;
import com.niupiao.deliveryapp.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ohjoseph on 6/22/2015.
 *
 * ListFragment that handles the list of available deliveries
 */
public class ListingsFragment extends ListFragment {
    public static final int DELIVERY_DETAILS = 120;

    // Holds the data
    public ArrayList<Delivery> mDeliveries;
    public DeliveryAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Initialize data banks
        mDeliveries = DataSource.get(getActivity()).getDeliveries();
        updateArray();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_listings, container, false);
        // Refresh list on swipe down
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh_listings_view);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateListings(true);
                Toast.makeText(getActivity(), "Refreshed deliveries", Toast.LENGTH_SHORT).show();
            }
        });
        updateListings(false);

        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Delivery d = mAdapter.getItem(position);
        // Start new activity with information from the selected Delivery
        Intent i = new Intent(getActivity(), DeliveryPagerActivity.class);
        i.putExtra(DeliveryFragment.EXTRA_DELIVERY_ID, d.getId());
        getActivity().startActivityForResult(i, DELIVERY_DETAILS);
    }

    // Custom class to handle Delivery list view items
    public class DeliveryAdapter extends ArrayAdapter<Delivery> {

        public DeliveryAdapter(ArrayList<Delivery> deliveries) {
            super(getActivity(), 0, deliveries);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_view_delivery, null);
            }

            Delivery d = getItem(position);
            TextView bounty = (TextView) convertView.findViewById(R.id.list_item_bounty);
            bounty.setText("$" + d.getWage());
            TextView puTime = (TextView) convertView.findViewById(R.id.list_item_pickup_time);
            puTime.setText(d.getPickupTime() + " - " + (d.getPickupTime() + 3) + " AM");
            TextView puDistance = (TextView) convertView.findViewById(R.id.list_item_distance);
            puDistance.setText(d.getDistance() + " km");


            View statusColorView = convertView.findViewById(R.id.priority_indicator);
            switch (d.getDeliveryStatus()) {
                case Delivery.READY_FOR_PICKUP:
                    statusColorView.setBackgroundColor(getResources().getColor(R.color.ColorPrimary));
                    break;
                case Delivery.PICKED_UP:
                    statusColorView.setBackgroundColor(getResources().getColor(R.color.ColorPrimary));
                    break;
                case Delivery.DELIVERED:
                    statusColorView.setBackgroundColor(getResources().getColor(R.color.ColorPrimaryDark));
                    break;
            }

            return convertView;
        }
    }

    // Updates the information displayed on screen
    private void updateArray() {
        DataSource.get(getActivity()).setDeliveries(mDeliveries);
        mAdapter = new DeliveryAdapter(mDeliveries);
        setListAdapter(mAdapter);
        ((DeliveryAdapter) getListAdapter()).notifyDataSetChanged();
        ((MainTabActivity) getActivity()).setCurrentList(mAdapter, mDeliveries);
    }

    // Fetch new data from the server
    public void updateListings(boolean isSwiped) {
        String url = "https://niupiaomarket.herokuapp.com/delivery/index?format=json&key=" + DataSource.USER_KEY;
        JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                // Set server data as new local data
                mDeliveries = new ArrayList<Delivery>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject jObj = jsonArray.getJSONObject(i);
                        Delivery d = new Delivery(jObj);
                        mDeliveries.add(d);
                    } catch (JSONException e) {
                        Log.e("JSON Object error: ", e.toString());
                    }
                }

                updateArray();
                ((SwipeRefreshLayout) getView().findViewById(R.id.refresh_listings_view)).setRefreshing(false);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), volleyError.toString(), Toast.LENGTH_LONG).show();
                return;
            }
        });
        // Time out
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 3000, 1, 1.0f));
        VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }
}
