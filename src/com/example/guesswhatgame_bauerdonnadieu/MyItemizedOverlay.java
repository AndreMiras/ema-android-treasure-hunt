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

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> implements
		LocationListener {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private final EventListenerList listeners = new EventListenerList();
	private Context mContext;
	private Location lastLocation;

	public MyItemizedOverlay(Drawable defaultMarker) {
		this(defaultMarker, null);
	}
	
	public MyItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
		  /*
		   * http://code.google.com/p/android/issues/detail?id=2035
		   * The subclass should call this as soon as it has data,
		   * before anything else gets called.
		   */
		  populate();
		}

	public void addOverlayItem(OverlayItem item) {
	    mOverlays.add(item);
	    populate();
	}

	public void addOverlayItems(ArrayList<OverlayItem> overlays) {
		for (OverlayItem overlayItem : overlays) {
		    mOverlays.add(overlayItem);
		}
	}

	public ArrayList<OverlayItem> getOverLayItems()
	{
		return mOverlays;
	}

	public boolean removeOverlayItem(OverlayItem item) {
		boolean removed = mOverlays.remove(item);
		populate();
		return removed;
	}

	public void addOverlayItemProximityListener(OverlayItemProximityListener listener) {
		listeners.add(OverlayItemProximityListener.class, listener);
	}

	public void removeOverlayItemProximityListener(OverlayItemProximityListener listener) {
        listeners.remove(OverlayItemProximityListener.class, listener);
    }

	public OverlayItemProximityListener[] getOverlayItemProximityListener() {
        return listeners.getListeners(OverlayItemProximityListener.class);
    }

	protected void fireOverlayItemNear(OverlayItem overlayItem) {
		for (OverlayItemProximityListener listener : getOverlayItemProximityListener()) {
			listener.overlayItemNear(overlayItem);
		}
    }

	/**
	 * Pop-ups the clue title when tapping on it
	 */
	@Override
	protected boolean onTap(int index) {
		String notCloseEnoughMessage = "Not close enough"; // TODO[hardcoded]: to localised in appropriated file

		boolean closeEnough = showPopupIfCloseEnough(index);
		if (!closeEnough) {
			OverlayItem item = mOverlays.get(index);
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			dialog.setTitle(item.getTitle());
			dialog.setMessage(notCloseEnoughMessage);
			dialog.show();
		}
		return closeEnough;
	}

	// TODO: we may later display the popup automatically when the user is close enough
	// this could be done using Proximity Alert
	private boolean showPopupIfCloseEnough(int index) {
		OverlayItem item = mOverlays.get(index);
		return showPopupIfCloseEnough(item);
	}

	private boolean showPopupIfCloseEnough(OverlayItem item) {
		int distanceMeter = 1000; // TODO[hardcoded]: to be defined has const somewhere
		boolean closeEnough = isClose(item.getPoint(), distanceMeter);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);

		if (closeEnough)
		{
			dialog.setTitle(item.getTitle());
			dialog.setMessage(item.getSnippet());
			dialog.show();
			fireOverlayItemNear(item);
		}

		return closeEnough;
	}

	/**
	 * Loops over all the markers and show the ones that are close enough
	 * @return true if a popup was shown
	 */
	private boolean showClosePopupIfAny()
	{
		boolean wasCloseEnough = false;
		ArrayList<OverlayItem> overlays = mOverlays;

		for (int i = 0; i < mOverlays.size(); i++) {
			OverlayItem item = mOverlays.get(i);
			wasCloseEnough |= showPopupIfCloseEnough(item);
		}

		return wasCloseEnough;
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
		showClosePopupIfAny();
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
