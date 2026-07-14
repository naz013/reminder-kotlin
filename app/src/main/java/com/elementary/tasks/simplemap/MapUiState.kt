package com.elementary.tasks.simplemap

import android.location.Address
import com.elementary.tasks.core.data.ui.place.UiPlaceList

data class MapUiState(
  val mapType: Int,
  val mapStyle: Int,
  val pendingStyle: Int,
  val radius: Int,
  val radiusValueTo: Float,
  val radiusText: String = "",
  val activePicker: MapPicker? = null,
  val markers: List<MarkerState> = emptyList(),
  val hasLocationPermission: Boolean = false,
  val recentPlaces: List<UiPlaceList> = emptyList(),
  val addressQuery: String = "",
  val addressSuggestions: List<Address> = emptyList(),
)

/** Which single picker card (if any) is currently expanded - mutually exclusive by design. */
enum class MapPicker { LAYERS, MAP_STYLE, MARKER_STYLE, RADIUS, PLACES }

/**
 * Events [SimpleMapView] itself must act on (camera animation, permission requests) are separate
 * [MapViewModel] properties ([MapViewModel.cameraEvent], [MapViewModel.permissionRequestEvent]) -
 * only events the *caller* of [SimpleMapView] needs go through this one, since [Event]'s
 * single-consumption semantics don't support two independent observers sharing one stream.
 */
sealed interface MapCallerEvent {
  data class LocationSelected(val markerState: MarkerState) : MapCallerEvent

  data object MapClicked : MapCallerEvent

  data class CustomButtonClicked(val id: Int) : MapCallerEvent
}
