package com.digitalobstaclecourse.bluefinder;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Chris on 1/18/14.
 */
public class PowerDisconnectReciever extends BroadcastReceiver {
    static final String TAG = "PowerDisconnectReciever";

    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //SharedPreferences prefs = context.getSharedPreferences();//context.getSharedPreferences("com.digitalobstaclecourse.bluefinder", Context.MODE_PRIVATE);
        boolean showCheckbox = prefs.getBoolean(context.getString(R.string.pref_toast_notification_key), true);
        if (showCheckbox) {
            Toast.makeText(context, "Disconnected From " + "POWER" + "@" + "POWER", Toast.LENGTH_LONG).show();
        }
        DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(context);

        LocationManager last_location = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        Intent i = new Intent(Globals.ACTION_LOCATION_CHANGED);

        i.putExtra("name", "Power Disconnect Location");
        i.putExtra("address", "POWER");
        Log.i(TAG, "packaging up intent");
        PendingIntent _locationChangeServicePendingIntent = PendingIntent.getService(context, 0, i, 0);
        Criteria valid_location = new Criteria();
        valid_location.setAccuracy(Criteria.ACCURACY_FINE);
        try {
            last_location.requestSingleUpdate(valid_location, _locationChangeServicePendingIntent);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
