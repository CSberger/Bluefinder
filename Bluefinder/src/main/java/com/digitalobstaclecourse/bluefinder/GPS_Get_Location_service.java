package com.digitalobstaclecourse.bluefinder;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GPS_Get_Location_service extends IntentService {
    private static final String TAG = "GPS_Get_Location";

    public GPS_Get_Location_service() {
        super("GPS_Get_Location_Service");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.i(TAG, "Service Running");
        String action = intent.getAction();
        Log.i(TAG, "ACTION = " + action);
        assert action != null;
        if (action.equals(Globals.ACTION_LOCATION_CHANGED)) {
            Bundle extras = intent.getExtras();
            assert extras != null;
            String device_name = extras.getString("name");
            String device_address = extras.getString("address");
            Location location = (Location) extras.get(LocationManager.KEY_LOCATION_CHANGED);
            if (location != null) {
                String logMessage = LogHelper.formatLocationInfo(location);
                Log.i(TAG, "LOCATION CHANGED *** => " + logMessage);
                DataAccessModule data_access = DataAccessModule.getDataAccessModule(this.getApplicationContext());
                data_access.setLocation(device_name, device_address, location, location.getTime());
            }
        }
    }


}
