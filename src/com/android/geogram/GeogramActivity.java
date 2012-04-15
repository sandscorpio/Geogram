package com.android.geogram;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class GeogramActivity extends MapActivity {
	private static final int DEFAULT_ZOOM = 12;
	private static final String TAG = "Geogram";
	
	private LocationManager _locManager;
	private GeoInstagram _geoInstagram;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);
        
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(DEFAULT_ZOOM);
        
        _locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        centerOnLocation(mapView, _locManager);       
        
        MyLocationOverlay mUserLocOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(mUserLocOverlay);
		
		mUserLocOverlay.enableMyLocation();
		mUserLocOverlay.enableCompass();

		_geoInstagram = new GeoInstagram();
		_geoInstagram.getPics();
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
   
    public static GeoPoint locationToGeoPoint(Location mLocation) {
		double mLat = mLocation.getLatitude();
		double mLon = mLocation.getLongitude();
		GeoPoint newPoint = new GeoPoint((int) (mLat * 1e6), (int) (mLon * 1e6));
		return newPoint;
	}
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}