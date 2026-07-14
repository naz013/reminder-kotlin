package com.elementary.tasks.simplemap

import com.elementary.tasks.config.RadiusConfig

/**
 * Backs [MapViewModel]'s radius slider: keeps the slider's upper bound ([valueTo]) auto-expanding
 * as the value approaches either end, ported from the legacy `RadiusSliderBehaviour` View
 * behaviour (same 95%/10% thresholds and 20% growth/shrink step). [updateValue] and
 * [growRangeIfNeeded] are split rather than combined into one "on drag" call because growing
 * [valueTo] mid-gesture resets a Compose `Slider`'s drag tracking - see [MapViewModel.onRadiusChanged].
 */
class RadiusRangeState(
  initialRadius: Int,
) {
  var radius: Int = initialRadius.coerceAtLeast(0)
    private set
  var valueTo: Float = valueToFor(radius.toFloat())
    private set

  fun updateValue(value: Float) {
    radius = value.toInt()
  }

  fun growRangeIfNeeded() {
    val percent = radius / valueTo * 100f
    when {
      percent > 95f && valueTo < MAX_RADIUS -> valueTo += valueTo * 0.2f
      percent < 10f && valueTo > DEFAULT_VALUE_TO -> valueTo -= valueTo * 0.2f
    }
  }

  fun seedRadius(newRadius: Int) {
    radius = newRadius.coerceAtLeast(0)
    valueTo = valueToFor(radius.toFloat())
  }

  companion object {
    private val MAX_RADIUS = RadiusConfig.MAX_RADIUS.toFloat()
    private const val DEFAULT_VALUE_TO = 5000f

    private fun valueToFor(radius: Float): Float = if (radius <= 0f) DEFAULT_VALUE_TO else radius * 2f
  }
}
