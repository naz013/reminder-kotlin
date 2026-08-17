package com.elementary.tasks.module.uimap

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.ui.map.MapPreferences

class MapPreferencesImpl(
  private val prefs: Prefs,
) : MapPreferences {
  override var mapType: Int
    get() = prefs.mapType
    set(value) { prefs.mapType = value }

  override var mapStyle: Int
    get() = prefs.mapStyle
    set(value) { prefs.mapStyle = value }

  override var radius: Int
    get() = prefs.radius
    set(value) { prefs.radius = value }

  override var markerStyle: Int
    get() = prefs.markerStyle
    set(value) { prefs.markerStyle = value }

  override val hapticsEnabled: Boolean
    get() = prefs.hapticsEnabled

  override val useMetric: Boolean
    get() = prefs.useMetric
}
