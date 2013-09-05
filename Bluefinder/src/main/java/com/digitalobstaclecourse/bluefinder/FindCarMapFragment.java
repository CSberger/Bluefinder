package com.digitalobstaclecourse.bluefinder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;


public class FindCarMapFragment extends SupportMapFragment {
	public FindCarMapFragment() {
		super();
	}

	public static FindCarMapFragment newInstance() {
		FindCarMapFragment instance = new FindCarMapFragment(); 
		
		return instance;
	}
	@Override
	public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
		View v = super.onCreateView(arg0, arg1, arg2);
		//initMap();
		return v;
		
	}
	private void initMap() {
		
		GoogleMap gm = this.getMap();
		Log.d("TAG", "initMap()");
	}
	
	
}
