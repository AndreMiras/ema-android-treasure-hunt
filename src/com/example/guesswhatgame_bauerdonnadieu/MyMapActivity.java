package com.example.guesswhatgame_bauerdonnadieu;

/**
 * https://developers.google.com/maps/documentation/android/v1/hello-mapview
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MyMapActivity extends MapActivity implements OverlayItemProximityListener, LocationListener {
	private final int RESULT_CLOSE_ALL = 30;	
	private static int CLUE_NUMBERS = 0;
	public static final String PREFS_NAME = "MyPrefsFile";
	
	// Solution de l'�nigme
	private static String enigmaSolution = "solution";
	private static String markersTitle  = "";
	private List<String> clues;
	

	private MapView mMapView;
	private MapController mController;
	private LocationManager locationManager;
	private MyItemizedOverlay itemizedOverlay;
	private MyLocationOverlay myLocationOverlay;
	/*
	 * The list of already found clue markers
	 */
	private ArrayList<OverlayItem> foundClueMarkersOverlayItems = new ArrayList<OverlayItem>();
	private Random randomGenerator = new Random();
	// private final ProgressDialog progressDialog = new ProgressDialog(MyMapActivity.this);
	private ProgressDialog progressDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		markersTitle = this.getResources().getString(R.string.markers_title);
		
		Bundle extras = getIntent().getExtras();		
		if(extras != null) 
		{
			enigmaSolution = extras.getString(this.getResources().getString(R.string.enigma_solution_var));
			
			String cluesVarNames = this.getResources().getString(R.string.clues_var_names);
			this.clues = extras.getStringArrayList(cluesVarNames);	
			
			if(clues != null && clues.size() > 0)
			{				
				CLUE_NUMBERS = clues.size();
			}			
		}
		
		checkForGpsSettings();
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
		itemizedOverlay.addOverlayItemProximityListener(this);
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

	/**
	 * - waits for GPS location message
	 * - centres the map on user location
	 * - loads makers around the user location
	 */
	private void waitForFirstFix()
	{
		waitForLocationMessage();

		/**
		 * - dismisses the progressDialog
		 * - loads the markers
		 * - centres the map on the user location
		 */
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				endWaitForLocationMessage();
				GeoPoint point = myLocationOverlay.getMyLocation();
				mController.animateTo(point);
				mController.setZoom(17);

				loadMarkers();
				optimalMapSetup();
			}
		});
	}

	/**
	 * if GPS settings are enabled, shows the wait for location message
	 */
	private void waitForLocationMessage() {
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

			// Waiting for GPS location message
			if (progressDialog == null) {
				progressDialog = new ProgressDialog(MyMapActivity.this);
			}
			progressDialog.setMessage("Waiting for location...");
			progressDialog.show();
		}
	}

	private void endWaitForLocationMessage() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}

	/**
	 * Checks if the GPS is enabled, opens the location settings activity if not
	 */
	private boolean checkForGpsSettings()
	{
		setupLocationManager();
		boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(!isGPS)
		{
			// disables the wait for location progress dialog first
			endWaitForLocationMessage();
			openGpsSettingsDialog();
		}

		return isGPS;
	}

	private void openGpsSettingsDialog() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.gps);
		dialog.setTitle("GPS");
		dialog.setCancelable(false);
		dialog.show();

		Button gpsSettings = (Button) dialog.findViewById(R.id.gps_sett_button);
		gpsSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				startActivityForResult(
						new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
						0);
			}
		});

		Button gpsCancel = (Button) dialog.findViewById(R.id.gps_cancel_button);
		gpsCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				finish();
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
		if (locationManager == null)
		{
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
	}

	// TODO: we should be loading them from XML files or something like that
	private void loadMarkers() {

		int squareSizeMeter = 10 * 1000; // 10 KM // TODO[hardcoded]: use consts instead
		GeoPoint squareCenter = myLocationOverlay.getMyLocation();

		addRandomMarkers(squareCenter, squareSizeMeter, CLUE_NUMBERS);
	}

	private void addRandomMarkers(GeoPoint squareCenter, int squareSizeMeter, int count)
	{
		ArrayList<GeoPoint> geoPoints = getRandomGeoPoints(squareCenter, squareSizeMeter, count);
		GeoPoint markerPoint;
		String markerTitle;
		String markerDescription;
		for (int i = 0; i < count; i++) {
			markerPoint = geoPoints.get(i);
			markerTitle = markersTitle + i;
			markerDescription = clues.get(i);
			addMarker(markerPoint, markerTitle, markerDescription);
		}		
	}

	private void addMarker(GeoPoint markerPoint, String markerTitle, String markerDescription) {
		OverlayItem overlayitem = new OverlayItem(markerPoint, markerTitle,
				markerDescription);
		itemizedOverlay.addOverlayItem(overlayitem);
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
		/*
		 * TODO: is it OK if the point are not always physically
		 * reachable (as they're generated randomly)?
		 */
		double distanceY = squareSizeMeter / 2;
		double distanceX = squareSizeMeter / 2;

		GeoPoint topLeftPoint = addDistanceOffset(squareCenter, -distanceX, distanceY);
		/*
		// TODO[debug]: just for testing purposes
		OverlayItem overlayitem = new OverlayItem(topLeftPoint, "markerTitle1 top left",
				"'markerDescription");
		itemizedOverlay.addOverlay(overlayitem);
		*/

		GeoPoint topRightPoint = addDistanceOffset(squareCenter, distanceX, distanceY);
		/*
		// TODO[debug]: just for testing purposes
		overlayitem = new OverlayItem(topRightPoint, "markerTitle2 top right",
				"'markerDescription");
		itemizedOverlay.addOverlay(overlayitem);
		*/

		GeoPoint bottomRightPoint = addDistanceOffset(squareCenter, distanceX, -distanceY);
		/*
		// TODO[debug]: just for testing purposes
		overlayitem = new OverlayItem(bottomRightPoint, "markerTitle3 bottom right",
				"'markerDescription");
		itemizedOverlay.addOverlay(overlayitem);
		*/

		GeoPoint bottomLeftPoint = addDistanceOffset(squareCenter, -distanceX, -distanceY);
		/*
		// TODO[debug]: just for testing purposes
		overlayitem = new OverlayItem(bottomLeftPoint, "markerTitle4 bottom left",
				"'markerDescription");
		itemizedOverlay.addOverlay(overlayitem);
		*/

		// so we get a random lat/long that is between min lat/long (bottom left) and max lat/long top right corner
		// which ever left or right, we're interested in bottom and top
		int minLat = bottomLeftPoint.getLatitudeE6();
		int maxLat = topLeftPoint.getLatitudeE6();
		int randomLat = getRandomInt(minLat, maxLat);

		// which ever bottom or top, we're interested in left and right
		int minLng = topLeftPoint.getLongitudeE6();
		int maxLng = topRightPoint.getLongitudeE6();
		int randomLng = getRandomInt(minLng, maxLng);
		GeoPoint point = new GeoPoint(randomLat, randomLng);

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
				
		for ( OverlayItem ovit : foundClueMarkersOverlayItems)
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
			locationManager.removeUpdates(this);
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
			final long minTimeMilliseconds = 1000L; // 1 second
			final float minDistanceMeter = 1.0f; // 1 meter
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMilliseconds,
					minDistanceMeter, itemizedOverlay);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMilliseconds,
					minDistanceMeter, this);

		}
		waitForLocationMessage();
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

	@Override
	public void overlayItemNear(OverlayItem overlayItem) {
		// TODO: verify the item is not already present in the list
		foundClueMarkersOverlayItems.add(overlayItem);
		itemizedOverlay.removeOverlayItem(overlayItem);
		mMapView.invalidate();
	}

	@Override
	public void onLocationChanged(Location arg0) {
		endWaitForLocationMessage();
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
	}
}
