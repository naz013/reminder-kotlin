package com.elementary.tasks.simplemap

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.elementary.tasks.core.utils.BuildParams
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.common.Permissions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID
import com.google.maps.android.compose.GoogleMap as ComposeGoogleMap

private const val RAIL_ANIM_DURATION_MS = 300
private const val SEARCH_ANIM_DURATION_MS = 300
private const val SEARCH_ANIM_DELAY_MS = 80

/**
 * Compose replacement for the legacy `SimpleMapFragment`: a Google Map with an optional tap-to-set
 * marker + radius circle, and the layers/marker-style/radius/recent-places picker chrome. Every
 * state transition and business decision lives in its own internal [SimpleMapViewViewModel] - callers only
 * ever see [mapParams]/[markers] going in and the `onX` callbacks (plus [onControllerReady] for
 * the few things, like "move to this place" or "back button", that need to be driven
 * imperatively from outside).
 */
@Composable
fun SimpleMapView(
  modifier: Modifier = Modifier,
  mapParams: MapParams,
  markers: List<MapMarker> = emptyList(),
  onLocationSelected: (MarkerState) -> Unit = {},
  onMapClick: (() -> Unit)? = null,
  onCustomButtonClick: (Int) -> Unit = {},
  onControllerReady: (SimpleMapController) -> Unit = {},
  edgeToEdge: Boolean = false,
) {
  val viewModelKey = rememberSaveable { UUID.randomUUID().toString() }
  val viewModel = koinViewModel<SimpleMapViewViewModel>(key = viewModelKey) { parametersOf(mapParams) }

  val controller = remember(viewModel) { SimpleMapController(viewModel) }
  LaunchedEffect(controller) { onControllerReady(controller) }

  val permissionRequester = rememberPermissionRequesterRationale()
  val coroutineScope = rememberCoroutineScope()
  val cameraPositionState = rememberCameraPositionState()
  val hapticFeedback = LocalHapticFeedback.current
  var rawMap by remember { mutableStateOf<GoogleMap?>(null) }
  var controlsVisible by rememberSaveable { mutableStateOf(false) }

  val uiState by viewModel.state.collectAsState(MapUiState())

  LaunchedEffect(markers) { viewModel.setSeedMarkers(markers) }

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is SimpleMapViewViewModel.MapEvent.RequestMyLocationPermission -> {
        permissionRequester.request(
          listOf(Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION),
          onGranted = { viewModel.onLocationPermissionGranted() },
        )
      }

      is SimpleMapViewViewModel.MapEvent.ZoomToLocation -> {
        coroutineScope.launch {
          cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(event.latLng, event.zoom))
        }
      }

      is SimpleMapViewViewModel.MapEvent.RequestLocationPermission -> {
        permissionRequester.request(
          listOf(Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION),
          onGranted = { viewModel.onLocationPermissionGranted() },
        )
      }

      is SimpleMapViewViewModel.MapEvent.CustomButtonClicked -> {
        onCustomButtonClick(event.id)
      }

      is SimpleMapViewViewModel.MapEvent.LocationSelected -> {
        onLocationSelected(event.markerState)
      }

      is SimpleMapViewViewModel.MapEvent.MapClicked -> {
        onMapClick?.invoke()
      }

      is SimpleMapViewViewModel.MapEvent.HapticFeedback -> {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      }
    }
  }

  val mapProperties = remember(uiState.mapType, uiState.mapStyleOptions, uiState.hasLocationPermission) {
    MapProperties(
      mapType = uiState.mapType,
      mapStyleOptions = uiState.mapStyleOptions,
      isMyLocationEnabled = uiState.hasLocationPermission,
    )
  }
  val mapUiSettings = remember {
    MapUiSettings(myLocationButtonEnabled = false, compassEnabled = false, mapToolbarEnabled = false)
  }

  Box(modifier = modifier.fillMaxSize()) {
    ComposeGoogleMap(
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState,
      properties = mapProperties,
      uiSettings = mapUiSettings,
      onMapLoaded = {
        controlsVisible = true
        viewModel.onMapReady()
      },
      onMapClick = { viewModel.onMapClick(it) },
    ) {
      MapEffect(Unit) { map -> rawMap = map }
      uiState.markers.forEach { marker ->
        MapMarkerAndCircle(markerState = marker)
      }
    }

    Row(
      modifier = Modifier
        .fillMaxSize()
        .then(if (edgeToEdge) Modifier.statusBarsPadding() else Modifier),
    ) {
      AnimatedVisibility(
        visible = controlsVisible,
        enter = fadeIn(tween(RAIL_ANIM_DURATION_MS)) +
          slideInHorizontally(tween(RAIL_ANIM_DURATION_MS)) { -it },
      ) {
        Column(modifier = Modifier.width(56.dp + 16.dp).padding(horizontal = 8.dp, vertical = 8.dp)) {
          mapParams.customButtons.forEach { button ->
            MapRailButton(
              icon = button.icon,
              onClick = { viewModel.onCustomButtonClicked(button.id) },
              modifier = Modifier.padding(bottom = 8.dp),
            )
          }
          if (mapParams.isLayers) {
            MapRailButton(
              icon = R.drawable.ic_builder_map_layers,
              onClick = viewModel::onLayersButtonClicked,
              modifier = Modifier.padding(bottom = 8.dp),
            )
          }
          if (mapParams.isStyles && BuildParams.isPro) {
            MapRailButton(
              icon = R.drawable.ic_fluent_style_guide,
              onClick = viewModel::onMarkerStyleButtonClicked,
              modifier = Modifier.padding(bottom = 8.dp),
            )
          }
          if (mapParams.isRadius) {
            MapRailButton(
              icon = R.drawable.ic_builder_map_radius,
              onClick = viewModel::onRadiusButtonClicked,
              modifier = Modifier.padding(bottom = 8.dp),
            )
          }
          if (mapParams.isPlaces && uiState.recentPlaces.isNotEmpty()) {
            MapRailButton(
              icon = R.drawable.ic_builder_map_history,
              onClick = viewModel::onPlacesButtonClicked,
              modifier = Modifier.padding(bottom = 8.dp),
            )
          }
        }
      }

      Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 8.dp)) {
        if (mapParams.isSearch) {
          AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(SEARCH_ANIM_DURATION_MS, delayMillis = SEARCH_ANIM_DELAY_MS)) +
              slideInVertically(tween(SEARCH_ANIM_DURATION_MS, delayMillis = SEARCH_ANIM_DELAY_MS)) { -it },
          ) {
            Row(modifier = Modifier.fillMaxWidth()) {
              AddressSearchField(
                query = uiState.addressQuery,
                suggestions = uiState.addressSuggestions,
                onQueryChange = viewModel::onAddressQueryChanged,
                onSuggestionSelected = viewModel::onAddressSuggestionSelected,
                onDismissSuggestions = viewModel::dismissAddressSuggestions,
                modifier = Modifier.weight(1f),
              )
              MyLocationButton(onClick = viewModel::onMyLocationClick)
            }
          }
        }
        when (uiState.activePicker) {
          MapPicker.LAYERS -> LayerTypeCard(
            onTypeSelected = viewModel::onMapTypeSelected,
            modifier = Modifier.padding(top = 8.dp),
          )

          MapPicker.MAP_STYLE -> MapStyleCard(
            onStyleSelected = viewModel::onMapStyleSelected,
            modifier = Modifier.padding(top = 8.dp),
          )

          MapPicker.MARKER_STYLE -> {
            MarkerStyleCard(
              colors = uiState.markerStyleSliderColors,
              selectedIndex = uiState.selectedMarkerStyle,
              onStyleSelected = viewModel::onMarkerStyleSelected,
              modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
              hapticFeedbackEnabled = uiState.hapticFeedbackEnabled,
            )
          }

          MapPicker.RADIUS -> MarkerRadiusCard(
            radius = uiState.radiusMeters,
            valueTo = uiState.maxRadiusMeters,
            formattedRadius = uiState.radiusText,
            onValueChange = viewModel::onRadiusChanged,
            onValueChangeFinished = { },
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
          )

          MapPicker.PLACES -> RecentPlacesCard(
            places = uiState.recentPlaces,
            onPlaceSelected = viewModel::onRecentPlaceSelected,
            modifier = Modifier.padding(top = 8.dp),
          )

          null -> Unit
        }
      }
    }
  }
}

@Composable
@GoogleMapComposable
private fun MapMarkerAndCircle(markerState: MarkerState) {
  val gmsMarkerState = remember(markerState.latLng) {
    com.google.maps.android.compose.MarkerState(position = markerState.latLng)
  }
  MarkerComposable(
    markerState.iconRes,
    markerState.color,
    state = gmsMarkerState,
    title = markerState.title,
  ) {
    Icon(
      painter = painterResource(markerState.iconRes),
      contentDescription = null,
      tint = markerState.color,
    )
  }
  Circle(
    center = markerState.latLng,
    radius = markerState.radius.toDouble(),
    strokeWidth = 3f,
    fillColor = markerState.circleColor,
    strokeColor = markerState.circleStrokeColor,
  )
}

@Composable
private fun MapRailButton(
  @DrawableRes icon: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    modifier = modifier.size(56.dp),
    shape = RoundedCornerShape(5.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    tonalElevation = 1.dp,
    shadowElevation = 1.dp,
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
      )
    }
  }
}

@Composable
private fun MyLocationButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    modifier = modifier.size(56.dp),
    color = MaterialTheme.colorScheme.secondary,
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        painter = painterResource(R.drawable.ic_builder_map_my_location),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSecondary,
      )
    }
  }
}
