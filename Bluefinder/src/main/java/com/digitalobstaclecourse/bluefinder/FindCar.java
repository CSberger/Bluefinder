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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

public class FindCar extends FragmentActivity implements
        NotifyNoBluetoothDialog.NoticeDialogListener, BluetoothDeviceListFragment.Callbacks {
    private static String TAG = "FindCar";
    private static final String PREFS_NAME = "LOCATION_KEY_VALUE";
    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;

    // private ArrayAdapter<String> mListAdapter;
    private BluetoothDeviceAdapter mListAdapter;
    private boolean mTwoPane = false;
    private ArrayList<BluetoothDeviceInfo> device_info_list;
    private LocationManager locationManager;
    private String ITEM_TYPE_INFINITE = "bluefinder_full_pass";
    IInAppBillingService _service;


    private ServiceConnection mServiceConn;


    public void startSetup(final OnIabSetupFinishedListener listener) {
        mServiceConn =             new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                _service = IInAppBillingService.Stub.asInterface(iBinder);
                Log.d(TAG, "onServiceConnected");
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                _service = null;
            }
        };

    }

    Bundle get_available_purchases() throws RemoteException {
       ArrayList skuList = new ArrayList();
        skuList.add(ITEM_TYPE_INFINITE);
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
        if (_service == null) {
            Log.e(TAG, "service is null");
        }

        Bundle skuDetails = _service.getSkuDetails(3,getPackageName(),"inapp",querySkus);
        return skuDetails;


    }

    void log_purchases() {
        try {
            Bundle purchases = get_available_purchases();
            int response = purchases.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> purchases_details = purchases.getStringArrayList("DETAILS_LIST");
                for (String thisResponse : purchases_details) {
                    JSONObject object = new JSONObject(thisResponse);
                    String sku = object.getString("productId");
                    String price = object.getString("price");
                    Log.d(TAG, String.format("productID = %s, %s", sku, price));


                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    void handle_items() {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                IInAppBillingService _service = IInAppBillingService.Stub.asInterface(iBinder);


                try {
                    Bundle available_purchases =_service.getSkuDetails(3,getPackageName(),"inapp", null);
                    Bundle owned_items = _service.getPurchases(3,getPackageName(),ITEM_TYPE_INFINITE, null);
                    int response = owned_items.getInt("RESPONSE_CODE");
                    if (response == 0) {
                        ArrayList ownedSkus = owned_items.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
    }


    void insert_paired_devices_into_database() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showNoBluetoothDialog();
        }

        Set<BluetoothDevice> paired_devices = mBluetoothAdapter
                .getBondedDevices();
        DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(this);
        for (BluetoothDevice d : paired_devices) {
            Log.d(TAG, "msg" + d.getName());
            dataAccess.add_device(d);
        }
        Log.d(TAG, "Logging all devices");
        for (BluetoothDeviceInfo info : dataAccess.getAllDevices()) {
            Log.d(TAG, "Name = " + info.getName());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_car);


        Log.d("onCreate", "play services available? " +
                (ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext())));
        
        bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn, Context.BIND_AUTO_CREATE);
        Log.d(TAG,"service Bound");
        //log_purchases();

        if (findViewById(R.id.map) != null) {
            mTwoPane = true;
            Log.d(TAG, "twopane");
            BluetoothDeviceListFragment list_frag = ((BluetoothDeviceListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.device_list));

            list_frag.setActivateOnItemClick(true);
            list_frag.getListView().setEmptyView(findViewById(R.id.empty_list_view));

        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(this);
        dataAccess.verify_db_existence(DataAccessModule.SQLModelOpener.DEVICE_TABLE_NAME);
        dataAccess.verify_db_existence(DataAccessModule.SQLModelOpener.LOCATION_TABLE_NAME);
        BluetoothDeviceInfo[] devices = dataAccess.getAllDevices();

        final SharedPreferences locationKeyValue = getSharedPreferences(
                PREFS_NAME, 0);
        ArrayList<String> device_name_list = new ArrayList<String>();
        device_info_list = new ArrayList<BluetoothDeviceInfo>();
        insert_paired_devices_into_database();



        for (BluetoothDeviceInfo device : dataAccess.getAllDevices()) {
            Log.d("PAIRDEVICE", "Device name:" + device.getName());
            device_name_list.add(device.getName());
            device_info_list.add(new BluetoothDeviceInfo(device.getName(),
                    device.getAddress()));
        }
        if (findViewById(R.id.device_list) != null) {
            ((BluetoothDeviceListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.device_list))
                    .refresh_devices();
        }


    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceConn != null) {
            unbindService(mServiceConn);
        }
    }

    public void onItemSelected(String id) {
        Log.d(TAG, "Item selected: " + id);
        if (mTwoPane) {
            Log.d(TAG, "item selected = " + id);
            Bundle arguments = new Bundle();
            arguments.putString("DEVICE_ID", id);
            ((FindCarMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .displayLocationsForDevice(id);
        } else {
            Intent detailIntent = new Intent(this, FindCarLocatorActivity.class);
            detailIntent.putExtra("DEVICE_ID", id);

            startActivity(detailIntent);
        }
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
        MenuItem _menuItemAction = menu.findItem(R.id.menu_ammo);
        View actionView =_menuItemAction.getActionView();
        assert actionView != null;
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onclick actionView");
            }
        });
        actionView.findViewById(R.id.uses_remaining);

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

    private class OnIabSetupFinishedListener {
    }
}
