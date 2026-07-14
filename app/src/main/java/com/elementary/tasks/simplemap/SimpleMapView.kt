package com.elementary.tasks.simplemap

import android.content.Context
import android.graphics.drawable.Drawable
import android.location.Criteria
import androidx.annotation.DrawableRes
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.elementary.tasks.core.os.compose.rememberPermissionRequesterRationale
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.io.BitmapUtils
import com.elementary.tasks.core.utils.ui.DrawableHelper
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.common.Permissions
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.ui.common.theme.ThemeProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap as ComposeGoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Compose replacement for the legacy `SimpleMapFragment`: a Google Map with an optional tap-to-set
 * marker + radius circle, and the layers/marker-style/radius/recent-places picker chrome. Purely a
 * renderer over [viewModel] - every state transition and business decision lives there; this
 * composable's own state is limited to the two things that must stay Compose-side: the
 * `CameraPositionState` driving map animation, and the location-permission request flow (which
 * needs an `Activity`/launcher [viewModel] can't hold).
 */
@Composable
fun SimpleMapView(
  viewModel: MapViewModel,
  markers: List<MarkerState> = emptyList(),
  edgeToEdge: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val themeProvider = koinInject<ThemeProvider>()
  val systemServiceProvider = koinInject<SystemServiceProvider>()
  val permissionRequester = rememberPermissionRequesterRationale()
  val coroutineScope = rememberCoroutineScope()
  val cameraPositionState = rememberCameraPositionState()
  var rawMap by remember { mutableStateOf<GoogleMap?>(null) }

  val uiState by viewModel.state.collectAsState()
  val mapParams = viewModel.mapParams

  LaunchedEffect(markers) { viewModel.setSeedMarkers(markers) }

  viewModel.cameraEvent.ObserveEvent { latLng ->
    coroutineScope.launch {
      cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
    }
  }
  viewModel.permissionRequestEvent.ObserveEvent { moveToMyLocationAfterGrant ->
    permissionRequester.request(
      listOf(Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION),
      onGranted = { viewModel.onLocationPermissionGranted(moveToMyLocationAfterGrant) },
    )
  }
  viewModel.myLocationRequestEvent.ObserveEvent {
    resolveMyLocation(systemServiceProvider, rawMap)?.let { latLng ->
      coroutineScope.launch {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
      }
    }
  }

  val mapStyleOptions = remember(uiState.mapType, uiState.mapStyle) {
    if (uiState.mapType == GoogleMap.MAP_TYPE_NORMAL) {
      MapStyleOptions.loadRawResourceStyle(context, themeProvider.getMapStyleJson(uiState.mapStyle))
    } else {
      null
    }
  }
  val mapProperties = remember(uiState.mapType, mapStyleOptions, uiState.hasLocationPermission) {
    MapProperties(
      mapType = uiState.mapType.toComposeMapType(),
      mapStyleOptions = mapStyleOptions,
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
      onMapLoaded = viewModel::onMapReady,
      onMapClick = { viewModel.onMapClick(it) },
    ) {
      MapEffect(Unit) { map -> rawMap = map }
      uiState.markers.forEach { marker ->
        MapMarkerAndCircle(markerState = marker, themeProvider = themeProvider)
      }
    }

    Row(
      modifier = Modifier
        .fillMaxSize()
        .then(if (edgeToEdge) Modifier.statusBarsPadding() else Modifier),
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

      Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 8.dp)) {
        if (mapParams.isSearch) {
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
            val colors = remember {
              ThemeProvider.colorsForSlider(context).map { argb -> Color(argb) }
            }
            MarkerStyleCard(
              colors = colors,
              selectedIndex = uiState.pendingStyle,
              selectorColor = colorResource(themeProvider.pickColorRes(R.color.pureBlack, R.color.pureWhite)),
              onStyleSelected = viewModel::onMarkerStyleSelected,
              modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
            )
          }

          MapPicker.RADIUS -> MarkerRadiusCard(
            radius = uiState.radius,
            valueTo = uiState.radiusValueTo,
            formattedRadius = uiState.radiusText,
            onValueChange = viewModel::onRadiusChanged,
            onValueChangeFinished = viewModel::onRadiusChangeFinished,
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
private fun MapMarkerAndCircle(
  markerState: MarkerState,
  themeProvider: ThemeProvider,
) {
  val context = LocalContext.current
  val icon = remember(markerState.style) { createMarkerIcon(context, themeProvider, markerState.style) }
  val gmsMarkerState = remember(markerState.latLng) {
    com.google.maps.android.compose.MarkerState(position = markerState.latLng)
  }
  Marker(state = gmsMarkerState, title = markerState.title, icon = icon)

  val radiusStyle = themeProvider.getMarkerRadiusStyle(markerState.style)
  Circle(
    center = markerState.latLng,
    radius = markerState.radius.toDouble(),
    strokeWidth = 3f,
    fillColor = colorResource(radiusStyle.fillColor),
    strokeColor = colorResource(radiusStyle.strokeColor),
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

/**
 * Prefers `LocationManager`'s last known location, falling back to the raw `GoogleMap`'s own
 * my-location layer when that cache is empty (common on emulators / devices where the fused
 * provider hasn't populated `LocationManager` yet, but the map's blue dot already has a fix).
 */
@Suppress("DEPRECATION")
private fun resolveMyLocation(
  systemServiceProvider: SystemServiceProvider,
  map: GoogleMap?,
): LatLng? {
  val locationManager = systemServiceProvider.provideLocationManager()
  val criteria = Criteria()
  val fromLocationManager = runCatching {
    locationManager?.getLastKnownLocation(locationManager.getBestProvider(criteria, false) ?: "")
  }.getOrNull()
  if (fromLocationManager != null) {
    return LatLng(fromLocationManager.latitude, fromLocationManager.longitude)
  }
  return runCatching { map?.myLocation }.getOrNull()?.let { LatLng(it.latitude, it.longitude) }
}

private fun createMarkerIcon(
  context: Context,
  themeProvider: ThemeProvider,
  style: Int,
): BitmapDescriptor {
  val drawable: Drawable = DrawableHelper
    .withContext(context)
    .withDrawable(R.drawable.ic_fluent_place)
    .withColor(themeProvider.getMarkerLightColor(style))
    .tint()
    .get()
  return BitmapUtils.getDescriptor(drawable)
}

private fun Int.toComposeMapType(): MapType = when (this) {
  GoogleMap.MAP_TYPE_SATELLITE -> MapType.SATELLITE
  GoogleMap.MAP_TYPE_TERRAIN -> MapType.TERRAIN
  GoogleMap.MAP_TYPE_HYBRID -> MapType.HYBRID
  GoogleMap.MAP_TYPE_NONE -> MapType.NONE
  else -> MapType.NORMAL
}
