package com.elementary.tasks.simplemap

import android.view.View
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.config.RadiusConfig
import com.elementary.tasks.core.utils.ui.ValueFormatter
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.isVisible
import com.elementary.tasks.core.utils.ui.radius.RadiusSliderBehaviour
import com.elementary.tasks.core.utils.ui.visible
import com.google.android.material.slider.Slider

class MarkerRadiusController(
  rootView: View,
  currentRadius: Int,
  formatter: ValueFormatter<Int>,
  private val listener: OnRadiusChangedListener
) {

  private val radiusButton = rootView.findViewById<View>(R.id.radiusCard)
  private val radiusPickerCard = rootView.findViewById<View>(R.id.markerRadiusContainer)
  private val radiusSlider = rootView.findViewById<Slider>(R.id.markerRadiusSlider)
  private val radiusTextView = rootView.findViewById<TextView>(R.id.markerRadiusTitleView)

  private lateinit var radiusSliderBehaviour: RadiusSliderBehaviour

  var selectedRadius: Int = currentRadius
    private set

  init {
    if (currentRadius < RadiusConfig.MIN_RADIUS) {
      selectedRadius = 0
    }

    radiusSliderBehaviour = RadiusSliderBehaviour(radiusSlider, selectedRadius) {
      radiusTextView.text = formatter.format(it)
      selectedRadius = it
      listener.onChanged(it)
    }
    radiusTextView.text = formatter.format(selectedRadius)
    radiusButton.setOnClickListener { toggleCard() }
    hideCard()
  }

  fun setRadius(radius: Int) {
    selectedRadius = radius
    if (::radiusSliderBehaviour.isInitialized) {
      radiusSliderBehaviour.setRadius(radius)
    }
  }

  fun onOutsideClick() {
    hideCard()
  }

  fun isLayerVisible(): Boolean {
    return radiusPickerCard.isVisible()
  }

  private fun toggleCard() {
    listener.onRadiusButtonClicked()
    if (radiusPickerCard.isVisible()) {
      hideCard()
    } else {
      showCard()
    }
  }

  private fun showCard() {
    radiusPickerCard.visible()
  }

  private fun hideCard() {
    radiusPickerCard.gone()
  }

  interface OnRadiusChangedListener {
    fun onChanged(radius: Int)
    fun onRadiusButtonClicked()
  }
}
