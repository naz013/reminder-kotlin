package com.elementary.tasks.simplemap

import android.annotation.SuppressLint
import android.location.Address
import android.location.Criteria
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.adapter.place.UiPlaceListAdapter
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.GeocoderTask
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.PlaceRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimpleMapViewViewModel(
  val mapParams: MapParams,
  private val dispatcherProvider: DispatcherProvider,
  private val prefs: Prefs,
  private val geocoderTask: GeocoderTask,
  private val placeRepository: PlaceRepository,
  private val uiPlaceListAdapter: UiPlaceListAdapter,
  textProvider: TextProvider,
  private val systemServiceProvider: SystemServiceProvider,
  private val mapStyle: MapStyle,
) : ViewModel() {
  private val radiusRange = RadiusRangeState(prefs.radius)
  private val radiusFormatter = DefaultRadiusFormatter(textProvider, prefs.useMetric)
  private var hasSeededActiveMarker = false
  private var hasCenteredOnMarkers = false

  private val _state = MutableStateFlow(MapUiState())
  val state = _state.stateInWhileSubscribed(MapUiState())
    .onStart { _state.update { loadInitialState(it) } }

  val event: LiveData<Event<MapEvent>> field = mutableLiveEventOf()

  private var permissionMode = PermissionMode.General

  init {
    if (mapParams.isPlaces) {
      loadRecentPlaces()
    }
  }

  fun onMyLocationClick() {
    Logger.i(TAG, "On my location click")
    permissionMode = PermissionMode.MyLocation
    event.emit(MapEvent.RequestMyLocationPermission)
  }

  fun onLocationPermissionGranted() {
    Logger.i(TAG, "On location permission granted, mode: $permissionMode")
    _state.update { it.copy(hasLocationPermission = true) }
    when (permissionMode) {
      PermissionMode.MyLocation -> {
        resolveMyLocation()?.also {
          Logger.i(TAG, "Resolving my location")
          event.emit(MapEvent.ZoomToLocation(it, MapConfig.DEFAULT_ZOOM))
        }
      }

      PermissionMode.General -> {
        Logger.i(TAG, "General permission granted")
      }
    }
  }

  /**
   * Called once the underlying map view is ready. Requests location permission if it isn't
   * already known to be granted, and otherwise frames the camera on the device's current
   * location - unless a marker is already seeded, in which case [setSeedMarkers] owns framing.
   */
  fun onMapReady() {
    Logger.i(TAG, "On map ready")
    if (!_state.value.hasLocationPermission) {
      permissionMode = PermissionMode.General
      event.emit(MapEvent.RequestLocationPermission)
    }
  }

  /**
   * Feeds in the caller-supplied initial marker(s): seeds the single editable marker (touch mode,
   * only the first entry is used) or drives read-only multi-marker display, in both cases framing
   * the camera on the first marker exactly once.
   */
  fun setSeedMarkers(markers: List<MapMarker>) {
    if (mapParams.isTouch) {
      if (hasSeededActiveMarker) return
      val seed = markers.firstOrNull()?.let { toMarkerState(it) } ?: return
      hasSeededActiveMarker = true
      radiusRange.seedRadius(seed.radius)
      _state.update {
        it.copy(
          markers = listOf(seed),
          selectedMarkerStyle = seed.styleIndex,
          radiusMeters = radiusRange.radius,
          maxRadiusMeters = radiusRange.valueTo,
          radiusText = radiusFormatter.format(radiusRange.radius),
        )
      }
      event.emit(MapEvent.ZoomToLocation(seed.latLng, MapConfig.DEFAULT_ZOOM))
    } else {
      _state.update { it.copy(markers = markers.map { toMarkerState(it) }) }
      if (!hasCenteredOnMarkers) {
        val first = markers.firstOrNull() ?: return
        hasCenteredOnMarkers = true
        event.emit(MapEvent.ZoomToLocation(first.latLng, MapConfig.DEFAULT_ZOOM))
      }
    }
  }

  fun moveCamera(latLng: LatLng) {
    event.emit(MapEvent.ZoomToLocation(latLng, MapConfig.DEFAULT_ZOOM))
  }

  fun onMapClick(latLng: LatLng) {
    if (_state.value.activePicker != null) {
      _state.update { it.copy(activePicker = null) }
      return
    }
    Logger.v(TAG, "On map clicked with location: ${Logger.private(latLng.toString())}")
    if (mapParams.isTouch) {
      val title = geocoderTask.getAddressForLocation(latLng) ?: latLng.toString()
      placeMarker(
        updateMarkerStyle(
          MarkerState(latLng = latLng, radius = _state.value.radiusMeters, title = title),
          _state.value.selectedMarkerStyle,
        )
      )
    } else {
      event.emit(MapEvent.MapClicked)
    }
  }

  fun onAddressQueryChanged(query: String) {
    _state.update { it.copy(addressQuery = query) }
    geocoderTask.findAddresses(query) { found ->
      _state.update { it.copy(addressSuggestions = found) }
    }
  }

  fun onAddressSuggestionSelected(address: Address) {
    _state.update {
      it.copy(
        addressQuery = address.toDisplayTitle(),
        addressSuggestions = emptyList()
      )
    }
    val latLng = LatLng(address.latitude, address.longitude)
    placeMarker(
      updateMarkerStyle(
        MarkerState(
          latLng = latLng,
          radius = _state.value.radiusMeters,
          title = address.toDisplayTitle()
        ),
        _state.value.selectedMarkerStyle,
      ),
    )
  }

  fun dismissAddressSuggestions() {
    _state.update { it.copy(addressSuggestions = emptyList()) }
  }

  fun onLayersButtonClicked() {
    _state.update {
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
    _state.update { it.copy(activePicker = if (it.activePicker == picker) null else picker) }
  }

  fun onCustomButtonClicked(id: Int) {
    event.emit(MapEvent.CustomButtonClicked(id))
  }

  fun onMapTypeSelected(type: Int) {
    if (mapParams.rememberMapStyle) prefs.mapType = type
    _state.update {
      it.copy(
        selectedMapType = type,
        mapType = type.toComposeMapType(),
        activePicker = if (type == GoogleMap.MAP_TYPE_NORMAL) MapPicker.MAP_STYLE else null
      )
    }
  }

  fun onMapStyleSelected(style: Int) {
    if (mapParams.rememberMapStyle) prefs.mapStyle = style
    _state.update {
      it.copy(
        selectedMapStyle = style,
        mapStyleOptions = mapStyle.getMapStyleOptions(style, prefs.mapType),
        activePicker = null
      )
    }
  }

  fun onMarkerStyleSelected(style: Int) {
    if (style != _state.value.selectedMarkerStyle && prefs.hapticsEnabled) {
      event.emit(MapEvent.HapticFeedback)
    }
    if (mapParams.rememberMarkerStyle) prefs.markerStyle = style
    _state.update { it.copy(selectedMarkerStyle = style) }
    val current = _state.value.markers.firstOrNull() ?: return
    placeMarker(updateMarkerStyle(current, style))
  }

  fun onRadiusChanged(value: Float) {
    val previousRadius = radiusRange.radius
    radiusRange.updateValue(value)
    if (radiusRange.radius != previousRadius && prefs.hapticsEnabled) {
      event.emit(MapEvent.HapticFeedback)
    }
    if (mapParams.rememberMarkerRadius) prefs.radius = radiusRange.radius
    _state.update {
      it.copy(
        radiusMeters = radiusRange.radius,
        radiusText = radiusFormatter.format(radiusRange.radius)
      )
    }
    val current = _state.value.markers.firstOrNull() ?: return
    placeMarker(current.copy(radius = radiusRange.radius))
  }

  fun onRecentPlaceSelected(place: UiPlaceList) {
    val style = if (BuildParams.isPro) place.markerStyle else _state.value.selectedMarkerStyle
    _state.update { it.copy(selectedMarkerStyle = style, activePicker = null) }
    placeMarker(
      updateMarkerStyle(
        MarkerState(
          latLng = place.latLng,
          radius = _state.value.radiusMeters,
          title = place.name
        ),
        style,
      )
    )
  }

  /** Returns `true` if the caller should proceed with its own back-navigation. */
  fun onBackPressed(): Boolean =
    if (_state.value.activePicker != null) {
      _state.update { it.copy(activePicker = null) }
      false
    } else {
      true
    }

  private fun placeMarker(marker: MarkerState) {
    _state.update { it.copy(markers = listOf(marker)) }
    event.emit(MapEvent.ZoomToLocation(marker.latLng, MapConfig.DEFAULT_ZOOM))
    event.emit(MapEvent.LocationSelected(marker))
  }

  private fun loadRecentPlaces() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val places = placeRepository.getAll().map { uiPlaceListAdapter.convert(it) }
      withContext(dispatcherProvider.main()) {
        _state.update { it.copy(recentPlaces = places) }
      }
    }
  }

  @SuppressLint("MissingPermission")
  @Suppress("DEPRECATION")
  private fun resolveMyLocation(): LatLng? {
    val locationManager = systemServiceProvider.provideLocationManager()
    val criteria = Criteria()
    val fromLocationManager = runCatching {
      locationManager?.getLastKnownLocation(locationManager.getBestProvider(criteria, false) ?: "")
    }.getOrNull()
    if (fromLocationManager != null) {
      return LatLng(fromLocationManager.latitude, fromLocationManager.longitude)
    }
    return null
  }

  private fun loadInitialState(current: MapUiState): MapUiState {
    return current.copy(
      selectedMapType = prefs.mapType,
      selectedMapStyle = prefs.mapStyle,
      selectedMarkerStyle = if (BuildParams.isPro) {
        prefs.markerStyle
      } else {
        MapConfig.DEFAULT_MARKER_STYLE
      },
      radiusMeters = radiusRange.radius,
      maxRadiusMeters = radiusRange.valueTo,
      radiusText = radiusFormatter.format(radiusRange.radius),
      mapStyleOptions = mapStyle.getMapStyleOptions(prefs.mapStyle, prefs.mapType),
      markerStyleSliderColors = mapStyle.colorsForSlider(),
      hapticFeedbackEnabled = prefs.hapticsEnabled,
    )
  }

  private fun updateMarkerStyle(
    markerState: MarkerState,
    newStyleIndex: Int,
  ): MarkerState {
    val colors = mapStyle.getMarkerRadiusStyle(newStyleIndex)
    return markerState.copy(
      styleIndex = newStyleIndex,
      color = mapStyle.getMarkerColor(newStyleIndex),
      circleStrokeColor = colors.strokeColor,
      circleColor = colors.fillColor,
    )
  }

  private fun Int.toComposeMapType(): MapType = when (this) {
    GoogleMap.MAP_TYPE_SATELLITE -> MapType.SATELLITE
    GoogleMap.MAP_TYPE_TERRAIN -> MapType.TERRAIN
    GoogleMap.MAP_TYPE_HYBRID -> MapType.HYBRID
    GoogleMap.MAP_TYPE_NONE -> MapType.NONE
    else -> MapType.NORMAL
  }

  private fun toMarkerState(mapMarker: MapMarker): MarkerState {
    return updateMarkerStyle(
      MarkerState(
        latLng = mapMarker.latLng,
        title = mapMarker.title,
        radius = mapMarker.radius
      ),
      mapMarker.style,
    )
  }

  private enum class PermissionMode {
    MyLocation,
    General
  }

  sealed interface MapEvent {
    data object RequestLocationPermission : MapEvent

    data object RequestMyLocationPermission : MapEvent

    data class ZoomToLocation(
      val latLng: LatLng,
      val zoom: Float,
    ) : MapEvent

    data class LocationSelected(val markerState: MarkerState) : MapEvent

    data object MapClicked : MapEvent

    data class CustomButtonClicked(val id: Int) : MapEvent

    data object HapticFeedback : MapEvent
  }

  companion object {
    private const val TAG = "MapViewModel"
  }
}
