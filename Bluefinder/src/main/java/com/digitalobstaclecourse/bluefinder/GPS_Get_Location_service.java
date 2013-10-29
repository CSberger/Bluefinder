package com.digitalobstaclecourse.bluefinder;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.app.IntentService;
import android.util.Log;

/**
 * Created by Chris on 10/29/13.
 */
public class GPS_Get_Location_service extends IntentService {
    private static String TAG = "GPS_Get_Location";

    public GPS_Get_Location_service() {
        super("GPS_Get_Location_Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service Running");
        String action = intent.getAction();
        if (action.equals(Globals.ACTION_LOCATION_CHANGED)) {
            Bundle extras = intent.getExtras();
            String device_name = extras.getString("name");
            String device_address = extras.getString("address");

            Location location = (Location) extras.get(LocationManager.KEY_LOCATION_CHANGED);
            if (location != null) {
                String logMessage = LogHelper.formatLocationInfo(location);
                Log.d(TAG, "LOCATION CHANGED *** => " + logMessage);
                DataAccessModule data_access = DataAccessModule.getDataAccessModule(this.getApplicationContext());
                data_access.setLocation(device_name, device_address, location, location.getTime());

            }
        }
    }



}
