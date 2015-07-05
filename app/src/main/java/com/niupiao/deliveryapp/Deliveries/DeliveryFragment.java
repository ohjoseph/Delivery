package com.niupiao.deliveryapp.Deliveries;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.niupiao.deliveryapp.R;
import com.niupiao.deliveryapp.VolleySingleton;

import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by Joseph on 6/22/15.
 *
 * Fragment that displays the full information of any given Delivery
 */
public class DeliveryFragment extends Fragment {

    public static final String EXTRA_DELIVERY_ID = "Delivery ID";

    private Delivery mDelivery;

    // New instance method with extras
    public static DeliveryFragment newInstance(Bundle extras) {
        DeliveryFragment frag = new DeliveryFragment();
        frag.setArguments(extras);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Gets specific delivery from DataSource
        UUID id = (UUID) getArguments().getSerializable(EXTRA_DELIVERY_ID);
        mDelivery = DataSource.get(getActivity()).getDelivery(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_delivery, parent, false);

        if (mDelivery.isClaimed()) { // Don't show button if already claimed
            RelativeLayout buttonParentView = (RelativeLayout) v.findViewById(R.id.button_view);
            buttonParentView.setVisibility(View.INVISIBLE);
        } else { // Show button if claimable
            Button claimButton = (Button) v.findViewById(R.id.claim_button);
            claimButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateClaimed(mDelivery);

                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            });
        }

        TextView puDist = (TextView) v.findViewById(R.id.pickup_distance);
        TextView puAddress = (TextView) v.findViewById(R.id.pickup_address);
        TextView puTime = (TextView) v.findViewById(R.id.pickup_time);
        TextView puName = (TextView) v.findViewById(R.id.pickup_name);
        TextView puPhone = (TextView) v.findViewById(R.id.pickup_phone);

        TextView doDist = (TextView) v.findViewById(R.id.dropoff_distance);
        TextView doAddress = (TextView) v.findViewById(R.id.dropoff_address);
        TextView doTime = (TextView) v.findViewById(R.id.dropoff_time);
        TextView doName = (TextView) v.findViewById(R.id.dropoff_name);
        TextView doPhone = (TextView) v.findViewById(R.id.dropoff_phone);

        return v;
    }

    // Handle options menu clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null)
                    NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Claim an item and update the server
    public void updateClaimed(Delivery d) {
        String url = "https://niupiaomarket.herokuapp.com/delivery/claim?format=json&key=" + DataSource.USER_KEY;
        url = url + "&delivery_id=" + d.getItemID();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getActivity(), "Claimed!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                });

        VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }
}
