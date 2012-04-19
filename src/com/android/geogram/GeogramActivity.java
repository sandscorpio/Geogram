package com.android.geogram;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


public class GeogramActivity extends MapActivity {
	private static final int DEFAULT_ZOOM = 13;
	private static final String TAG = "Geogram";
	
	private LocationManager _locManager;
	private GeoInstagram _geoInstagram;
	private double _lat;
	private double _long;
	MapView mapView;
    MyLocationOverlay mUserLocOverlay;
	HelloItemizedOverlay _itemizedoverlay;
	List<Overlay> _mapOverlays;
	long unixTime, WeekFromNow, Recentl;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button Recent = (Button) findViewById(R.id.recent);
        Button Best = (Button) findViewById(R.id.best);
        
        //setActionBarContentView(R.layout.main);
        
        
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(DEFAULT_ZOOM);
        
        _locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        centerOnLocation(mapView, _locManager);    
        
        Drawable drawable = this.getResources().getDrawable(R.drawable.ga_map_pin);
        _itemizedoverlay = new HelloItemizedOverlay(drawable, this);
        
        mUserLocOverlay = new MyLocationOverlay(this, mapView);
        mapView.getOverlays().add(mUserLocOverlay);		
		mUserLocOverlay.enableMyLocation();
		mUserLocOverlay.enableCompass();

		_geoInstagram = new GeoInstagram(this);
		
		unixTime = System.currentTimeMillis() / 1000L;
		WeekFromNow = unixTime - 604800000 / 1000L;
		Recentl = unixTime - 86400000 / 1000L;
		
		
				
		_geoInstagram.getPics(_lat, _long, WeekFromNow);
		
		
		Recent.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Switching to Register screen
				mapView.getOverlays().clear();
				mapView.invalidate();
				mapView.getOverlays().add(mUserLocOverlay);		
				mUserLocOverlay.enableMyLocation();
				mUserLocOverlay.enableCompass();

				_geoInstagram.getPics(_lat, _long);
				
				
			}
		});
		
		Best.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Switching to Register screen
				mapView.getOverlays().clear();
				mapView.invalidate();
				mapView.getOverlays().add(mUserLocOverlay);		
				mUserLocOverlay.enableMyLocation();
				mUserLocOverlay.enableCompass();
				_geoInstagram.getPics(_lat, _long, WeekFromNow);
				
				
			}
		});
		
		/*
    	GeoPoint point = new GeoPoint((int)(1E6*_lat), (int)(1E6*_long));
        OverlayItem item = new OverlayItem(point, "", "");
        _itemizedoverlay.addOverlay(item);	
        mapView.getOverlays().add(_itemizedoverlay);
		*/
    }
     
    public void centerOnLocation(MapView mapView, LocationManager locManager) {
		Location mGPSLoc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location mNetLoc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Log.i(TAG, "GPS LOCATION = " + mGPSLoc + " " + "NETWORK LOCATION = " + mNetLoc);
		
		if (mGPSLoc != null) {
			GeoPoint newPoint = locationToGeoPoint(mGPSLoc);
			mapView.getController().animateTo(newPoint);
		}
		else{
			GeoPoint newPoint = locationToGeoPoint(mNetLoc);
			mapView.getController().animateTo(newPoint);
		}
	}
   
    public GeoPoint locationToGeoPoint(Location mLocation) {
		_lat = mLocation.getLatitude();
		_long = mLocation.getLongitude();
		GeoPoint newPoint = new GeoPoint((int) (_lat * 1e6), (int) (_long * 1e6));
		return newPoint;
	}
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    public void initOverlays(InstaData[] data){
    	int num = 0;
    	
    	Log.i(TAG, "initOverlays() called");
    	for (InstaData s : data) {
    		String uri = s.url;
    		double lat = Double.valueOf(s.latitude);
    		double lon = Double.valueOf(s.longitude);
    		String id = String.valueOf(s.id);
    		
    		Log.i(TAG, "URI = " + uri + "lat " + lat);
    		
        	GeoPoint point = new GeoPoint((int)(1E6*lat), (int)(1E6*lon));
            OverlayItem item = new OverlayItem(point, id+":" + String.valueOf(num+1), uri);
            _itemizedoverlay.addOverlay(item);
            
            
            num++;
            if (num == 7) {
            	break;
            }
            
    	}
    	
    	mapView.getOverlays().add(_itemizedoverlay);
    }
}