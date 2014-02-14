package com.digitalobstaclecourse.bluefinder;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

@SuppressWarnings("rawtypes")
public class BluetoothDeviceAdapter extends ArrayAdapter {
    private static final String TAG = "BluetoothDeviceAdapter";
    private final Activity activity;
    private List bluetooth_devices;
    private List other_devices;

    @SuppressWarnings("unchecked")
    public BluetoothDeviceAdapter(Activity activity, List bluetooth_devices, List other_events) {
        super(activity, R.layout.device_item, bluetooth_devices);
        this.activity = activity;
        this.bluetooth_devices = bluetooth_devices;
        this.other_devices = other_events;
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return !isHeader(position);
    }

    public boolean isHeader(int pos) {
        if (pos == 0) {
            return true;
        } else if (pos == bluetooth_devices.size() + 1) {
            return true;
        }
        return false;
    }

    @Override
    public int getCount() {
        return this.bluetooth_devices.size() + this.other_devices.size() + 2;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int pos) {

        if (isHeader(pos)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
        final int type = getItemViewType(position);
        BluetoothDeviceView blView;
        if (rowView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            if (isHeader(position)) {
                rowView = inflater.inflate(R.layout.item_header, null);
            } else {
                rowView = inflater.inflate(R.layout.device_item, null);
            }
            blView = new BluetoothDeviceView();
			blView.name= (TextView) rowView.findViewById(R.id.label1);
            rowView.setTag(blView);
		}
		else {
			blView = (BluetoothDeviceView) rowView.getTag();
		}
        Log.i(TAG, "" + position);

        if (type == 0) {
            if (position > 0 && position < 1 + bluetooth_devices.size()) {
                BluetoothDeviceInfo currentDevice = (BluetoothDeviceInfo) bluetooth_devices.get(position - 1);
                blView.name.setText(currentDevice.getName());
            } else {
                Log.w(TAG, "position : " + position + " bluetooth devices size: " + bluetooth_devices.size());
                int index_into_other_devices = position - bluetooth_devices.size() - 2;
                Log.w(TAG, "Index into other_devices = " + index_into_other_devices);
                BluetoothDeviceInfo currentDevice = (BluetoothDeviceInfo) other_devices.get(index_into_other_devices);
                blView.name.setText(currentDevice.getName());
            }

        } else {

            if (position == 0) {
                blView.name.setText("Bluetooth Devices");
            } else {
                blView.name.setText("Other Events");
            }
        }
        return rowView;
		
	}

    public void setBluetoothDevices(List devices) {
        this.bluetooth_devices = devices;
        notifyDataSetChanged();
    }

    public void setOtherDevices(List devices) {
        this.other_devices = devices;
        notifyDataSetChanged();
    }

    public static class BluetoothDeviceView {
        protected TextView name;
	}
}
