package com.elementary.tasks.core.interfaces

import com.google.android.gms.maps.model.LatLng

interface MapListener {
  fun placeChanged(place: LatLng, address: String)
  fun onZoomClick(isFull: Boolean)
  fun onBackClick()
  fun onRadiusChanged(radiusInM: Int)
}
