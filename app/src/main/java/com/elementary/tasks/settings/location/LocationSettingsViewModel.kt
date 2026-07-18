package com.elementary.tasks.settings.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.simplemap.DefaultRadiusFormatter
import com.elementary.tasks.simplemap.MapConfig
import com.elementary.tasks.simplemap.MapStyle
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.SystemInfo
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.ui.common.theme.ThemeProvider
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

class LocationSettingsViewModel(
  private val prefs: Prefs,
  private val textProvider: TextProvider,
  private val themeProvider: ThemeProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val systemInfo: SystemInfo,
  private val mapStyle: MapStyle,
) : ViewModel() {

  private val _state = MutableStateFlow(buildState())
  val state = _state.stateInWhileSubscribed(buildState())
    .onStart { refreshState() }
  val navigationEvent: LiveData<Event<LocationSettingsEvent>> field = mutableLiveEventOf()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.LOCATION_SETTINGS))
  }

  fun onNotificationToggle() {
    prefs.isDistanceNotificationEnabled = !prefs.isDistanceNotificationEnabled
    refreshState()
  }

  fun onRadiusClick() {
    _state.update {
      it.copy(
        dialog =
          LocationSettingsDialog.Radius(
            value = prefs.radius,
            valueTo = MapConfig.Radius.MAX_METERS.toFloat(),
            formattedValue = formatRadius(prefs.radius),
          ),
      )
    }
  }

  fun onRadiusPreviewChange(value: Int) {
    val dialog = _state.value.dialog as? LocationSettingsDialog.Radius ?: return
    if (dialog.value != value && prefs.hapticsEnabled) {
      navigationEvent.value = Event(LocationSettingsEvent.HapticFeedback)
    }
    _state.update { current ->
      val currentDialog = current.dialog as? LocationSettingsDialog.Radius ?: return@update current
      current.copy(
        dialog = currentDialog.copy(
          value = value,
          formattedValue = formatRadius(value)
        )
      )
    }
  }

  fun onRadiusConfirm() {
    val dialog = _state.value.dialog as? LocationSettingsDialog.Radius ?: return
    prefs.radius = dialog.value
    dismissDialog()
  }

  fun onMapTypeClick() {
    val options = mapTypeOptions()
    _state.update {
      it.copy(
        dialog = LocationSettingsDialog.MapType(
          options = options,
          selectedIndex = mapTypePosition(prefs.mapType)
        )
      )
    }
  }

  fun onMapTypeOptionSelected(index: Int) {
    prefs.mapType = index + 1
    dismissDialog()
  }

  fun onMapStyleClick() {
    navigationEvent.value = Event(LocationSettingsEvent.OpenMapStyle)
  }

  fun onMarkerStyleClick() {
    navigationEvent.value = Event(
      LocationSettingsEvent.ShowMarkerColorPicker(
        title = textProvider.getString(R.string.style_of_marker),
        currentColorIndex = prefs.markerStyle,
        colors = mapStyle.colorsForSlider(),
        hapticFeedbackEnabled = prefs.hapticsEnabled,
      )
    )
  }

  fun onMarkerColorSelected(colorIndex: Int) {
    prefs.markerStyle = colorIndex
    refreshState()
  }

  fun onTrackerClick() {
    _state.update { it.copy(dialog = LocationSettingsDialog.Tracker(seconds = prefs.trackTime)) }
  }

  fun onTrackerPreviewChange(seconds: Int) {
    val dialog = _state.value.dialog as? LocationSettingsDialog.Tracker ?: return
    if (dialog.seconds != seconds && prefs.hapticsEnabled) {
      navigationEvent.value = Event(LocationSettingsEvent.HapticFeedback)
    }
    _state.update { current ->
      val currentDialog = current.dialog as? LocationSettingsDialog.Tracker ?: return@update current
      current.copy(dialog = currentDialog.copy(seconds = seconds))
    }
  }

  fun onTrackerConfirm() {
    val dialog = _state.value.dialog as? LocationSettingsDialog.Tracker ?: return
    prefs.trackTime = dialog.seconds
    dismissDialog()
  }

  fun onPlacesClick() {
    navigationEvent.value = Event(LocationSettingsEvent.OpenPlaces)
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  private fun dismissDialog() {
    _state.update { buildState().copy(dialog = null) }
  }

  private fun refreshState() {
    _state.update { buildState().copy(dialog = it.dialog) }
  }

  private fun buildState(): LocationSettingsState =
    LocationSettingsState(
      isNotificationChecked = prefs.isDistanceNotificationEnabled,
      radiusText = formatRadius(prefs.radius),
      mapTypeName = mapTypeOptions()[mapTypePosition(prefs.mapType)],
      isMapStyleRowEnabled = mapTypePosition(prefs.mapType) == 0,
      mapStylePreviewRes = themeProvider.mapStylePreview,
      mapStyleName = textProvider.getString(themeProvider.styleName),
      isMarkerStyleVisible = BuildParams.isPro,
      markerColor = themeProvider.getMarkerLightColor(prefs.markerStyle),
      hasLocation = systemInfo.hasLocation,
    )

  private fun formatRadius(meters: Int): String {
    val formatter = DefaultRadiusFormatter(textProvider, prefs.useMetric)
    return formatter.format(meters)
  }

  private fun mapTypeOptions(): List<String> =
    listOf(
      textProvider.getString(R.string.normal),
      textProvider.getString(R.string.satellite),
      textProvider.getString(R.string.terrain),
      textProvider.getString(R.string.hybrid),
    )

  private fun mapTypePosition(type: Int): Int =
    when (type) {
      GoogleMap.MAP_TYPE_SATELLITE -> 1
      GoogleMap.MAP_TYPE_TERRAIN -> 2
      GoogleMap.MAP_TYPE_HYBRID -> 3
      else -> 0
    }
}
