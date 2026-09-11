package com.github.naz013.ui.map

class RadiusRangeState(
  initialRadius: Int,
) {
  var radius: Int = initialRadius.coerceAtLeast(MIN_RADIUS)
    private set
  var valueTo: Float = MAX_RADIUS
    private set

  fun updateValue(value: Float) {
    radius = value.toInt().coerceAtLeast(MIN_RADIUS)
  }

  fun seedRadius(newRadius: Int) {
    radius = newRadius.coerceAtLeast(MIN_RADIUS)
  }

  companion object {
    private const val MIN_RADIUS = MapConfig.Radius.MIN_METERS
    private const val MAX_RADIUS = MapConfig.Radius.MAX_METERS.toFloat()
    private const val DEFAULT_VALUE_TO = 5000f
  }
}
