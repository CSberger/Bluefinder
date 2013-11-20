package com.digitalobstaclecourse.bluefinder;

import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;


public class DataAccessModule {
    //private static final String CRASH_STRING = "{"mResults":[0.0,0.0],"mProvider":"gps","mExtras":{"mParcelledData":{"mOwnsNativeParcelObject":true,"mNativePtr":1923002448},"mHasFds":false,"mFdsKnown":true,"mAllowFds":true},"mDistance":0.0,"mElapsedRealtimeNanos":350104309684898,"mTime":1362695509000,"mAltitude":-40.5,"mLongitude":-121.93942104,"mLon2":0.0,"mLon1":0.0,"mLatitude":38.47395716,"mLat1":0.0,"mLat2":0.0,"mInitialBearing":0.0,"mHasSpeed":true,"mHasBearing":false,"mHasAltitude":true,"mHasAccuracy":true,"mAccuracy":15.0,"mSpeed":0.0,"mBearing":0.0}";
    private static final String TAG = "DataAccessModule";

    public class SQLModelOpener extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "LocationDatabase";
        public static final int DATABASE_VERSION = 1;
        public static final String DEVICE_TABLE_NAME = "DeviceTable";
        public static final String LOCATION_TABLE_NAME = "LocationTable";
        public static final String DEVICE_NAME = "deviceName";
        public static final String DEVICE_ADDR = "deviceAddr";
        public static final String DEVICE_USER_KEY = "deviceUserKey";
        public static final String DEVICE_LOCATION = "deviceLoc";
        public static final String DEVICE_DATE = "deviceDate";
        public static final String LOCATION_DEVICE_KEY = "deviceTableKey";
        public static final String LOCATION_DATE = "locationDate";
        public static final String LOCATION_COORDS = "locationCoords";
        public static final String DEVICE_TABLE_CREATE =
                "CREATE TABLE " + DEVICE_TABLE_NAME + " (" +
                        "_id INTEGER PRIMARY KEY" + ", " +
                        DEVICE_NAME + " TEXT" + "," +
                        DEVICE_ADDR + " TEXT" + "," +
                        "UNIQUE (" + DEVICE_NAME + "," + DEVICE_ADDR + ") ON CONFLICT REPLACE" +
                        ");";
        public static final String LOCATION_TABLE_CREATE = "CREATE TABLE " + LOCATION_TABLE_NAME + " (" +
                "_id INTEGER PRIMARY KEY" + ", " +
                LOCATION_DEVICE_KEY + " INTEGER, " +
                LOCATION_DATE + " TEXT, " +
                LOCATION_COORDS + " TEXT, " +
                "FOREIGN KEY(" + LOCATION_DEVICE_KEY + ") REFERENCES " + DEVICE_TABLE_NAME + "(_id)" +
                ");";

        public SQLModelOpener(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            Log.d("TAG", DEVICE_TABLE_CREATE);
            db.execSQL(DEVICE_TABLE_CREATE);
            Log.d("TAG", LOCATION_TABLE_CREATE);
            db.execSQL(LOCATION_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub

        }

    }

    public static final String PREFS_NAME = "FindCarPrefs";
    Location loc;
    Date time_stamp;
    Context c;

    private DataAccessModule(Context c) {
        this.c = c;

    }

    private static DataAccessModule accessModule;

    public static synchronized DataAccessModule getDataAccessModule(Context c) {
        if (c == null && accessModule == null) {
            throw new NullPointerException();
        } else if (accessModule == null && c != null) {
            accessModule = new DataAccessModule(c);
        } else if (accessModule != null && c != null) {
            accessModule.c = c;
        } else if (accessModule != null && c == null) {
            throw new NullPointerException();
        }
        return accessModule;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static void storeLocationForKey(String key, DataAccessModule m) {

    }

    public void add_device(BluetoothDevice d) {
        SQLModelOpener opener = new SQLModelOpener(this.c);
        SQLiteDatabase db = opener.getReadableDatabase();
        ContentValues values = new ContentValues();
        String name = d.getName();
        String addr = d.getAddress();
        BluetoothDeviceInfo info = getBluetoothDeviceInfo(name, addr);
        if (info == null) {
            values.put(SQLModelOpener.DEVICE_NAME, d.getName());
            values.put(SQLModelOpener.DEVICE_ADDR, d.getAddress());
            db.insert(SQLModelOpener.DEVICE_TABLE_NAME, null, values);
        }
        db.close();

    }

    private Location[] getLastNLocations(int device_id, int n) {
        Location[] lastNLocations = new Location[n];
        String query = "SELECT * FROM " + DataAccessModule.SQLModelOpener.LOCATION_TABLE_NAME +
                " WHERE ";

        return lastNLocations;
    }

    public BluetoothDeviceInfo getBluetoothDeviceInfo(int device_id) {
        SQLModelOpener opener = new SQLModelOpener(this.c);
        SQLiteDatabase db = opener.getReadableDatabase();


        Cursor cur = db.query(SQLModelOpener.DEVICE_TABLE_NAME, new String[]{"_id", SQLModelOpener.DEVICE_NAME, SQLModelOpener.DEVICE_ADDR},
                "_id = ?", new String[]{"" + device_id}, null, null, null);
        cur.moveToFirst();
        BluetoothDeviceInfo info = null;
        if (!cur.isAfterLast()) {
            String new_name = cur.getString(1);
            String new_addr = cur.getString(2);
            info = new BluetoothDeviceInfo(new_name, new_addr);

        }
        db.close();
        return info;

    }

    public BluetoothDeviceInfo getBluetoothDeviceInfo(String addr) {
        SQLModelOpener opener = new SQLModelOpener(this.c);
        SQLiteDatabase db = opener.getReadableDatabase();


        Cursor cur = db.query(SQLModelOpener.DEVICE_TABLE_NAME, new String[]{"_id", SQLModelOpener.DEVICE_NAME, SQLModelOpener.DEVICE_ADDR},
                SQLModelOpener.DEVICE_ADDR + "= ?", new String[]{addr}, null, null, null);
        cur.moveToFirst();
        BluetoothDeviceInfo info = null;
        if (!cur.isAfterLast()) {
            String new_name = cur.getString(1);
            String new_addr = cur.getString(2);
            info = new BluetoothDeviceInfo(new_name, new_addr);

        }
        db.close();
        return info;

    }

    public BluetoothDeviceInfo getBluetoothDeviceInfo(String name, String addr) {
        SQLModelOpener opener = new SQLModelOpener(this.c);
        SQLiteDatabase db = opener.getReadableDatabase();


        Cursor cur = db.query(SQLModelOpener.DEVICE_TABLE_NAME, new String[]{"_id", SQLModelOpener.DEVICE_NAME, SQLModelOpener.DEVICE_ADDR},
                SQLModelOpener.DEVICE_NAME + "= ? AND " + SQLModelOpener.DEVICE_ADDR + "= ?", new String[]{name, addr}, null, null, null);
        cur.moveToFirst();
        BluetoothDeviceInfo info = null;
        if (!cur.isAfterLast()) {
            String new_name = cur.getString(1);
            String new_addr = cur.getString(2);
            info = new BluetoothDeviceInfo(new_name, new_addr);

        }
        db.close();
        return info;

    }

    public BluetoothDeviceInfo[] getAllDevices() {
        SQLModelOpener opener = new SQLModelOpener(this.c);
        SQLiteDatabase db = opener.getReadableDatabase();
        Cursor cur = db.query(SQLModelOpener.DEVICE_TABLE_NAME, new String[]{SQLModelOpener.DEVICE_NAME, SQLModelOpener.DEVICE_ADDR}, null, null, null, null, null);

        int num_rows = cur.getCount();

        ArrayList<BluetoothDeviceInfo> device_list = new ArrayList<BluetoothDeviceInfo>();
        if (num_rows > 0) {
            cur.moveToFirst();
            while (!cur.isLast()) {
                String device_name = cur.getString(0);
                String device_addr = cur.getString(1);
                device_list.add(new BluetoothDeviceInfo(device_name, device_addr));
                cur.moveToNext();
            }
        }
        db.close();
        return device_list.toArray(new BluetoothDeviceInfo[device_list.size()]);

    }

    public int getDeviceID(String name, String addr) {
        SQLModelOpener opener = new SQLModelOpener(this.c);
        SQLiteDatabase db = opener.getReadableDatabase();
        int _id = -1;
        Cursor cur = db.query(SQLModelOpener.DEVICE_TABLE_NAME, new String[]{"_id", SQLModelOpener.DEVICE_NAME, SQLModelOpener.DEVICE_ADDR},
                SQLModelOpener.DEVICE_NAME + "= ? AND " + SQLModelOpener.DEVICE_ADDR + "= ?", new String[]{name, addr}, null, null, null);

        if (cur.moveToFirst()) {
            _id = cur.getInt(0);
        }
        db.close();
        return _id;

    }

    public int getDeviceID(String addr) {
        SQLModelOpener opener = new SQLModelOpener(this.c);
        SQLiteDatabase db = opener.getReadableDatabase();
        int _id = -1;
        Cursor cur = db.query(SQLModelOpener.DEVICE_TABLE_NAME, new String[]{"_id", SQLModelOpener.DEVICE_NAME, SQLModelOpener.DEVICE_ADDR},
                SQLModelOpener.DEVICE_ADDR + "= ?", new String[]{addr}, null, null, null);

        if (cur.moveToFirst()) {
            _id = cur.getInt(0);
        }
        db.close();
        return _id;

    }

    public DataAccessModule getLocationFromKey(String key) {
        //Context context = Context.create
////		
////		SharedPreferences prefs = SharedPreferences.getSharedPreferences(PREFS_NAME, 0 );
////		Location stored_loc = new Location(stored_loc,Context.getApplicationContext());
////		
//		ModelStoreLocationData m = new ModelStoreLocationData(stored_loc, new Date());

        SharedPreferences prefs;
        DataAccessModule m = accessModule;
        SQLModelOpener opener = m.new SQLModelOpener(c);
        SQLiteDatabase db = opener.getReadableDatabase();
        db.query(
                opener.DEVICE_TABLE_NAME,
                new String[]
                        {SQLModelOpener.DEVICE_ADDR, SQLModelOpener.DEVICE_NAME},
                null, null, null, null, null);
        Location loc = null;
        Date d = null;
        m.loc = loc;
        m.time_stamp = d;

        db.close();
        return m;
    }

    public void setLocation(String name, String address, Location l, long date) {
        int device_id = getDeviceID(name, address);
        dbAddLocationRow(device_id, l, date);
    }

    private void dbAddLocationRow(int device_id, Location l, long date) {
        Log.d(TAG, "dbAddLocationRow");
        Log.d(TAG, "lon, lat = " + l.getLatitude() + "," + l.getLongitude());
        //Log.d(TAG, "Location: " + new Location()
        String db_location = serializeLocationToJSON(l);
        SQLModelOpener opener = new SQLModelOpener(this.c);
        SQLiteDatabase db = opener.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SQLModelOpener.LOCATION_DEVICE_KEY, device_id);
        cv.put(SQLModelOpener.LOCATION_DATE, date);
        cv.put(SQLModelOpener.LOCATION_COORDS, db_location);
        db.insert(SQLModelOpener.LOCATION_TABLE_NAME, null, cv);
        db.close();
        Log.d(TAG, "added row: " + cv.toString());
        //Location ll = new Location(l);

    }

    public String getMostRecentLocationForDeviceAddr(String device_addr) {
        BluetoothDeviceInfo info = getBluetoothDeviceInfo(device_addr);
        int device_id = getDeviceID(info.getName(), info.getAddress());


        SQLModelOpener opener = new SQLModelOpener(this.c);
        SQLiteDatabase db = opener.getReadableDatabase();
        String query = "SELECT locationCoords FROM LocationTable where locationDate = (select max(locationDate) from LocationTable WHERE " + SQLModelOpener.LOCATION_DEVICE_KEY + " = ?)";
        Cursor cur = db.rawQuery(query, new String[]{Integer.toString(device_id)});
        String result = null;
        if (cur.moveToFirst()) {
            result = cur.getString(0);
        }
        db.close();
        if (result == null) {
            return null;
        }
        return result;
    }

    public String getMostRecentTimeOfLocation(String device_id) {
        SQLModelOpener opener = new SQLModelOpener(this.c);
        SQLiteDatabase db = opener.getReadableDatabase();
        String query = "SELECT locationDate FROM LocationTable where locationDate = (select max(locationDate) from LocationTable WHERE deviceTableKey = ?)";
        Cursor cur = db.rawQuery(query, new String[]{device_id});
        String result = null;
        if (cur.moveToFirst()) {
            result = cur.getString(0);
        }
        db.close();
        if (result == null) {
            return null;
        }
        return result;
    }

    private static String serializeLocationToJSON(Location l) {
        Gson gson = new Gson();
        String json = gson.toJson(l);
        return json;
    }

    private static Location deserializeLocationToJSON(String ljson) {
        Gson gson = new Gson();
        Log.d(TAG, "" + ljson);
        Location l = gson.fromJson(ljson, Location.class);
        return l;
    }
}
