/*******************************************************************************
 * Copyright (c) 2013, Christopher Berger
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by  DigitalObstacleCourse.com.
 * 4. Neither the name of DigitalObstacleCourse.com nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY Christopher Berger ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Christopher Berger BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.digitalobstaclecourse.bluefinder;

import java.util.ArrayList;
import java.util.Set;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcel;
import android.preference.PreferenceFragment;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class FindCar extends Activity implements
		NotifyNoBluetoothDialog.NoticeDialogListener {
	private static final String PREFS_NAME = "LOCATION_KEY_VALUE";
	private BluetoothAdapter mBluetoothAdapter;
	private ListView mListView;
	// private ArrayAdapter<String> mListAdapter;
	private BluetoothDeviceAdapter mListAdapter;
	private ArrayList<BluetoothDeviceInfo> device_info_list;
	private LocationManager locationManager;
	private LocationListener locationListener;
	void insert_paired_devices_into_database() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();		
		if (mBluetoothAdapter == null) {
			showNoBluetoothDialog();
		}
		
		Set<BluetoothDevice> paired_devices = mBluetoothAdapter
				.getBondedDevices();		
		DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(this);
		for (BluetoothDevice d: paired_devices){
			Log.d("TAG", "msg" + d.getName());
			dataAccess.add_device(d);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_car);
		Log.d("onCreate", "play services available? " +
                (ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext())));
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(this);
		BluetoothDeviceInfo[] devices = dataAccess.getAllDevices();
		/*
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();		
		if (mBluetoothAdapter == null) {
			showNoBluetoothDialog();
		}
		loc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Set<BluetoothDevice> paired_devices = mBluetoothAdapter
				.getBondedDevices();
				*/
		final SharedPreferences locationKeyValue = getSharedPreferences(
				PREFS_NAME, 0);
		ArrayList<String> device_name_list = new ArrayList<String>();
		device_info_list = new ArrayList<BluetoothDeviceInfo>();
		insert_paired_devices_into_database();
		locationListener = new LocationListener() {

			public void onLocationChanged(Location arg0) {	
			}

			public void onProviderDisabled(String arg0) {
			}

			public void onProviderEnabled(String arg0) {
			}

			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			}
	
		};

		
		for (BluetoothDeviceInfo device : dataAccess.getAllDevices()) {
			Log.d("PAIRDEVICE", "Device name:" + device.getName());
			device_name_list.add(device.getName());
			device_info_list.add(new BluetoothDeviceInfo(device.getName(),
					device.getAddress()));
		}
		mListView = (ListView) findViewById(R.id.device_list);
		mListAdapter = new BluetoothDeviceAdapter(this, device_info_list);
		mListView.setAdapter(mListAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {

		
			public void onItemClick(AdapterView<?> arg0, View view, int pos,
					long id) {
				BluetoothDeviceInfo d = device_info_list.get(pos);
				String device_key = d.getAddress();
				int count = locationKeyValue.getInt("count:>" + device_key, 0);
				SharedPreferences.Editor editor = locationKeyValue.edit();
				editor.putInt("count:>" + device_key, ++count);
				editor.commit();
				Location last_location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				DataAccessModule  data_module = DataAccessModule.getDataAccessModule(getApplication().getApplicationContext());
				int device_id = data_module.getDeviceID(d.getName(), d.getAddress());
				String most_recent_loc = data_module.getMostRecentLocationForDevice("" + device_id);
				
				if (most_recent_loc != null){
					makeUseOfNewLocation(most_recent_loc, device_id);
				}
				else {
					Toast.makeText(getApplicationContext(), "There are no recorded locations for this device", Toast.LENGTH_LONG).show();
				}
				/*
				Toast.makeText(getApplicationContext(),
						"Clicked: " + device_key + ":" + count,
						Toast.LENGTH_SHORT).show();
				 */
			}
		});

	}

	protected void makeUseOfNewLocation(String location, int id) {
		Log.d("LOCATION", "location = " + location.toString());
		Intent intent = new Intent();
		//Parcel p = new Parcel();
		//intent.putExtra(location.writeToParcel(parcel, flags), value)
		intent.putExtra("DEVICE_ID", id);
		intent.putExtra("LAST_GPS_LOCATION", location);
	
		//intent.putExtra("LAST_GPS_LOCATION", location.writeToParcel(parcel, 0));
		intent.setClass(this, FindCarLocatorActivity.class);
		startActivity(intent);

	}

	public static class PrefsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			getActivity();

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_find_car, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("MENU", (String) item.getTitle());
		Intent intent = new Intent();
		intent.setClass(FindCar.this, SettingsActivity.class);
		startActivityForResult(intent, 0);
		Log.d("MENU", "MENU finished");
		return true;
	}

	public void showNoBluetoothDialog() {
		NotifyNoBluetoothDialog dialog = new NotifyNoBluetoothDialog();
		dialog.setCancelable(false);
		dialog.show(getFragmentManager(), "NoBluetoothDialogFragment");
	}

	public void onDialogConfirmClick(NotifyNoBluetoothDialog dialog) {
		Log.d("TAG", "Dialog onClick");
		finish();
	}

}