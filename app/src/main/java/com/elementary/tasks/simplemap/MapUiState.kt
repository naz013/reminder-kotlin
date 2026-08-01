package com.elementary.tasks.simplemap

import android.location.Address
import androidx.compose.ui.graphics.Color
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapType

data class MapUiState(
  val selectedMapType: Int = GoogleMap.MAP_TYPE_NORMAL,
  val mapType: MapType = MapType.NORMAL,
  val selectedMarkerStyle: Int = MapConfig.DEFAULT_MARKER_STYLE,
  val radiusMeters: Int = MapConfig.Radius.DEFAULT_METERS,
  val maxRadiusMeters: Float = MapConfig.Radius.MAX_METERS.toFloat(),
  val radiusText: String = "",
  val activePicker: MapPicker? = null,
  val markers: List<MarkerState> = emptyList(),
  val hasLocationPermission: Boolean = false,
  val recentPlaces: List<UiPlaceList> = emptyList(),
  val addressQuery: String = "",
  val addressSuggestions: List<Address> = emptyList(),
  val mapStyleOptions: MapStyleOptions? = null,
  val selectedMapStyle: Int = MapConfig.DEFAULT_MAP_STYLE,
  val markerStyleSliderColors: List<Color> = emptyList(),
  val hapticFeedbackEnabled: Boolean = true,
)

/** Which single picker card (if any) is currently expanded - mutually exclusive by design. */
enum class MapPicker { LAYERS, MAP_STYLE, MARKER_STYLE, RADIUS, PLACES }
