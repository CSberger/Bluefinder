package com.digitalobstaclecourse.bluefinder;

//import com.google.android.maps.MapActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.internal.f;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MarkerOptionsCreator;
import com.google.gson.Gson;

import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

public class FindCarLocatorActivity extends FragmentActivity {
	private static String TAG = "FindCarLocator";
	private static final int PADDING = 15;
	private Location mLocation;
	private int mDevice_id;
	private BluetoothDeviceInfo mInfo;
	private SupportMapFragment mMapFragment;
	private static Location deserializeJSONToLocation(String ljson) {
		Gson gson = new Gson();
		Log.d(TAG, "" + ljson);
		Location l = gson.fromJson(ljson, Location.class);
		return l;
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
				mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
			}
		}
		else {
			mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("mapFragTag");
		}
		//Location initial_location;
		String serialized_location =getIntent().getExtras().getString("LAST_GPS_LOCATION"); 
		if (serialized_location != null) {
			mLocation = deserializeJSONToLocation(serialized_location);
		}
		mDevice_id = getIntent().getExtras().getInt("DEVICE_ID");
		mInfo = data_module.getBluetoothDeviceInfo(mDevice_id);
		
		data_module.getMostRecentLocationForDevice("" + mDevice_id);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
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


		GoogleMap gm = mMapFragment.getMap();
		gm.setMyLocationEnabled(true);
		LatLng current_position = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
		gm.moveCamera(CameraUpdateFactory.newLatLngZoom(current_position, 13));
		MarkerOptions options = new MarkerOptions();
		options.snippet("" + mDevice_id);
		options.title(mInfo.getName());
		options.position(current_position);
		gm.addMarker(options);

		
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
