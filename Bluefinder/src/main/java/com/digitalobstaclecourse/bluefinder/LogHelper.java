package com.digitalobstaclecourse.bluefinder;

import android.location.Location;

/**
 * Created by Chris on 10/29/13.
 */
public class LogHelper {
    public static String formatLocationInfo(Location location) {
        return "(LAT,LNG) ="+ "(" + location.getLatitude() + "," +  location.getLongitude() + ")";

    }
}
