package com.elementary.tasks.settings.location

import androidx.compose.ui.graphics.Color

data class LocationSettingsState(
  val isNotificationChecked: Boolean = false,
  val radiusText: String = "",
  val mapTypeName: String = "",
  val isMapStyleRowEnabled: Boolean = false,
  val mapStylePreviewRes: Int = 0,
  val mapStyleName: String = "",
  val isMarkerStyleVisible: Boolean = false,
  val markerColor: Int = 0,
  val dialog: LocationSettingsDialog? = null,
  val hasLocation: Boolean = false,
)

sealed class LocationSettingsDialog {
  data class MapType(
    val options: List<String>,
    val selectedIndex: Int,
  ) : LocationSettingsDialog()

  data class Tracker(
    val seconds: Int,
  ) : LocationSettingsDialog()

  data class Radius(
    val value: Int,
    val valueTo: Float,
    val formattedValue: String,
  ) : LocationSettingsDialog()
}

sealed class LocationSettingsEvent {
  data object OpenMapStyle : LocationSettingsEvent()

  data object OpenPlaces : LocationSettingsEvent()

  data class ShowMarkerColorPicker(
    val title: String,
    val currentColorIndex: Int,
    val colors: List<Color>,
    val hapticFeedbackEnabled: Boolean,
  ) : LocationSettingsEvent()

  data object HapticFeedback : LocationSettingsEvent()
}
