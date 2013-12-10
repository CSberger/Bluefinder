package com.digitalobstaclecourse.bluefinder;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FindCarLocatorActivity extends FragmentActivity {
	private static String TAG = "FindCarLocator";
	private static final int PADDING = 15;
    private static String date_format = "hh:mm a - MM-dd-yyyy";
	private Location mLocation;
	private int mDevice_id;
    private FindCarMapFragment mMapFragment;
    private String m_last_locator_time;
    final String _cameraPositionKey = "cameraPosition";
    private String mDevice_addr;

    private static Location deserializeJSONToLocation(String ljson) {
        Gson gson = new Gson();
		Log.d(TAG, "" + ljson);
        return gson.fromJson(ljson, Location.class);

    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.car_map);
        Log.d("FindCarLocatorActivity", "onCreate");
        Log.d("onCreate", "play services available? " +
                (ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext())));

		DataAccessModule  data_module = DataAccessModule.getDataAccessModule(getApplication().getApplicationContext());
		if (findViewById(R.id.map) != null) {
			if(mMapFragment == null) {
				mMapFragment = (FindCarMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
			}
		}

		mDevice_addr = getIntent().getExtras().getString("DEVICE_ID");

        BluetoothDeviceInfo mInfo = data_module.getBluetoothDeviceInfo(mDevice_id);



	}

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        CameraPosition _last_position = inState.getParcelable(_cameraPositionKey);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        CameraPosition p = mMapFragment.getMap().getCameraPosition();
        outState.putParcelable(_cameraPositionKey, p);
        Log.d(TAG, "SaveInstanceState");
    }
	
	private void zoomCameraToIncludePoints(GoogleMap map, LatLng l1, LatLng l2) {
		LatLngBounds.Builder bounds = LatLngBounds.builder();
		bounds.include(l1);
		bounds.include(l2);
		
		map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), PADDING));
		
	}
	@Override
	protected void onResume(){
		super.onResume();
		/*
		if (mMapFragment == null) {
			mMapFragment = FindCarMapFragment.newInstance();
		}
		?*/
        ((FindCarMapFragment)mMapFragment)
                .displayLocationsForDevice(mDevice_addr);




    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_find_car_locator, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}




}
