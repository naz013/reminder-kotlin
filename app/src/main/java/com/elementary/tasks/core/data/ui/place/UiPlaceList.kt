package com.elementary.tasks.core.data.ui.place

import android.graphics.drawable.Drawable
import com.google.android.gms.maps.model.LatLng

data class UiPlaceList(
  val id: String,
  val marker: Drawable,
  val name: String,
  val latLng: LatLng,
  val markerStyle: Int,
  val formattedDate: String?
)
