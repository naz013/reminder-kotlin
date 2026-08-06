package com.elementary.tasks.places.create

import com.elementary.tasks.simplemap.MapConfig
import com.elementary.tasks.simplemap.MapMarker
import com.github.naz013.ui.common.R
import com.google.android.gms.maps.model.LatLng
import java.util.UUID

data class EditPlaceState(
  val screenTitle: Int = R.string.new_place,
  val id: String = UUID.randomUUID().toString(),
  val name: String = "",
  val nameError: Boolean = false,
  val canDelete: Boolean = false,
  val canSave: Boolean = false,
  val markers: List<EditMarker> = emptyList(),
  val lat: Double = 0.0,
  val lng: Double = 0.0,
  val address: String = "",
  val markerStyle: Int = MapConfig.DEFAULT_MARKER_STYLE,
  val markerRadius: Int = MapConfig.Radius.DEFAULT_METERS,
  val hasSameInDb: Boolean = false,
  val isFromFile: Boolean = false,
) {
  fun hasLatLng(): Boolean = lat != 0.0 && lng != 0.0
}

data class EditMarker(
  override val latLng: LatLng,
  override val style: Int,
  override val radius: Int,
  override val title: String
) : MapMarker
