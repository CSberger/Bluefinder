package com.digitalobstaclecourse.bluefinder;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FindCarMapFragment extends SupportMapFragment {
    private static final int DEFAULT_ZOOM = 15;
    private static final int PADDING = 15;
    public static final String DEVICE_KEY = "DEVICE_KEY";
    private static String TAG = "FindCarMapFragment";
    private String mCurrentlyDisplayedDevice = null;

    private static Location deserializeJSONToLocation(String ljson) {
        Gson gson = new Gson();
        return gson.fromJson(ljson, Location.class);
    }

    public FindCarMapFragment() {
        super();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(DEVICE_KEY, mCurrentlyDisplayedDevice);
        super.onSaveInstanceState(outState);

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentlyDisplayedDevice = savedInstanceState.getString(DEVICE_KEY);
        }
        //displayLocationsForDevice(mCurrentlyDisplayedDevice);

    }

    @Override
    public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
        View v = super.onCreateView(arg0, arg1, arg2);
        initMap();
        if (mCurrentlyDisplayedDevice != null) {
            displayLocationsForDevice(mCurrentlyDisplayedDevice);
        }
        return v;
    }

    private void initMap() {
        GoogleMap gm = this.getMap();

        if (gm == null) {
            //Log.e(TAG, "initMap Error");
            return;
        }
        gm.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (loc != null) {
            gm.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), DEFAULT_ZOOM));
        }
    }

    public void displayLocationsForDevice(String id) {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        //Log.d(TAG, String.format("displayLocationsForDevice %s", id));
        if (mCurrentlyDisplayedDevice != null) {
            getMap().clear();
        }
        DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(getActivity());
        mCurrentlyDisplayedDevice = id;
        String mostRecentLocationForDevice = dataAccess.getMostRecentLocationForDeviceAddr(id);
        if (mostRecentLocationForDevice == null) {
            Toast.makeText(getActivity(), "No locations recorded for this device", Toast.LENGTH_LONG).show();
            return;
        }
        int dev_id = dataAccess.getDeviceID(id);
        BluetoothDeviceInfo device_info = dataAccess.getBluetoothDeviceInfo(dev_id);
        DataAccessModule.LocationInfoTuple[] last_five_locations = dataAccess.getLastNLocations(dev_id, 1);
        for (DataAccessModule.LocationInfoTuple info : last_five_locations) {
            if (info == null) {
                break;
            }
            Location loc = deserializeJSONToLocation(info.loc);
            Long l = Long.valueOf(info.time);
            LatLng mostLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            MarkerOptions options = new MarkerOptions();
            String date_format = "hh:mm a - MM-dd-yyyy";
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(date_format);
            Date last_located_date = new Date(l);
            String date = DATE_FORMAT.format(last_located_date);
            options.snippet(date);
            options.title(device_info.getName());
            options.position(mostLatLng);
            getMap().addMarker(options);
            zoomCameraToIncludeLocations(getMap(), loc, getMap().getMyLocation());
/*
            getView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (top != 0 || bottom != 0) {
                        mMapLayoutDone = true;
                    }
                }
            });
            */
        }
    }


    private void zoomCameraToIncludeLocations(GoogleMap map, Location p1, Location p2) {
        if (p1 == null || p2 == null) {
            return;
        }
        LatLng loc1 = new LatLng(p1.getLatitude(), p1.getLongitude());
        LatLng loc2 = new LatLng(p2.getLatitude(), p2.getLongitude());
        LatLngBounds.Builder bounds = LatLngBounds.builder();
        bounds.include(loc1);
        bounds.include(loc2);
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), PADDING));
        /*
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), PADDING), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                getMap().animateCamera(CameraUpdateFactory.zoomBy(-5));
            }

            @Override
            public void onCancel() {
            }
        });
*/
    }

}
