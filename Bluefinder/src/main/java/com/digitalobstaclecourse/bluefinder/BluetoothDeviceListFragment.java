package com.digitalobstaclecourse.bluefinder;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Chris on 11/8/13.
 */

public class BluetoothDeviceListFragment extends ListFragment {
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private ArrayList<BluetoothDeviceInfo> device_info_list;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;
    // private ArrayAdapter<String> mListAdapter;
    private BluetoothDeviceAdapter mListAdapter;

    public interface Callbacks {
        public void onItemSelected(String id);


    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {

        }
    };

    public BluetoothDeviceListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setListAdapter();
        device_info_list = new ArrayList<BluetoothDeviceInfo>();
        ArrayList<String> device_name_list = new ArrayList<String>();
        DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(getActivity());
        for (BluetoothDeviceInfo device : dataAccess.getAllDevices()) {
            Log.d("PAIRDEVICE", "Device name:" + device.getName());
            device_name_list.add(device.getName());
            device_info_list.add(new BluetoothDeviceInfo(device.getName(),
                    device.getAddress()));
        }

        setListAdapter(new BluetoothDeviceAdapter(getActivity(), device_info_list));
        //mListAdapter = new BluetoothDeviceAdapter(this,device_info_list);
        //mListView.setAdapter(mListAdapter);
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);

    }
}
