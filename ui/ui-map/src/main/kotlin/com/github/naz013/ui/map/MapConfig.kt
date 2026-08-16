package com.github.naz013.ui.map

import com.google.android.gms.maps.GoogleMap

object MapConfig {
  const val DEFAULT_MARKER_STYLE = 5
  const val DEFAULT_MAP_STYLE = 6 // Auto
  const val DEFAULT_MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL
  const val DEFAULT_ZOOM = 13f

  object Radius {
    const val MAX_METERS = 10000
    const val MIN_METERS = 0
    const val DEFAULT_METERS = 50
  }
}
