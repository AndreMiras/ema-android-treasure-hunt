package com.example.guesswhatgame_bauerdonnadieu;

/**
 * https://developers.google.com/maps/documentation/android/v1/hello-mapview
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MyMapActivity extends MapActivity {	
	// TODO : mickael (solve) debug
	private boolean clue1Found = false; 
	private boolean clue2Found = false;
	private boolean clue3Found = false;
	
	
	private final int RESULT_CLOSE_ALL = 30;
	public static final String PREFS_NAME = "MyPrefsFile";	
	

	private MapView mMapView;
	private MapController mController;
	private LocationManager locationManager;
	private MyItemizedOverlay itemizedOverlay;
	private MyLocationOverlay myLocationOverlay;
	/*
	 * The list of all clue markers that could be found in the map
	 */
	private ArrayList<OverlayItem> allClueMarkersOverlayItems;
	/*
	 * The list of already found clue markers
	 */
	private ArrayList<OverlayItem> foundClueMarkersOverlayItems;
	private Random randomGenerator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		setupMapView();
		loadMarkers();
		optimalMapSetup();
		setupLocationManager();
	}

	private void setupMapView() {
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setBuiltInZoomControls(true);
		mController = mMapView.getController();

		// sets the default marker for all overlay items
		Drawable drawable = this.getResources()
				.getDrawable(R.drawable.blue_dot);
		itemizedOverlay = new MyItemizedOverlay(drawable,
				this);
		List<Overlay> mapOverlays = mMapView.getOverlays();

		// sets myLocation
		myLocationOverlay = new MyLocationOverlay(
				getApplicationContext(),
				mMapView);
		myLocationOverlay.enableMyLocation();
		// centers the map on user location
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mController.animateTo(myLocationOverlay.getMyLocation());
				mController.setZoom(17);
			}
		});

		// adds itemizedOverlay and myLocationOverlay
		mapOverlays.add(itemizedOverlay);
		mapOverlays.add(myLocationOverlay);
		mMapView.invalidate(); // refreshes the map
	}

	private void optimalMapSetup()
	{
		mController.zoomToSpan(
				itemizedOverlay.getLatSpanE6(),
				itemizedOverlay.getLonSpanE6());
	}

	private void setupLocationManager()
	{
		// getSystemService
		// Get the location manager
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    /*
	    // Define the criteria how to select the locatioin provider -> use
	    // default
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		// Initialize the location fields
		if (location != null) {
			// System.out.println("Provider " + provider + " has been selected.");
			onLocationChanged(location);
		} else {
			// latituteField.setText("Location not available");
			// longitudeField.setText("Location not available");
		}
	     */
	}

	// TODO: we should be loading them from XML files or something like that
	private void loadMarkers() {
		if (allClueMarkersOverlayItems == null)
		{
			addRandomMarkers(5); // TODO[hardcoded]: use consts instead
		}
		else
		{
			itemizedOverlay.addOverlays(allClueMarkersOverlayItems);
		}
		// allClueMarkersOverlayItems
	}

	private void addRandomMarkers(int count)
	{
		ArrayList<String> markerDescriptions = new ArrayList<String>();
		for (int i = 0; i < count; i++) {
			markerDescriptions.add("Clue " + i);
		}
		addRandomMarkers(markerDescriptions);
		allClueMarkersOverlayItems = itemizedOverlay.getOverLays();
	}

	private void addRandomMarkers(ArrayList<String> markerDescriptions) {
		randomGenerator = new Random();
		String markerTitle;
		int i = 0;
		for (String markerDescription : markerDescriptions) {
			markerTitle = "Clue " + i;
			addRandomMarker(markerTitle, markerDescription);
			i++;
		}
	}

	// TODO: Get available points from an XML file and "randomize" it
	/**
	 *
	 * @return a random GeoPoint that isn't already used
	 */
	private GeoPoint getRandomGeoPoint() {
		// generates random lat long
		// TODO: this is for debugging purposes, but the GeoPoint must be
		// physically reachable
		// int maxLat = 90;
		// int maxLng = 180;
		int franceLat = 46;
		int franceLng = 2;
		//
		int randomLat = getRandomInt(franceLat - 1, franceLat + 1);
		int randomLng = getRandomInt(franceLng - 1, franceLng + 1);
		// converts to micro dedegrees GeoPoint
		GeoPoint point = new GeoPoint((int) (randomLat * 1E6),
				(int) (randomLng * 1E6));

		return point;
	}

	private int getRandomInt(int min, int max) {
		return randomGenerator.nextInt(max - min + 1) + min;
	}

	private void addRandomMarker(String markerTitle, String markerDescription) {
		GeoPoint point = getRandomGeoPoint();
		OverlayItem overlayitem = new OverlayItem(point, markerTitle,
				markerDescription);
		itemizedOverlay.addOverlay(overlayitem);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_map, menu);
		return true;
	}

	public void goToSolveActivityClick(View v)
    {	
		Intent activity = new Intent(this,SolveActivity.class);		
		
		String ovitTitle = "";
		String ovitSnippet = "";		
		String activityExtraValue = "";
		
		ArrayList<String> activityExtras = new ArrayList<String>();
		
		String cluesVarNames = this.getResources().getString(R.string.clues_var_names);
		String separator = this.getResources().getString(R.string.clues_var_separator);
		
		for ( OverlayItem ovit : allClueMarkersOverlayItems)
		// TODO : travailler avec la liste de markers découverts
		//for ( OverlayItem ovit : foundClueMarkersOverlayItems)
		{
			ovitTitle = ovit.getTitle();
			ovitSnippet = ovit.getSnippet();
			activityExtraValue = ovitTitle + separator + ovitSnippet;
			activityExtras.add(activityExtraValue);
		}		
		activity.putExtra(cluesVarNames, activityExtras);		
		startActivityForResult(activity, RESULT_CLOSE_ALL);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch ( resultCode)
		{
			case RESULT_CLOSE_ALL:
				setResult(RESULT_CLOSE_ALL);
				MyMapActivity.this.finish();
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * Disables:
	 *	- myLocationOverlay
	 * 	- location updates requests
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		myLocationOverlay.disableMyLocation();
		locationManager.removeUpdates(itemizedOverlay);
	}

	/**
	 * Enables:
	 *	- myLocationOverlay
	 * 	- location updates requests
	 */
	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();

		// Starts listeners
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				1, itemizedOverlay);
	}

	public void savePreferences() {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		boolean testBoolValue = false;
		editor.putBoolean("testBoolValue", testBoolValue);

		// Commit the edits!
		editor.commit();

		/*
		 * I'd like to be able to save the whole thing
		 * allClueMarkersOverlayItems and foundClueMarkersOverlayItems But I
		 * feel like I'm gonna have to do it myself using either sqlite or raw
		 * files :(
		 */
		/*
		 * savedInstanceState.putSerializable("allClueMarkersOverlayItems",
		 * allClueMarkersOverlayItems);
		 * savedInstanceState.putSerializable("foundClueMarkersOverlayItems",
		 * foundClueMarkersOverlayItems);
		 */
	}

	public void restorePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		boolean silent = settings.getBoolean("silentMode", false);
		// setSilent(silent);

		/*
		 * allClueMarkersOverlayItems = (ArrayList<OverlayItem>)
		 * savedInstanceState .getSerializable("allClueMarkersOverlayItems");
		 * foundClueMarkersOverlayItems = (ArrayList<OverlayItem>)
		 * savedInstanceState .getSerializable("foundClueMarkersOverlayItems");
		 */
	}
	
	public void fakeClue1(View v)
	{
		clue1Found = true;
	}
	
	public void fakeClue2(View v)
	{
		clue2Found = true;
	}
	
	public void fakeClue3(View v)
	{
		clue3Found = true;
	}
}
