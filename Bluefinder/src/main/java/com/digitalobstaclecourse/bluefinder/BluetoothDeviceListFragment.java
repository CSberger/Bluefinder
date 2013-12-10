package com.digitalobstaclecourse.bluefinder;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private static String TAG = "BluetoothDeviceListFragment";

    public interface Callbacks {
        public void onItemSelected(String id);


    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
            Log.d(TAG, "dummy onItemSelected");
        }
    };

    public BluetoothDeviceListFragment() {

    }

    public void refresh_devices() {
        Log.d(TAG, "Refresh device list");
        device_info_list.clear();
        DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(getActivity());
        for (BluetoothDeviceInfo device : dataAccess.getAllDevices()) {
            Log.d("PAIRDEVICE", "Device name:" + device.getName());

            device_info_list.add(new BluetoothDeviceInfo(device.getName(),
                    device.getAddress()));
        }

        setListAdapter(new BluetoothDeviceAdapter(getActivity(), device_info_list));
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setListAdapter();
        device_info_list = new ArrayList<BluetoothDeviceInfo>();
        ArrayList<String> device_name_list = new ArrayList<String>();
        DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(getActivity());
        refresh_devices();

        //getListView().setEmptyView(getActivity().findViewById(R.id.empty_list_view));
        //mListAdapter = new BluetoothDeviceAdapter(this,device_info_list);
        //mListView.setAdapter(mListAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_frag_layout, container, false);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Log.d(TAG, "onListItemClick");
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(device_info_list.get(position).getAddress());
    }

    public void setActivateOnItemClick(@SuppressWarnings("SameParameterValue") boolean activateOnItemClick) {
        Log.d(TAG, "setActivateOnItemClick:" + activateOnItemClick);
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);


    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
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
