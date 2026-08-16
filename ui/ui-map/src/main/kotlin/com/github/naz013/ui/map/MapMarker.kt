package com.github.naz013.ui.map

import com.google.android.gms.maps.model.LatLng

interface MapMarker {
  val style: Int
  val radius: Int
  val latLng: LatLng
  val title: String
}
