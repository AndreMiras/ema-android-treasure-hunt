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
import android.location.Location;
import android.location.LocationListener;
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
	private final int RESULT_CLOSE_ALL = 30;
	public static final String PREFS_NAME = "MyPrefsFile";
	
	// Solution de l'énigme
	private static String enigmaSolution = "solution";
	

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
	private Random randomGenerator = new Random();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		setupMapView();
		setupLocationManager();
		waitForFirstFix();
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

		// adds itemizedOverlay and myLocationOverlay
		mapOverlays.add(itemizedOverlay);
		mapOverlays.add(myLocationOverlay);
		mMapView.invalidate(); // refreshes the map
	}

	// TODO: display some kind of animation and message saying we are waiting for GPS FIX
	/**
	 * - centers the map on user location
	 * - loads makers around the user location
	 */
	private void waitForFirstFix()
	{

		// centers the map on user location
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				GeoPoint point = myLocationOverlay.getMyLocation();
				mController.animateTo(point);
				mController.setZoom(17);

				loadMarkers();
				optimalMapSetup();
			}
		});
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
	}

	// TODO: we should be loading them from XML files or something like that
	private void loadMarkers() {

		int squareSizeMeter = 10 * 1000; // 10 KM // TODO[hardcoded]: use consts instead
		GeoPoint squareCenter = myLocationOverlay.getMyLocation();

		if (allClueMarkersOverlayItems == null) // TODO: is this still needed?
		{
			addRandomMarkers(squareCenter, squareSizeMeter, 5); // TODO[hardcoded]: use consts instead
		}
		// itemizedOverlay.addOverlays(allClueMarkersOverlayItems); // TODO: is this still needed?
	}

	private void addRandomMarkers(GeoPoint squareCenter, int squareSizeMeter, int count)
	{
		ArrayList<GeoPoint> geoPoints = getRandomGeoPoints(squareCenter, squareSizeMeter, count);
		GeoPoint markerPoint;
		String markerTitle;
		String markerDescription;
		for (int i = 0; i < count; i++) {
			markerPoint = geoPoints.get(i);
			markerTitle = "Title " + i;
			markerDescription = "Clue " + i;
			addMarker(markerPoint, markerTitle, markerDescription);
		}
		// allClueMarkersOverlayItems = itemizedOverlay.getOverLays(); // TODO: is this still needed?
	}

	private void addMarker(GeoPoint markerPoint, String markerTitle, String markerDescription) {
		OverlayItem overlayitem = new OverlayItem(markerPoint, markerTitle,
				markerDescription);
		itemizedOverlay.addOverlay(overlayitem);
	}

	/**
	 * This is quite precise as long as the random distance offset is between 10-100 km
	 * @param originGeoPoint
	 * @param distanceXMeter
	 * @param distanceYMeter
	 * @return
	 */
	private GeoPoint addDistanceOffset(GeoPoint originGeoPoint, double distanceXMeter, double distanceYMeter)
	{
		GeoPoint point;
		final double earthRadiusMeter = 6378137;
		double lat0 = originGeoPoint.getLatitudeE6() / 1E6;
		double lng0 = originGeoPoint.getLongitudeE6() / 1E6;
		// http://stackoverflow.com/questions/2839533/adding-distance-to-a-gps-coordinate
		double lat = lat0 + (180/Math.PI) * (distanceYMeter/earthRadiusMeter);
		double lng = lng0 + (180/Math.PI) * (distanceXMeter/earthRadiusMeter)/Math.cos(lat0);
		// radius = (int)projection.metersToEquatorPixels(item.getRadiusInMeters());
		// converts to micro dedegrees GeoPoint
		// GeoPoint point = new GeoPoint((int) (randomLat * 1E6), (int) (randomLng * 1E6));
		int latE6 = (int) (lat * 1E6);
		int lngE6 = (int) (lng * 1E6);
		point = new GeoPoint(latE6, lngE6);

		return point;
	}

	// TODO: update docstring
	// TODO: Get available points from an XML file and "randomize" it
	/**
	 *
	 * @return a random GeoPoint in the given square
	 */
	private GeoPoint getRandomGeoPoint(GeoPoint squareCenter, int squareSizeMeter) {
		// generates random lat long
		// TODO: this is for debugging purposes, but the GeoPoint must be
		// physically reachable
		// int maxLat = 90;
		// int maxLng = 180;
		int latE6 = squareCenter.getLatitudeE6();
		int lngE6 = squareCenter.getLongitudeE6();
		double lat0 = squareCenter.getLatitudeE6() / 1E6;
		double lng0 = squareCenter.getLongitudeE6() / 1E6;
		//
		int randomLat = getRandomInt(latE6 - 1, latE6 + 1);
		int randomLng = getRandomInt(lngE6 - 1, lngE6 + 1);
		// http://stackoverflow.com/questions/2839533/adding-distance-to-a-gps-coordinate
		double earthRadiusMeter = 6378137;
		double distanceY = squareSizeMeter / 2;
		double distanceX = squareSizeMeter / 2;

		GeoPoint point;
		point = addDistanceOffset(squareCenter, -distanceX, distanceY);
		// TODO[debug]: just for testing proses
		OverlayItem overlayitem = new OverlayItem(point, "markerTitle",
				"'markerDescription");
		itemizedOverlay.addOverlay(overlayitem);


		point = addDistanceOffset(squareCenter, distanceX, distanceY);
		// TODO[debug]: just for testing proses
		overlayitem = new OverlayItem(point, "markerTitle",
				"'markerDescription");
		itemizedOverlay.addOverlay(overlayitem);


		point = addDistanceOffset(squareCenter, distanceX, -distanceY);
		// TODO[debug]: just for testing proses
		overlayitem = new OverlayItem(point, "markerTitle",
				"'markerDescription");
		itemizedOverlay.addOverlay(overlayitem);


		point = addDistanceOffset(squareCenter, -distanceX, -distanceY);
		// TODO[debug]: just for testing proses
		overlayitem = new OverlayItem(point, "markerTitle",
				"'markerDescription");
		itemizedOverlay.addOverlay(overlayitem);

		return point;
	}

	private int getRandomInt(int min, int max) {
		return randomGenerator.nextInt(max - min + 1) + min;
	}

	/**
	 * TODO: make the GeoPoints unique in the list
	 * Gives a list a random GeoPoints
	 * @param squareCenter
	 * @param squareSizeMeter
	 * @param nbPoints
	 * @return
	 */
	private ArrayList<GeoPoint> getRandomGeoPoints(GeoPoint squareCenter, int squareSizeMeter, int nbPoints)
	{
		ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		GeoPoint geoPoint;
		for (int i = 0; i < nbPoints; i++) {
			geoPoint = getRandomGeoPoint(squareCenter, squareSizeMeter);
			geoPoints.add(geoPoint);
		}

		return geoPoints;
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
		activity.putExtra(this.getResources().getString(R.string.enigma_solution_var), enigmaSolution);
		
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
		if (myLocationOverlay != null)
		{
			myLocationOverlay.disableMyLocation();
		}
		if (locationManager != null)
		{
			locationManager.removeUpdates(itemizedOverlay);
			locationManager.removeUpdates(locationListener);
		}
	}

	/**
	 * Enables:
	 *	- myLocationOverlay
	 * 	- location updates requests
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (myLocationOverlay != null)
		{
			myLocationOverlay.enableMyLocation();
		}

		if (locationManager != null)
		{
			// Starts listeners
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
					1, itemizedOverlay);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
					1, locationListener);
		}
	}

	private final LocationListener locationListener = new LocationListener() {
		// TODO: verify we're close enough from a marker before showing it
		public void onLocationChanged(Location location) {
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
	};

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
		//clue1Found = true;
	}
	
	public void fakeClue2(View v)
	{
		//clue2Found = true;
	}
	
	public void fakeClue3(View v)
	{
		//clue3Found = true;
	}
}
