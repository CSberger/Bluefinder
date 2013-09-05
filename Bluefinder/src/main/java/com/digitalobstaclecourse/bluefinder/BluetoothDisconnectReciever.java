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

import java.util.Date;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BluetoothDisconnectReciever extends BroadcastReceiver {
    private static String TAG = "BluetoothDisconnectReceiver";
	private static final String EXTRA_DEVICE = BluetoothDevice.EXTRA_DEVICE;
	public BluetoothDisconnectReciever() {
	}

	@Override
	public void onReceive(final Context context, Intent intent) {
		// TODO: This method is called when the BroadcastReceiver is receiving
		// an Intent broadcast.
		Log.d(TAG, "bluetooth connected");
		Bundle extras= intent.getExtras();
		Log.d(TAG, extras.toString());
		Log.d(TAG, "extras keys: " + extras.keySet());
		
		//Parcel p = 
		
		final BluetoothDevice device = extras.getParcelable(EXTRA_DEVICE);//BluetoothDevice.CREATOR.createFromParcel(p);
		String device_address = device.getAddress();

		Log.d(TAG, "DISCONNECTING FROM " + device_address);
		DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(context);
		LocationListener locationListener = new LocationListener() {
			
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}

			public void onLocationChanged(Location location) {
				makeUseOfNewLocation(location, device, context);
			}
		};
		LocationManager last_location = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		last_location.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
		Location loc = last_location.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		makeUseOfNewLocation(loc, device, context);
		
		
		
		Toast.makeText(context, "Woo connected To " + device.getName() + "@" +device_address, Toast.LENGTH_SHORT).show();
		//throw new UnsupportedOperationException("Not yet implemented");
	}

	protected void makeUseOfNewLocation(Location location, BluetoothDevice device, Context c) {
		// TODO Auto-generated method stub
		DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(c);
		dataAccess.setLocation(device.getName(), device.getAddress(), location, new Date().getTime());
	}
}
