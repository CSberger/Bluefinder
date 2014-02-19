package com.digitalobstaclecourse.bluefinder;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 *
 * Created by Chris on 11/27/13.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}


