package com.elementary.tasks.settings.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.config.RadiusConfig
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.radius.DefaultRadiusFormatter
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.ui.common.theme.ThemeProvider
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class LocationSettingsViewModel(
  private val prefs: Prefs,
  private val textProvider: TextProvider,
  private val themeProvider: ThemeProvider,
) : ViewModel() {

  val state: StateFlow<LocationSettingsState> field = MutableStateFlow(buildState())
  val navigationEvent: LiveData<Event<LocationSettingsEvent>> field = mutableLiveEventOf()

  fun onNotificationToggle() {
    prefs.isDistanceNotificationEnabled = !prefs.isDistanceNotificationEnabled
    refreshState()
  }

  fun onRadiusClick() {
    val radius = prefs.radius
    val valueTo = initialValueTo(radius.toFloat())
    state.update {
      it.copy(
        dialog = LocationSettingsDialog.Radius(
          value = radius,
          valueTo = valueTo,
          formattedValue = formatRadius(radius),
        ),
      )
    }
  }

  fun onRadiusPreviewChange(value: Int) {
    state.update { current ->
      val dialog = current.dialog as? LocationSettingsDialog.Radius ?: return@update current
      var valueTo = dialog.valueTo
      val percent = value / valueTo * 100f
      if (percent > 95f && valueTo < MAX_RADIUS) {
        valueTo += valueTo * 0.2f
      } else if (percent < 10f && valueTo.toInt() > 5000) {
        valueTo -= valueTo * 0.2f
      }
      current.copy(dialog = dialog.copy(value = value, valueTo = valueTo, formattedValue = formatRadius(value)))
    }
  }

  fun onRadiusConfirm() {
    val dialog = state.value.dialog as? LocationSettingsDialog.Radius ?: return
    prefs.radius = dialog.value
    dismissDialog()
  }

  fun onMapTypeClick() {
    val options = mapTypeOptions()
    state.update {
      it.copy(dialog = LocationSettingsDialog.MapType(options = options, selectedIndex = mapTypePosition(prefs.mapType)))
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
    navigationEvent.value = Event(LocationSettingsEvent.ShowMarkerColorPicker(prefs.markerStyle))
  }

  fun onMarkerColorSelected(colorIndex: Int) {
    prefs.markerStyle = colorIndex
    refreshState()
  }

  fun onTrackerClick() {
    state.update { it.copy(dialog = LocationSettingsDialog.Tracker(seconds = prefs.trackTime)) }
  }

  fun onTrackerPreviewChange(seconds: Int) {
    state.update { current ->
      val dialog = current.dialog as? LocationSettingsDialog.Tracker ?: return@update current
      current.copy(dialog = dialog.copy(seconds = seconds))
    }
  }

  fun onTrackerConfirm() {
    val dialog = state.value.dialog as? LocationSettingsDialog.Tracker ?: return
    prefs.trackTime = dialog.seconds
    dismissDialog()
  }

  fun onPlacesClick() {
    navigationEvent.value = Event(LocationSettingsEvent.OpenPlaces)
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  /** Called on every resume - the marker color and map-style preview can be changed by sub-screens
   *  (color dialog, [MapStyleFragment]) that this ViewModel doesn't observe directly. */
  fun onResume() {
    refreshState()
  }

  private fun dismissDialog() {
    state.update { buildState().copy(dialog = null) }
  }

  private fun refreshState() {
    state.update { buildState().copy(dialog = it.dialog) }
  }

  private fun buildState(): LocationSettingsState = LocationSettingsState(
    isNotificationChecked = prefs.isDistanceNotificationEnabled,
    radiusText = formatRadius(prefs.radius),
    mapTypeName = mapTypeOptions()[mapTypePosition(prefs.mapType)],
    isMapStyleRowEnabled = mapTypePosition(prefs.mapType) == 0,
    mapStylePreviewRes = themeProvider.mapStylePreview,
    mapStyleName = textProvider.getString(themeProvider.styleName),
    isMarkerStyleVisible = BuildParams.isPro,
    markerColor = themeProvider.getMarkerLightColor(prefs.markerStyle),
  )

  private fun formatRadius(meters: Int): String {
    val formatter = DefaultRadiusFormatter(textProvider, prefs.useMetric)
    return formatter.format(meters)
  }

  private fun mapTypeOptions(): List<String> = listOf(
    textProvider.getString(R.string.normal),
    textProvider.getString(R.string.satellite),
    textProvider.getString(R.string.terrain),
    textProvider.getString(R.string.hybrid),
  )

  private fun mapTypePosition(type: Int): Int = when (type) {
    GoogleMap.MAP_TYPE_SATELLITE -> 1
    GoogleMap.MAP_TYPE_TERRAIN -> 2
    GoogleMap.MAP_TYPE_HYBRID -> 3
    else -> 0
  }

  private fun initialValueTo(radius: Float): Float {
    var valueTo = MAX_DEF_RADIUS
    if (valueTo < radius && valueTo < MAX_RADIUS) {
      valueTo = radius + (valueTo * 0.2f)
    }
    if (radius > MAX_RADIUS) {
      valueTo = MAX_RADIUS
    }
    valueTo = radius * 2f
    if (radius == 0f) {
      valueTo = MAX_DEF_RADIUS
    }
    return valueTo
  }

  companion object {
    private val MAX_RADIUS = RadiusConfig.MAX_RADIUS.toFloat()
    private const val MAX_DEF_RADIUS = 5000f
  }
}
