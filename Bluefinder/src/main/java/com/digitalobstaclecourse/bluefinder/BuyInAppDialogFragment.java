package com.digitalobstaclecourse.bluefinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Chris on 12/10/13.
 */
public class BuyInAppDialogFragment extends DialogFragment {
    public static final String TAG = "BuyInAppDialogFragment";
    private int mSelectedItem = -1;
    PurchaseDialogListener mListener;
    public interface PurchaseDialogListener {
        public void onDialogPositiveClick(BuyInAppDialogFragment dialog);
    }
    public int getSelectedItem() {
        return mSelectedItem;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PurchaseDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Purchase", new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "Clicked Purchase");
                mListener.onDialogPositiveClick(BuyInAppDialogFragment.this);
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "Clicked Cancel");
            }
        }).setSingleChoiceItems(R.array.purchase_descriptions, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "clicked i = " + i);
                mSelectedItem = i;
            }
        });
        return builder.create();
    }
}