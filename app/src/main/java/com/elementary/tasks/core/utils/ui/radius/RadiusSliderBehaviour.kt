package com.elementary.tasks.core.utils.ui.radius

import com.google.android.material.slider.Slider

class RadiusSliderBehaviour(
  private val slider: Slider,
  initValue: Int,
  private val onValueChanged: (value: Int) -> Unit
) {

  init {
    val current = initValue.toFloat()
    slider.addOnChangeListener { _, value, _ ->
      onValueChanged.invoke(value.toInt())
      val perc = value / slider.valueTo * 100f
      if (perc > 95f && slider.valueTo < MAX_RADIUS) {
        slider.valueTo = slider.valueTo + (slider.valueTo * 0.2f)
      } else if (perc < 10f && slider.valueTo.toInt() > 5000) {
        slider.valueTo = slider.valueTo - (slider.valueTo * 0.2f)
      }
    }
    slider.stepSize = 0f
    slider.valueFrom = 0f
    slider.valueTo = MAX_DEF_RADIUS

    if (slider.valueTo < current && slider.valueTo < MAX_RADIUS) {
      slider.valueTo = current + (slider.valueTo * 0.2f)
    }
    if (current > MAX_RADIUS) {
      slider.valueTo = MAX_RADIUS
    }
    slider.valueTo = current * 2f
    if (current == 0f) {
      slider.valueTo = MAX_DEF_RADIUS
    }
    slider.value = current
  }

  fun getRadius(): Int {
    return slider.value.toInt()
  }

  companion object {
    private const val MAX_RADIUS = 100000f
    private const val MAX_DEF_RADIUS = 5000f
  }
}
