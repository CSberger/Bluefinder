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
    private static final  String PURCHASE_TYPE_CONSUMABLE = "consumable";
    private static final  String PURCHASE_TYPE_INFINITE = "infinite";
    public static final int USES_PER_PURCHASE = 50;

    public int get_remaining_locations() {

        int trial_locations = Integer.parseInt(this.mContext.getString(R.integer.default_trial_location_count));
        int purchased_uses = get_number_of_purchased_uses();
        int times_used = getNumberOfTimesUsed();


        return trial_locations + purchased_uses - times_used;
    }

    public void increment_number_of_locations() {
        add_use();

    }

    public boolean locationsExistForId(String id) {
        return getMostRecentLocationForDeviceAddr(id) != null;

    }

    public void registerConsumablePurchase() {
        Log.i(TAG, "registerConsumablePurchase()");
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getWritableDatabase();
        assert db != null;
        ContentValues values = new ContentValues();
        Log.d(TAG, "registering transaction with database");
        values.put(SQLModelOpener.PURCHASE_DATE, new Date().getTime());
        values.put(SQLModelOpener.PURCHASE_TYPE, PURCHASE_TYPE_CONSUMABLE);
        long status_code = db.insert(SQLModelOpener.PURCHASES_TABLE_NAME, null, values);
        db.close();
        Log.d(TAG, "add_use STATUS:" + status_code);
    }

    public void registerInfinitePurchase() {
        Log.i(TAG, "registerInfinitePurchase()");
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getWritableDatabase();
        assert db != null;
        ContentValues values = new ContentValues();
        Log.d(TAG, "registering transaction with database");
        values.put(SQLModelOpener.PURCHASE_DATE, new Date().getTime());
        values.put(SQLModelOpener.PURCHASE_TYPE, PURCHASE_TYPE_INFINITE);
        long status_code = db.insert(SQLModelOpener.PURCHASES_TABLE_NAME, null, values);
        db.close();
        Log.d(TAG, "add_use STATUS:" + status_code);
    }
    public boolean hasPurchasedInfiniteUse() {
        Log.d(TAG, "hasPurchasedInfiniteUse");
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();
        int num_rows = -1;

        assert db != null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + SQLModelOpener.PURCHASES_TABLE_NAME +
                " WHERE "+ SQLModelOpener.PURCHASE_TYPE + " = '" + PURCHASE_TYPE_INFINITE + "'",
                null);
        //TODO:filter out only consumable purchases

        num_rows = cursor.getCount();
        db.close();
        return num_rows > 0;


    }

    public int get_number_of_purchased_uses() {
        Log.d(TAG, "getNumberOfLocations");
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();
        int num_rows = -1;

        assert db != null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + SQLModelOpener.PURCHASES_TABLE_NAME +
                " WHERE "+ SQLModelOpener.PURCHASE_TYPE + " = '" + PURCHASE_TYPE_CONSUMABLE + "'",
                null);
        //TODO:filter out only consumable purchases

        num_rows = cursor.getCount();
        db.close();



        return num_rows * USES_PER_PURCHASE;

    }

    public class LocationInfoTuple {
        public final String time;
        public final String loc;

        public LocationInfoTuple(String time, String loc) {
            this.time = time;
            this.loc = loc;
        }
    }

    public class SQLModelOpener extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "LocationDatabase";
        public static final int DATABASE_VERSION = 1;
        public static final String DEVICE_TABLE_NAME = "DeviceTable";
        public static final String EVENT_TABLE_NAME = "EventTable";
        public static final String LOCATION_TABLE_NAME = "LocationTable";
        public static final String USES_TABLE_NAME = "UsesTable";
        public static final String PURCHASES_TABLE_NAME = "PurchasesTable";

        public static final String EVENT_NAME = "eventName";
        public static final String EVENT_ADDR = "eventAddr";
        public static final String DEVICE_NAME = "deviceName";
        public static final String DEVICE_ADDR = "deviceAddr";
        public static final String DEVICE_USER_KEY = "deviceUserKey";
        public static final String DEVICE_LOCATION = "deviceLoc";
        public static final String DEVICE_DATE = "deviceDate";
        public static final String LOCATION_DEVICE_KEY = "deviceTableKey";
        public static final String LOCATION_DATE = "locationDate";
        public static final String LOCATION_COORDS = "locationCoords";
        public static final String USES_TABLE_DATE = "date_of_location";
        public static final String PURCHASE_TYPE = "type";
        public static final String PURCHASE_DATE = "date";

        public static final String DEVICE_TABLE_CREATE =
                "CREATE TABLE " + DEVICE_TABLE_NAME + " (" +
                        "_id INTEGER PRIMARY KEY" + ", " +
                        DEVICE_NAME + " TEXT" + "," +
                        DEVICE_ADDR + " TEXT" + "," +
                        "UNIQUE (" + DEVICE_NAME + "," + DEVICE_ADDR + ") ON CONFLICT REPLACE" +
                        ");";
        public static final String EVENT_TABLE_CREATE =
                "CREATE TABLE " + EVENT_TABLE_NAME + " (" +
                        "_id INTEGER PRIMARY KEY" + ", " +
                        EVENT_NAME + " TEXT" + "," +
                        EVENT_ADDR + " TEXT" + "," +
                        "UNIQUE (" + EVENT_NAME + "," + EVENT_ADDR + ") ON CONFLICT REPLACE" +
                        ");";
        public static final String LOCATION_TABLE_CREATE = "CREATE TABLE " + LOCATION_TABLE_NAME + " (" +
                "_id INTEGER PRIMARY KEY" + ", " +
                LOCATION_DEVICE_KEY + " INTEGER, " +
                LOCATION_DATE + " TEXT, " +
                LOCATION_COORDS + " TEXT, " +
                "FOREIGN KEY(" + LOCATION_DEVICE_KEY + ") REFERENCES " + DEVICE_TABLE_NAME + "(_id)" +
                ");";

        private final String USES_TABLE_CREATE = String.format("CREATE TABLE %s " +
                "(_id INTEGER PRIMARY KEY, %s TEXT)",
                USES_TABLE_NAME, USES_TABLE_DATE);

        private final String PURCHASES_TABLE_CREATE = String.format("CREATE TABLE %s " +
                "(_id INTEGER PRIMARY KEY, %s TEXT, %s TEXT)",
                PURCHASES_TABLE_NAME, PURCHASE_DATE, PURCHASE_TYPE);

        public SQLModelOpener(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            Log.d(TAG, DEVICE_TABLE_CREATE);
            db.execSQL(DEVICE_TABLE_CREATE);
            Log.d(TAG, "Created Device Table");
            Log.d(TAG, EVENT_TABLE_CREATE);
            db.execSQL(EVENT_TABLE_CREATE);
            Log.d(TAG, "Created Event Table");
            Log.d(TAG, LOCATION_TABLE_CREATE);
            db.execSQL(LOCATION_TABLE_CREATE);
            Log.d(TAG, "Created Location Table");
            Log.d(TAG, USES_TABLE_CREATE);
            db.execSQL(USES_TABLE_CREATE);
            Log.d(TAG, "Created Uses Table");
            Log.d(TAG, PURCHASES_TABLE_CREATE);
            db.execSQL(PURCHASES_TABLE_CREATE);
            Log.d(TAG, "Created Purchases Table");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub

        }

    }

    public static final String PREFS_NAME = "FindCarPrefs";
    Location loc;
    Date time_stamp;
    Context mContext;

    private DataAccessModule(Context c) {
        this.mContext = c;
    }

    private static DataAccessModule accessModule;

    public static synchronized DataAccessModule getDataAccessModule(Context c) {
        assert c != null;
        if (accessModule == null) {

            accessModule = new DataAccessModule(c);
        }
        else {
            accessModule.mContext = c;
        }

        return accessModule;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static void storeLocationForKey(String key, DataAccessModule m) {

    }

    public void add_use() {
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getWritableDatabase();
        assert db != null;
        ContentValues values = new ContentValues();

        Log.d(TAG, "registering transaction with database");
        values.put(SQLModelOpener.USES_TABLE_DATE, new Date().getTime());
        long status_code = db.insert(SQLModelOpener.USES_TABLE_NAME, null, values);
        db.close();
        Log.d(TAG, "add_use STATUS:" + status_code);


    }
    public void add_device(BluetoothDevice d) {
        Log.d(TAG, "Adding:" + d.getName() + "," + d.getAddress());
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getWritableDatabase();
        assert db != null;
        ContentValues values = new ContentValues();
        String name = d.getName();
        String addr = d.getAddress();
        BluetoothDeviceInfo info = getBluetoothDeviceInfo(name, addr);
        if (info == null) {
            Log.d(TAG, "PUTTING:" + d.getName() + "," + d.getAddress());
            values.put(SQLModelOpener.DEVICE_NAME, d.getName());
            values.put(SQLModelOpener.DEVICE_ADDR, d.getAddress());

            long status_code = db.insert(SQLModelOpener.DEVICE_TABLE_NAME, null, values);
            Log.d(TAG, "STATUS:" + status_code);
        }

        db.close();
    }

    public void add_event(String eventName, String eventID) {
        Log.d(TAG, "Adding:" + eventName + "," + eventID);
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getWritableDatabase();
        assert db != null;
        ContentValues values = new ContentValues();
        String name = eventName;
        String addr = eventID;
        BluetoothDeviceInfo info = getBluetoothDeviceInfo(name, addr);
        if (info == null) {
            Log.d(TAG, "PUTTING:" + eventName + "," + eventID);
            values.put(SQLModelOpener.EVENT_NAME, name);
            values.put(SQLModelOpener.EVENT_ADDR, addr);

            long status_code = db.insert(SQLModelOpener.EVENT_TABLE_NAME, null, values);
            Log.d(TAG, "STATUS:" + status_code);
        }

        db.close();
    }

    @SuppressWarnings("SameParameterValue")
    public LocationInfoTuple[] getLastNLocations(int device_id, int n) {
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();
        LocationInfoTuple[] lastNLocations;
        lastNLocations = new LocationInfoTuple[n];
        String query = "SELECT " + SQLModelOpener.LOCATION_DATE + "," + SQLModelOpener.LOCATION_COORDS + " FROM "
                + DataAccessModule.SQLModelOpener.LOCATION_TABLE_NAME + " WHERE "
                + SQLModelOpener.LOCATION_DEVICE_KEY + " = ? ORDER BY "
                + SQLModelOpener.LOCATION_DATE + " LIMIT ?";
        assert db != null;
        Cursor cur = db.rawQuery(query, new String[]{Integer.toString(device_id), Integer.toString(n)});
        String result = null;
        boolean stillValid = true;
        if (cur.moveToFirst()) {
            for (int i = 0; i < n && stillValid; i++) {
                lastNLocations[i] = new LocationInfoTuple(cur.getString(0), cur.getString(1));
                stillValid = cur.moveToNext();
            }
        }
        db.close();
        return lastNLocations;
    }

    public BluetoothDeviceInfo getBluetoothDeviceInfo(int device_id) {
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();


        assert db != null;
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
    private int getNumberOfTimesUsed() {
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();
        assert db != null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + SQLModelOpener.USES_TABLE_NAME,
                null);
        int number_of_uses = cursor.getCount();
        db.close();
        return number_of_uses;
    }
    public boolean verify_db_existence (String table_name) {
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();
        assert db != null;
        Cursor cur =
                db.rawQuery(
                        "SELECT name FROM sqlite_master WHERE type = 'table' AND name LIKE ?",
                        new String[]{table_name}
                );
        Log.d(TAG, "Count for table_name: '" + table_name + "' = " + cur.getCount());
        db.close();
        return true;
    }

    public BluetoothDeviceInfo getItemInfo(String addr, String type) {
        if (type == "BluetoothDevice") {
            return getBluetoothDeviceInfo(addr);
        } else {
            return getOtherEventInfo(addr);
        }
    }

    private BluetoothDeviceInfo getOtherEventInfo(String addr) {
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();


        assert db != null;
        Cursor cur = db.query(SQLModelOpener.EVENT_TABLE_NAME, new String[]{"_id", SQLModelOpener.EVENT_NAME, SQLModelOpener.EVENT_ADDR},
                SQLModelOpener.EVENT_ADDR + "= ?", new String[]{addr}, null, null, null);
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
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();


        assert db != null;
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
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();


        assert db != null;
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
    public int getNumberOfLocations() {
        Log.d(TAG, "getNumberOfLocations");
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();
        int num_rows = -1;

        assert db != null;
        Cursor cur = db.query(SQLModelOpener.USES_TABLE_NAME, new String[]{SQLModelOpener.USES_TABLE_DATE}, null, null, null, null, null);
        num_rows = cur.getCount();



        return num_rows;
    }
    public BluetoothDeviceInfo[] getAllDevices() {
        Log.d(TAG, "getAllDevices");
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();
        Cursor cur = db.query(SQLModelOpener.DEVICE_TABLE_NAME, new String[]{SQLModelOpener.DEVICE_NAME, SQLModelOpener.DEVICE_ADDR}, null, null, null, null, null);

        int num_rows = cur.getCount();
        Log.d(TAG, "getAllDevices count = " + num_rows);
        ArrayList<BluetoothDeviceInfo> device_list = new ArrayList<BluetoothDeviceInfo>();
        if (num_rows > 0) {
            cur.moveToFirst();

            while (!cur.isAfterLast()) {

                String device_name = cur.getString(0);
                String device_addr = cur.getString(1);
                device_list.add(new BluetoothDeviceInfo(device_name, device_addr));
                Log.d(TAG, "cursor on device "+ device_name);
                cur.moveToNext();
            }
        }
        db.close();
        return device_list.toArray(new BluetoothDeviceInfo[device_list.size()]);

    }

    public int getDeviceID(String name, String addr) {
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();
        int _id = -1;
        Cursor cur = db.query(SQLModelOpener.DEVICE_TABLE_NAME, new String[]{"_id", SQLModelOpener.DEVICE_NAME, SQLModelOpener.DEVICE_ADDR},
                SQLModelOpener.DEVICE_NAME + "= ? AND " + SQLModelOpener.DEVICE_ADDR + "= ?", new String[]{name, addr}, null, null, null);

        if (cur.moveToFirst()) {
            _id = cur.getInt(0);
        }
        db.close();
        if (_id == -1) {
            opener = new SQLModelOpener(this.mContext);
            db = opener.getReadableDatabase();

            cur = db.query(SQLModelOpener.EVENT_TABLE_NAME, new String[]{"_id", SQLModelOpener.EVENT_NAME, SQLModelOpener.EVENT_ADDR},
                    SQLModelOpener.EVENT_NAME + "= ? AND " + SQLModelOpener.EVENT_ADDR + "= ?", new String[]{name, addr}, null, null, null);

            if (cur.moveToFirst()) {
                _id = cur.getInt(0);
            }
            db.close();
        }
        return _id;

    }

    public int getDeviceID(String addr) {
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
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

        SQLModelOpener opener = accessModule.new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();
        db.query(
                opener.DEVICE_TABLE_NAME,
                new String[]
                        {SQLModelOpener.DEVICE_ADDR, SQLModelOpener.DEVICE_NAME},
                null, null, null, null, null);
        Location loc = null;
        Date d = null;
        accessModule.loc = loc;
        accessModule.time_stamp = d;

        db.close();
        return accessModule;
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
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
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
        BluetoothDeviceInfo info = getItemInfo(device_addr, "BluetoothDevice");
        if (info == null) {
            info = getItemInfo(device_addr, "otherDevice");
        }
        int device_id = getDeviceID(info.getName(), info.getAddress());


        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();
        String query = "SELECT locationCoords FROM LocationTable where locationDate = (select max(locationDate) from LocationTable WHERE " + SQLModelOpener.LOCATION_DEVICE_KEY + " = ?)";
        Cursor cur = db.rawQuery(query, new String[]{Integer.toString(device_id)});
        String result = null;
        if (cur.moveToFirst()) {
            result = cur.getString(0);
        }
        db.close();

        return result;
    }

    public String getNLocationsForDevice(String device_id) {
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
        SQLiteDatabase db = opener.getReadableDatabase();
        String query = "SELECT locationDate FROM LocationTable where locationDate = (select locationDate) from LocationTable WHERE deviceTableKey = ?)";
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

    public String getMostRecentTimeOfLocation(String device_id) {
        SQLModelOpener opener = new SQLModelOpener(this.mContext);
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
        return gson.toJson(l);
    }

    private static Location deserializeLocationToJSON(String ljson) {
        Gson gson = new Gson();
        Log.d(TAG, "" + ljson);
        return gson.fromJson(ljson, Location.class);
    }
}
