package com.digitalobstaclecourse.bluefinder;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
public class FindCarMapFragment extends SupportMapFragment {
    private static final int DEFAULT_ZOOM = 15;
    private static String TAG = "FindCarMapFragment";
    private String mCurrentlyDisplayedDevice = null;
    private static Location deserializeJSONToLocation(String ljson) {
        Gson gson = new Gson();
        Log.d(TAG, "" + ljson);
        return gson.fromJson(ljson, Location.class);
    }

    public FindCarMapFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
        View v = super.onCreateView(arg0, arg1, arg2);
        initMap();
        return v;
    }

    private void initMap() {
        GoogleMap gm = this.getMap();
        if (gm == null) {
            Log.e(TAG, "initMap Error");
            return;
        }
        gm.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (loc != null) {
            gm.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), DEFAULT_ZOOM));
        }
        Log.d("TAG", "initMap()");
    }

    public void displayLocationsForDevice(String id) {
        if (id.equals(mCurrentlyDisplayedDevice)) {
            return;
        }
        if (mCurrentlyDisplayedDevice != null) {
            getMap().clear();
        }
        Log.d(TAG, "displayLocationsForDevice:" + id);
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
        }
    }


}
