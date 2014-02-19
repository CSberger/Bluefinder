package com.digitalobstaclecourse.bluefinder;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;


/**
 *
 * Created by Chris on 11/8/13.
 */

public class BluetoothDeviceListFragment extends ListFragment {
    private Callbacks mCallbacks = sDummyCallbacks;
    private ArrayList<BluetoothDeviceInfo> device_info_list;
    private ArrayList<BluetoothDeviceInfo> mOtherEvents;
    private static String TAG = "BluetoothDeviceListFragment";

    public interface Callbacks {
        public void onItemSelected(String id, String type);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id, String type) {
            Log.d(TAG, "dummy onItemSelected");
        }
    };

    public BluetoothDeviceListFragment() {

    }

    public void refresh_devices() {
        Log.d(TAG, "Refresh device list");
        device_info_list.clear();
        mOtherEvents.clear();

        DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(getActivity());
        for (BluetoothDeviceInfo device : dataAccess.getAllDevices()) {
            Log.d("PAIRDEVICE", "Device name:" + device.getName());

            device_info_list.add(new BluetoothDeviceInfo(device.getName(),
                    device.getAddress()));
        }
        mOtherEvents.add(new BluetoothDeviceInfo("Last Power Location", getString(R.string.POWER)));
        setListAdapter(new BluetoothDeviceAdapter(getActivity(), device_info_list, mOtherEvents));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        device_info_list = new ArrayList<BluetoothDeviceInfo>();
        mOtherEvents = new ArrayList<BluetoothDeviceInfo>();
        refresh_devices();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_frag_layout, container, false);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Log.d(TAG, "onListItemClick");
        super.onListItemClick(listView, view, position, id);
        if (((BluetoothDeviceAdapter) getListAdapter()).isHeader(position)) {
            Log.d(TAG, "Header clicked");
        } else {
            if (position < device_info_list.size() + 1) {
                mCallbacks.onItemSelected(device_info_list.get(position - 1).getAddress(), "BluetoothDevice");
            } else {
                mCallbacks.onItemSelected(mOtherEvents.get(position - 2 - device_info_list.size()).getAddress(), "otherDevice");
            }
        }
    }

    public void setActivateOnItemClick(@SuppressWarnings("SameParameterValue") boolean activateOnItemClick) {
        Log.d(TAG, "setActivateOnItemClick:" + activateOnItemClick);
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalAccessError("Activity must implement Fragment's callbacks");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }


}
