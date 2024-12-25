package com.elementary.tasks.simplemap

import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.config.MapConfig
import com.elementary.tasks.core.utils.BuildParams
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.isVisible
import com.github.naz013.ui.common.view.visible
import com.github.naz013.colorslider.ColorSlider

class MarkerStyleController(
  rootView: View,
  startColor: Int,
  colors: IntArray,
  selectorColor: Int,
  private val listener: OnStyleSelectedListener
) {

  private val styleButton = rootView.findViewById<View>(R.id.markersCard)
  private val colorPickerCard = rootView.findViewById<View>(R.id.markerStyleContainer)
  private val colorSlider = rootView.findViewById<ColorSlider>(R.id.markerColorSlider)

  var selectedStyle: Int = startColor
    private set

  init {
    if (!BuildParams.isPro && selectedStyle != MapConfig.DEFAULT_MARKER_STYLE) {
      selectedStyle = MapConfig.DEFAULT_MARKER_STYLE
    }

    colorSlider.setColors(colors)
    colorSlider.setSelectorColorResource(selectorColor)
    colorSlider.setSelection(selectedStyle)
    colorSlider.setListener { i, _ ->
      selectedStyle = i
      listener.onStyleSelected(i)
    }
    styleButton.setOnClickListener { toggleCard() }
    hideCard()
  }

  fun setStyle(style: Int) {
    selectedStyle = style
    colorSlider.setSelection(style)
  }

  fun onOutsideClick() {
    hideCard()
  }

  fun isLayerVisible(): Boolean {
    return colorPickerCard.isVisible()
  }

  private fun toggleCard() {
    listener.onStyleButtonClicked()
    if (colorPickerCard.isVisible()) {
      hideCard()
    } else {
      showCard()
    }
  }

  private fun showCard() {
    colorPickerCard.visible()
  }

  private fun hideCard() {
    colorPickerCard.gone()
  }

  interface OnStyleSelectedListener {
    fun onStyleSelected(style: Int)
    fun onStyleButtonClicked()
  }
}
