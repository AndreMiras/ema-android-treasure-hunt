package com.example.guesswhatgame_bauerdonnadieu;

import java.util.EventListener;

import com.google.android.maps.OverlayItem;

/**
 * Listener for OverlayItem proximity
 *
 */

public interface OverlayItemProximityListener extends EventListener {

	void overlayItemNear(OverlayItem overlayItem);
}
