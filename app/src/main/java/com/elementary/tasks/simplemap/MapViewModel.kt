package com.elementary.tasks.simplemap

import android.location.Address
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.config.MapConfig
import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.GeocoderTask
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.radius.DefaultRadiusFormatter
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.repository.PlaceRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns every state transition and business decision for [SimpleMapView]: which picker is open,
 * the active/seeded marker(s), map type/style and marker style/radius selection (with prefs
 * persistence), reverse/forward geocoding, and the recent-places list. [SimpleMapView] only
 * renders [state] and forwards user gestures here.
 *
 * Three narrow exceptions stay in [SimpleMapView] itself, since they need APIs a ViewModel can't
 * hold: camera animation, which needs a Compose-scoped `CameraPositionState` ([cameraEvent]);
 * requesting the location permission, which needs an `Activity`/launcher
 * ([permissionRequestEvent]); and resolving "my location" itself, which needs a raw `GoogleMap`
 * reference as a fallback when `LocationManager`'s cache is empty ([myLocationRequestEvent]).
 */
class MapViewModel(
  val mapParams: MapParams,
  private val dispatcherProvider: DispatcherProvider,
  private val prefs: Prefs,
  private val geocoderTask: GeocoderTask,
  private val placeRepository: PlaceRepository,
  private val uiPlaceListAdapter: UiPlaceListAdapter,
  textProvider: TextProvider,
) : ViewModel() {
  private val radiusRange = RadiusRangeState(prefs.radius)
  private val radiusFormatter = DefaultRadiusFormatter(textProvider, prefs.useMetric)
  private var hasSeededActiveMarker = false
  private var hasCenteredOnMarkers = false

  val state: StateFlow<MapUiState> field = MutableStateFlow(
    MapUiState(
      mapType = prefs.mapType,
      mapStyle = prefs.mapStyle,
      pendingStyle = if (BuildParams.isPro) prefs.markerStyle else MapConfig.DEFAULT_MARKER_STYLE,
      radius = radiusRange.radius,
      radiusValueTo = radiusRange.valueTo,
      radiusText = radiusFormatter.format(radiusRange.radius),
    ),
  )

  /** Consumed only by [SimpleMapView] to animate its `CameraPositionState`. */
  val cameraEvent: LiveData<Event<LatLng>> field = mutableLiveEventOf()

  /** Consumed only by [SimpleMapView]; payload is whether to move the camera once granted. */
  val permissionRequestEvent: LiveData<Event<Boolean>> field = mutableLiveEventOf()

  /** Consumed only by [SimpleMapView], which resolves the device's current location and animates. */
  val myLocationRequestEvent: LiveData<Event<Unit>> field = mutableLiveEventOf()

  /** Consumed only by the screen embedding [SimpleMapView]. */
  val callerEvent: LiveData<Event<MapCallerEvent>> field = mutableLiveEventOf()

  init {
    if (mapParams.isPlaces) loadRecentPlaces()
  }

  /**
   * Called once the underlying map view is ready. Requests location permission if it isn't
   * already known to be granted, and otherwise frames the camera on the device's current
   * location - unless a marker is already seeded, in which case [setSeedMarkers] owns framing.
   */
  fun onMapReady() {
    if (!state.value.hasLocationPermission) {
      permissionRequestEvent.value = Event(false)
    } else if (state.value.markers.isEmpty()) {
      myLocationRequestEvent.value = Event(Unit)
    }
  }

  fun onLocationPermissionGranted(moveToMyLocation: Boolean) {
    state.update { it.copy(hasLocationPermission = true) }
    if (moveToMyLocation) myLocationRequestEvent.value = Event(Unit)
  }

  fun onMyLocationClick() {
    if (state.value.hasLocationPermission) {
      myLocationRequestEvent.value = Event(Unit)
    } else {
      permissionRequestEvent.value = Event(true)
    }
  }

  /**
   * Feeds in the caller-supplied initial marker(s): seeds the single editable marker (touch mode,
   * only the first entry is used) or drives read-only multi-marker display, in both cases framing
   * the camera on the first marker exactly once.
   */
  fun setSeedMarkers(markers: List<MarkerState>) {
    if (mapParams.isTouch) {
      if (hasSeededActiveMarker) return
      val seed = markers.firstOrNull() ?: return
      hasSeededActiveMarker = true
      radiusRange.seedRadius(seed.radius)
      state.update {
        it.copy(
          markers = listOf(seed),
          pendingStyle = seed.style,
          radius = radiusRange.radius,
          radiusValueTo = radiusRange.valueTo,
          radiusText = radiusFormatter.format(radiusRange.radius),
        )
      }
      cameraEvent.value = Event(seed.latLng)
    } else {
      state.update { it.copy(markers = markers) }
      if (!hasCenteredOnMarkers) {
        val first = markers.firstOrNull() ?: return
        hasCenteredOnMarkers = true
        cameraEvent.value = Event(first.latLng)
      }
    }
  }

  fun moveCamera(latLng: LatLng) {
    cameraEvent.value = Event(latLng)
  }

  fun onMapClick(latLng: LatLng) {
    if (state.value.activePicker != null) {
      state.update { it.copy(activePicker = null) }
      return
    }
    if (mapParams.isTouch) {
      val title = geocoderTask.getAddressForLocation(latLng) ?: latLng.toString()
      placeMarker(MarkerState(latLng = latLng, style = state.value.pendingStyle, radius = state.value.radius, title = title))
    } else {
      callerEvent.value = Event(MapCallerEvent.MapClicked)
    }
  }

  fun onAddressQueryChanged(query: String) {
    state.update { it.copy(addressQuery = query) }
    geocoderTask.findAddresses(query) { found ->
      state.update { it.copy(addressSuggestions = found) }
    }
  }

  fun onAddressSuggestionSelected(address: Address) {
    state.update { it.copy(addressQuery = address.toDisplayTitle(), addressSuggestions = emptyList()) }
    val latLng = LatLng(address.latitude, address.longitude)
    placeMarker(
      MarkerState(latLng = latLng, style = state.value.pendingStyle, radius = state.value.radius, title = address.toDisplayTitle()),
    )
  }

  fun dismissAddressSuggestions() {
    state.update { it.copy(addressSuggestions = emptyList()) }
  }

  fun onLayersButtonClicked() {
    state.update {
      it.copy(
        activePicker = when (it.activePicker) {
          MapPicker.LAYERS, MapPicker.MAP_STYLE -> null
          else -> MapPicker.LAYERS
        },
      )
    }
  }

  fun onMarkerStyleButtonClicked() = togglePicker(MapPicker.MARKER_STYLE)

  fun onRadiusButtonClicked() = togglePicker(MapPicker.RADIUS)

  fun onPlacesButtonClicked() = togglePicker(MapPicker.PLACES)

  private fun togglePicker(picker: MapPicker) {
    state.update { it.copy(activePicker = if (it.activePicker == picker) null else picker) }
  }

  fun onCustomButtonClicked(id: Int) {
    callerEvent.value = Event(MapCallerEvent.CustomButtonClicked(id))
  }

  fun onMapTypeSelected(type: Int) {
    if (mapParams.rememberMapStyle) prefs.mapType = type
    state.update {
      it.copy(mapType = type, activePicker = if (type == GoogleMap.MAP_TYPE_NORMAL) MapPicker.MAP_STYLE else null)
    }
  }

  fun onMapStyleSelected(style: Int) {
    if (mapParams.rememberMapStyle) prefs.mapStyle = style
    state.update { it.copy(mapStyle = style, activePicker = null) }
  }

  fun onMarkerStyleSelected(style: Int) {
    if (mapParams.rememberMarkerStyle) prefs.markerStyle = style
    state.update { it.copy(pendingStyle = style) }
    val current = state.value.markers.firstOrNull() ?: return
    placeMarker(current.copy(style = style))
  }

  /**
   * Called continuously while dragging. Deliberately does *not* touch [RadiusRangeState.valueTo]
   * - changing a Compose `Slider`'s `valueRange` mid-gesture resets its internal drag tracking,
   * which drops the pointer and forces the user to lift and re-drag. Range growth is deferred to
   * [onRadiusChangeFinished] instead.
   */
  fun onRadiusChanged(value: Float) {
    radiusRange.updateValue(value)
    if (mapParams.rememberMarkerRadius) prefs.radius = radiusRange.radius
    state.update {
      it.copy(radius = radiusRange.radius, radiusText = radiusFormatter.format(radiusRange.radius))
    }
    val current = state.value.markers.firstOrNull() ?: return
    placeMarker(current.copy(radius = radiusRange.radius))
  }

  /** Called once the drag gesture ends - grows/shrinks the slider's range for the next drag. */
  fun onRadiusChangeFinished() {
    radiusRange.growRangeIfNeeded()
    state.update { it.copy(radiusValueTo = radiusRange.valueTo) }
  }

  fun onRecentPlaceSelected(place: UiPlaceList) {
    val style = if (BuildParams.isPro) place.markerStyle else state.value.pendingStyle
    state.update { it.copy(pendingStyle = style, activePicker = null) }
    placeMarker(MarkerState(latLng = place.latLng, style = style, radius = state.value.radius, title = place.name))
  }

  /** Returns `true` if the caller should proceed with its own back-navigation. */
  fun onBackPressed(): Boolean =
    if (state.value.activePicker != null) {
      state.update { it.copy(activePicker = null) }
      false
    } else {
      true
    }

  private fun placeMarker(marker: MarkerState) {
    state.update { it.copy(markers = listOf(marker)) }
    cameraEvent.value = Event(marker.latLng)
    callerEvent.value = Event(MapCallerEvent.LocationSelected(marker))
  }

  private fun loadRecentPlaces() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val places = placeRepository.getAll().map { uiPlaceListAdapter.convert(it) }
      state.update { it.copy(recentPlaces = places) }
    }
  }
}
