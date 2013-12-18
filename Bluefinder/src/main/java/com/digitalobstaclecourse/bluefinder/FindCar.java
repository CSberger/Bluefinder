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

import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.digitalobstaclecourse.bluefinder.util.IabHelper;
import com.digitalobstaclecourse.bluefinder.util.IabResult;
import com.digitalobstaclecourse.bluefinder.util.Inventory;
import com.digitalobstaclecourse.bluefinder.util.Purchase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FindCar extends FragmentActivity implements
        NotifyNoBluetoothDialog.NoticeDialogListener, BluetoothDeviceListFragment.Callbacks, BuyInAppDialogFragment.PurchaseDialogListener {
    private static String TAG = "FindCar";
    private boolean mTwoPane = false;
    private IabHelper mIabHelper;
    private DataAccessModule mDataAccess;

    private static String ITEM_TYPE_INFINITE = "bluefinder_full_pass";
    private static String ITEM_TYPE_CONSUMABLE = "bluefinder_uses_refill";
    private View mActionView;


    void insert_paired_devices_into_database() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showNoBluetoothDialog();
        }

        Set<BluetoothDevice> paired_devices = null;
        if (mBluetoothAdapter != null) {
            paired_devices = mBluetoothAdapter.getBondedDevices();
        }
        DataAccessModule dataAccess = DataAccessModule.getDataAccessModule(this);
        if (paired_devices != null) {
            for (BluetoothDevice d : paired_devices) {
                Log.d(TAG, "msg" + d.getName());
                dataAccess.add_device(d);
            }
        }
        else {
            Log.e(TAG, "paired_devices  == null");
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
        mIabHelper = new IabHelper(getApplicationContext(), getString(R.string.play_public_key));
        mIabHelper.enableDebugLogging(true, "iab_TAG");
        mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                Log.i("iab", "iab_setup is finished");

                mIabHelper.queryInventoryAsync(true, new ArrayList<String>(Arrays.asList("bluefinder_uses_refill", "bluefinder_full_pass")), new IabHelper.QueryInventoryFinishedListener() {
                    @Override
                    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                        //inv.erasePurchase();
                        Log.i("iab_result_tag", "queryInventoryFinished");
                        Log.d(TAG, "QueryInventoryFinished" + inv);

                        //consumeProducts(inv);

                        Log.d(TAG, "Consuming Purchases");
                        mIabHelper.consumeAsync(inv.getAllPurchases(), new IabHelper.OnConsumeMultiFinishedListener() {
                            @Override
                            public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {
                                for (int i = 0; i < purchases.size(); i++) {

                                    IabResult result = results.get(i);
                                    Purchase purchase = purchases.get(i);
                                    Log.d(TAG, "Outstanding Purchase consumed:" + purchase.getSku());
                                    if (result.isSuccess()) {
                                        if (purchase.getSku() == ITEM_TYPE_CONSUMABLE) {
                                            get_data_access().registerConsumablePurchase();
                                            refresh_action_view();
                                        }
                                        if (purchase.getSku() == ITEM_TYPE_INFINITE) {
                                            get_data_access().registerInfinitePurchase();
                                            refresh_action_view();
                                        }
                                    }
                                }
                            }
                        });
                    }
                });


            }
        });


        Log.d(TAG,"service Bound");
        if (findViewById(R.id.map) != null) {
            mTwoPane = true;
            Log.d(TAG, "twopane");
            BluetoothDeviceListFragment list_frag = ((BluetoothDeviceListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.device_list));

            list_frag.setActivateOnItemClick(true);
            list_frag.getListView().setEmptyView(findViewById(R.id.empty_list_view));

        }


        DataAccessModule dataAccess = get_data_access();
        dataAccess.verify_db_existence(DataAccessModule.SQLModelOpener.DEVICE_TABLE_NAME);
        dataAccess.verify_db_existence(DataAccessModule.SQLModelOpener.LOCATION_TABLE_NAME);

        ArrayList<String> device_name_list = new ArrayList<String>();
        ArrayList<BluetoothDeviceInfo> device_info_list = new ArrayList<BluetoothDeviceInfo>();
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

    private DataAccessModule get_data_access() {
        if (mDataAccess == null) {
            mDataAccess = DataAccessModule.getDataAccessModule(getApplicationContext());
        }
        return mDataAccess;

    }

    private void consumeProducts(Inventory inv) {

        if (inv != null) {
            for (Purchase p : inv.getAllPurchases()) {
                Log.i(TAG, "SKU = " + p.getSku());


            };
            mIabHelper.consumeAsync(inv.getAllPurchases(), new IabHelper.OnConsumeMultiFinishedListener() {
                @Override
                public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {
                    for (int i = 0; i < purchases.size(); i++) {
                        IabResult result = results.get(i);
                        Purchase purchase = purchases.get(i);
                        if (result.isSuccess()) {
                            if (purchase.getSku() == ITEM_TYPE_CONSUMABLE) {
                                get_data_access().registerConsumablePurchase();
                                refresh_action_view();
                            }
                            if (purchase.getSku() == ITEM_TYPE_INFINITE) {
                                get_data_access().registerInfinitePurchase();
                                refresh_action_view();
                            }
                        }
                    }
                }
            });
        }

    }


    public void onItemSelected(String id) {
        if(getUsesRemaining() > 0) {
            Log.d(TAG, "Item selected: " + id);
            if (mDataAccess.locationsExistForId(id)) {
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
                mDataAccess.increment_number_of_locations();
            } else {
                Toast.makeText(this, "No locations recorded for this device", Toast.LENGTH_LONG).show();
            }
        }
    }

    private int getUsesRemaining() {
        return get_data_access().get_remaining_locations();
    }

    private int totalPurchasedUses() {

        return 0;
    }

    private void refresh_action_view() {
        Log.d(TAG, "refresh_action_view");
        //Menu menu = (Menu) findViewById(R.menu.activity_find_car);
        //MenuItem menu_ammo_item = menu.findItem(R.id.menu_ammo);
        View actionView = mActionView;
        TextView tv = (TextView)actionView.findViewById(R.id.uses_remaining);
        Log.d(TAG, String.format("refresh_action_view - get Uses Remaining %d", getUsesRemaining()));

        tv.setText(String.format("%d", getUsesRemaining()));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_find_car, menu);
        MenuItem _menuItemAction = menu.findItem(R.id.menu_ammo);
        View actionView = null;
        if (_menuItemAction != null) {
            actionView = _menuItemAction.getActionView();
        }
        else {
            Log.e(TAG, "getActionView()");
        }
        assert actionView != null;
        mActionView = actionView;

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onclick actionView");
                DialogFragment newFragment = new BuyInAppDialogFragment();
        newFragment.show(getFragmentManager(),"miss");
    }
});
        TextView uses_remaining_textview = (TextView)actionView.findViewById(R.id.uses_remaining);
        int remaining_locations = getUsesRemaining();
        uses_remaining_textview.setText(String.format("%d", remaining_locations));
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

    @Override
    public void onDialogPositiveClick(BuyInAppDialogFragment dialog) {

        int selectedItem = dialog.getSelectedItem();
        if (selectedItem == -1) {
            return;
        }
        Log.d(TAG, "purchase Dialog Selected Item: " + selectedItem);

        String[] purchaseOptions = new String[]{ITEM_TYPE_INFINITE, ITEM_TYPE_CONSUMABLE};

        String product_id = purchaseOptions[selectedItem];
        Log.d(TAG, "Purchasing: " + product_id);

        final String finalProduct_id = product_id;
        mIabHelper.launchPurchaseFlow(this, product_id, 0, new IabHelper.OnIabPurchaseFinishedListener() {
            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase info) {
                Log.i(TAG, "IabPurchaseFinished: Purchase of " + info);
                if (result.isSuccess()) {
                    if (info.getSku().equals(ITEM_TYPE_CONSUMABLE)) {
                        mIabHelper.consumeAsync(info, new IabHelper.OnConsumeFinishedListener() {
                            @Override
                            public void onConsumeFinished(Purchase purchase, IabResult result) {
                                if (result.isSuccess() ) {
                                    Log.i(TAG, "consumed " + purchase.getSku());
                                    get_data_access().registerConsumablePurchase();
                                    refresh_action_view();
                                }
                                else {
                                    Log.e(TAG, String.format("error in consuming %s: '%s' ", purchase.getSku(), result.getMessage()));
                                }
                            }
                        });
                    }
                } else {
                    if (result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {

                        Log.d(TAG, "Already Owned:" + finalProduct_id);
                        mIabHelper.consumeAsync(info, new IabHelper.OnConsumeFinishedListener() {
                            @Override
                            public void onConsumeFinished(Purchase purchase, IabResult result) {
                                if (result.isSuccess() ) {
                                    Log.i(TAG, "consumed " + purchase.getSku());
                                }
                            }
                        });
                    }
                }


            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass on the activity result to the helper for handling
        if (!mIabHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
