package com.example.guesswhatgame_bauerdonnadieu;
/**
 * https://developers.google.com/maps/documentation/android/v1/hello-mapview
 */

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> implements LocationListener {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private Location lastLocation;

	public MyItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}
	
	public MyItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
		}

	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}

	public ArrayList<OverlayItem> getOverLays()
	{
		return mOverlays;
	}

	@Override
	protected boolean onTap(int index) {
	  return onTapPopup(index);
	}

	// TODO: we may later display the popup automatically when the user is close enough
	// this could be done using Proximity Alert
	private boolean onTapPopup(int index) {
		String defaultMessage = "Not close enough"; // TODO[hardcoded]: to localized in appropriated file
		int distanceMeter = 1000; // TODO[hardcoded]: to be defined has const somewhere
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);

		dialog.setTitle(item.getTitle());
		if (isClose(item.getPoint(), distanceMeter))
		{
			dialog.setMessage(item.getSnippet());
		}
		else
		{
			dialog.setMessage(defaultMessage);
		}
		dialog.show();

		return true;
	}

	/**
	 *
	 * @param p1 the point to compare lastLocation distance to
	 * @param distanceMeter the distance in meter
	 * @return true if the user (lastLocation) is close enough to the marker
	 */
	private boolean isClose(GeoPoint p1, int distanceMeter) {
		// TODO: promp the user to enable the GPS
		// http://www.vogella.com/articles/AndroidLocationAPI/article.html#locationapi
		if (lastLocation == null)
		{
			return false;
		}
		int lastLocationLat = (int) (lastLocation.getLatitude() * 1E6);
		int lastLocationLng = (int) (lastLocation.getLongitude() * 1E6);
		GeoPoint p2 = new GeoPoint(lastLocationLat, lastLocationLng);
		return areClose(p1, p2, distanceMeter);
	}

	/**
	 *
	 * @param p1 first GeoPoint
	 * @param p2 second GeoPoint
	 * @param maxDistanceMeter the distance in meter
	 * @return true if p1 and p2 are separated by less than distanceMeter
	 */
	private boolean areClose(GeoPoint p1, GeoPoint p2, int maxDistanceMeter)
	{
		Location l1 = new Location("l1");
		l1.setLatitude(p1.getLatitudeE6() / 1E6);
		l1.setLongitude(p1.getLongitudeE6() / 1E6);

		Location l2 = new Location("l2");
		l2.setLatitude(p2.getLatitudeE6() / 1E6);
		l2.setLongitude(p2.getLongitudeE6() / 1E6);

		return areClose(l1, l2, maxDistanceMeter);
	}

	/**
	 *
	 * @param l1 first Location
	 * @param l2 second Location
	 * @param maxDistanceMeter the distance in meter
	 * @return true if p1 and p2 are separated by less than distanceMeter
	 */
	private boolean areClose(Location l1, Location	 l2, int maxDistanceMeter)
	{
		float actualDistanceMeter = l1.distanceTo(l2);
		return (actualDistanceMeter < (float)maxDistanceMeter);
	}

	// TODO: crashing: You are only allowed to have a single MapView in a
	// MapActivity
	/*
	private boolean onTapPopup2(int index) {
		OverlayItem item = mOverlays.get(index);
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// TODO: is this the correct way to do to get the map view?
		MapView map = (MapView) ((MapActivity) mContext)
		.findViewById(R.id.map_view);
		View popUp = inflater.inflate(R.layout.activity_map, map, false);
		MapView.LayoutParams mapParams = new MapView.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, item.getPoint(), 0, 0,
				MapView.LayoutParams.BOTTOM_CENTER);
		map.addView(popUp, mapParams);
		return true;
	}
	*/

	@Override
	protected OverlayItem createItem(int i) {
		if (i >= size())
			return null;
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	public void onLocationChanged(Location location) {
		lastLocation = location;
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

}
