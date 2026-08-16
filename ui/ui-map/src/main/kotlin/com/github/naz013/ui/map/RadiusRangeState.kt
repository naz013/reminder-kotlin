package com.github.naz013.ui.map

class RadiusRangeState(
  initialRadius: Int,
) {
  var radius: Int = initialRadius.coerceAtLeast(0)
    private set
  var valueTo: Float = MAX_RADIUS
    private set

  fun updateValue(value: Float) {
    radius = value.toInt()
  }

  fun seedRadius(newRadius: Int) {
    radius = newRadius.coerceAtLeast(0)
  }

  companion object {
    private const val MAX_RADIUS = MapConfig.Radius.MAX_METERS.toFloat()
    private const val DEFAULT_VALUE_TO = 5000f
  }
}
