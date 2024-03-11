package com.elementary.tasks.core.utils.ui.radius

import com.elementary.tasks.config.RadiusConfig
import com.google.android.material.slider.Slider

class RadiusSliderBehaviour(
  private val slider: Slider,
  initValue: Int,
  private val onValueChanged: (value: Int) -> Unit
) {

  init {
    slider.addOnChangeListener { _, value, _ ->
      onValueChanged.invoke(value.toInt())
      val perc = value / slider.valueTo * 100f
      if (perc > 95f && slider.valueTo < MAX_RADIUS) {
        slider.valueTo += (slider.valueTo * 0.2f)
      } else if (perc < 10f && slider.valueTo.toInt() > 5000) {
        slider.valueTo -= (slider.valueTo * 0.2f)
      }
    }
    slider.stepSize = 0f
    slider.valueFrom = 0f
    slider.valueTo = MAX_DEF_RADIUS
    updateSlider(initValue.toFloat())
  }

  fun setRadius(radius: Int) {
    updateSlider(radius.toFloat())
  }

  fun getRadius(): Int {
    return slider.value.toInt()
  }

  private fun updateSlider(radius: Float) {
    if (slider.valueTo < radius && slider.valueTo < MAX_RADIUS) {
      slider.valueTo = radius + (slider.valueTo * 0.2f)
    }
    if (radius > MAX_RADIUS) {
      slider.valueTo = MAX_RADIUS
    }
    slider.valueTo = radius * 2f
    if (radius == 0f) {
      slider.valueTo = MAX_DEF_RADIUS
    }
    slider.value = radius
  }

  companion object {
    private const val MAX_RADIUS = RadiusConfig.MAX_RADIUS.toFloat()
    private const val MAX_DEF_RADIUS = 5000f
  }
}
