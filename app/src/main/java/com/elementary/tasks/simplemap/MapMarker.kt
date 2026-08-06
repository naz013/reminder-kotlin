package com.elementary.tasks.simplemap

import com.google.android.gms.maps.model.LatLng

interface MapMarker {
  val style: Int
  val radius: Int
  val latLng: LatLng
  val title: String
}
