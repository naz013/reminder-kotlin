package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.settings.location.LocationSettingsPreferences

class LocationSettingsPreferencesImpl(
  private val prefs: Prefs,
) : LocationSettingsPreferences {
  override var isDistanceNotificationEnabled: Boolean
    get() = prefs.isDistanceNotificationEnabled
    set(value) { prefs.isDistanceNotificationEnabled = value }

  override var radius: Int
    get() = prefs.radius
    set(value) { prefs.radius = value }

  override var mapType: Int
    get() = prefs.mapType
    set(value) { prefs.mapType = value }

  override var mapStyle: Int
    get() = prefs.mapStyle
    set(value) { prefs.mapStyle = value }

  override var markerStyle: Int
    get() = prefs.markerStyle
    set(value) { prefs.markerStyle = value }

  override var trackTime: Int
    get() = prefs.trackTime
    set(value) { prefs.trackTime = value }

  override val useMetric: Boolean
    get() = prefs.useMetric

  override val hapticsEnabled: Boolean
    get() = prefs.hapticsEnabled
}
