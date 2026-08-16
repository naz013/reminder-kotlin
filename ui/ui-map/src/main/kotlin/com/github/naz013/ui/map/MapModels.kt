package com.github.naz013.ui.map

import android.location.Address
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.withAlpha
import com.google.android.gms.maps.model.LatLng

/**
 * Configuration for [SimpleMapView]. Initial marker style/radius/map type/style always come from
 * [MapPreferences] (matching the pre-Compose `SimpleMapFragment` behavior, where those values were
 * likewise never actually seedable by the caller - only `remember*` flags controlled whether picker
 * changes got persisted back to prefs).
 */
data class MapParams(
  val isTouch: Boolean = true,
  val isStyles: Boolean = true,
  val isPlaces: Boolean = true,
  val isSearch: Boolean = true,
  val isRadius: Boolean = true,
  val isLayers: Boolean = true,
  val rememberMapStyle: Boolean = true,
  val rememberMarkerRadius: Boolean = true,
  val rememberMarkerStyle: Boolean = true,
  val customButtons: List<MapCustomButton> = emptyList(),
)

data class MapCustomButton(
  @DrawableRes val icon: Int,
  val id: Int,
  val contentDescription: String? = null,
)

data class MarkerState(
  val latLng: LatLng,
  val radius: Int = MapConfig.Radius.DEFAULT_METERS,
  val title: String = "",
  val address: String = "",
  val styleIndex: Int = 0,
  val color: Color = Color.Red,
  val iconRes: Int = R.drawable.ic_fluent_place,
  val circleColor: Color = Color.Red.withAlpha(0.33f),
  val circleStrokeColor: Color = Color.Red,
)

internal fun Address.toDisplayTitle(): String = getAddressLine(0) ?: toShortDisplayName()

internal fun Address.toShortDisplayName(): String {
  val sb = StringBuilder()
  sb.append(featureName)
  adminArea?.let { sb.append(", ").append(it) }
  countryName?.let { sb.append(", ").append(it) }
  return sb.toString()
}
