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

import java.util.List;

import com.digitalobstaclecourse.bluefinder.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@SuppressWarnings("rawtypes")
public class BluetoothDeviceAdapter extends ArrayAdapter {
	private final Activity activity;
	private final List bluetooth_devices;
	
	@SuppressWarnings("unchecked")
	public BluetoothDeviceAdapter(Activity activity, List objects) {
			
		super(activity, R.layout.device_item, objects);
		this.activity = activity;
		this.bluetooth_devices = objects;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		BluetoothDeviceView blView = null;
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.device_item, null);
			blView = new BluetoothDeviceView();
			blView.name= (TextView) rowView.findViewById(R.id.label1);
			blView.addr= (TextView) rowView.findViewById(R.id.label2);
			rowView.setTag(blView);
		}
		else {
			blView = (BluetoothDeviceView) rowView.getTag();
		}
		BluetoothDeviceInfo currentDevice = (BluetoothDeviceInfo) bluetooth_devices.get(position);
		blView.name.setText(currentDevice.getName());
		blView.addr.setText(currentDevice.getAddress());
		return rowView;
		
	}

	public static class BluetoothDeviceView {
		protected TextView name;
		protected TextView addr;
	}
}
