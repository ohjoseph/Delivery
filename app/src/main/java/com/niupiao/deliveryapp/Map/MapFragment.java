package com.niupiao.deliveryapp.Map;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.niupiao.deliveryapp.Deliveries.DataSource;
import com.niupiao.deliveryapp.Deliveries.Delivery;
import com.niupiao.deliveryapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Inanity on 6/22/2015.
 */
public class MapFragment extends android.support.v4.app.Fragment implements LocationListener {
    private LocationManager mLocationManager;
    private String mProvider;
    private LatLng mCurLocation = new LatLng(47.92, 106.92);
    private MapView mMapView;
    private GoogleMap mMap;
    private ArrayList<Delivery> mDeliveries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the location manager
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use
        // default
        Criteria criteria = new Criteria();
        mProvider = mLocationManager.getBestProvider(criteria, false);
        Location location = mLocationManager.getLastKnownLocation(mProvider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + mProvider + " has been selected.");
            onLocationChanged(location);
        }

        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_map_tab, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMap = mMapView.getMap();
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mCurLocation).zoom(12).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        Button refreshButton = (Button) v.findViewById(R.id.refresh_markers);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateMarker().execute();
            }
        });

        // Perform any camera updates here
        updateMarkers();
        return v;
    }

    private LatLng getLatLongFromAddress(String address) {
        double lat = 0.0, lng = 0.0;

        Geocoder geoCoder = new Geocoder(this.getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
                LatLng p = new LatLng(
                        addresses.get(0).getLatitude(),
                        addresses.get(0).getLongitude());

                lat = p.latitude;
                lng = p.longitude;

                Log.d("Coordinates", "Lat: " + lat + " Long: " + lng);
                return p;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList createMarkers() {
        ArrayList<Delivery> array = DataSource.get(getActivity().getApplicationContext()).getInProgress();
        ArrayList<MarkerOptions> markers = new ArrayList<>();

        mMap = mMapView.getMap();
        for (Delivery d : array) {
            MarkerOptions pickupMarker = new MarkerOptions().position(
                    getLatLongFromAddress(d.getPickupAddress())).title(d.getPickupAddress() + " Pickup");

            MarkerOptions dropoffMarker = new MarkerOptions().position(
                    getLatLongFromAddress(d.getDropoffAddress())).title(d.getDropoffAddress() + " Drop Off");

            pickupMarker.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            dropoffMarker.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED));

            markers.add(pickupMarker);
            markers.add(dropoffMarker);
        }

        return markers;
    }

    private class UpdateMarker extends AsyncTask<Void, Void, ArrayList<MarkerOptions>> {
        ArrayList<MarkerOptions> markers;

        @Override
        protected ArrayList doInBackground(Void... params) {
            markers = createMarkers();
            return markers;
        }

        @Override
        protected void onPostExecute(ArrayList<MarkerOptions> markers) {
            for (MarkerOptions m : markers) {
                mMap.addMarker(m);
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public UpdateMarker updateMarkers() {
        UpdateMarker u = new UpdateMarker();
        u.execute();
        return u;
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocationManager.requestLocationUpdates(mProvider, 400, 1, this);
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null)
            mCurLocation = new LatLng(location.getLatitude(), location.getLongitude());
        else
            mCurLocation = new LatLng(47.92, 106.92);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}