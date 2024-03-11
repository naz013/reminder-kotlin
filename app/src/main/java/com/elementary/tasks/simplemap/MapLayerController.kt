package com.elementary.tasks.simplemap

import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.isVisible
import com.elementary.tasks.core.utils.ui.visible
import com.google.android.gms.maps.GoogleMap

class MapLayerController(
  rootView: View,
  private val listener: OnLayerStyleListener
) {

  private val layersButton = rootView.findViewById<View>(R.id.layersCard)
  private val layersContainer = rootView.findViewById<View>(R.id.layersContainer)

  private val normalLayerButton = rootView.findViewById<View>(R.id.typeNormal)
  private val satelliteLayerButton = rootView.findViewById<View>(R.id.typeSatellite)
  private val terrainLayerButton = rootView.findViewById<View>(R.id.typeTerrain)
  private val hybridLayerButton = rootView.findViewById<View>(R.id.typeHybrid)

  private val mapStyleContainer = rootView.findViewById<View>(R.id.mapStyleContainer)

  private val styleDayButton = rootView.findViewById<View>(R.id.styleDay)
  private val styleRetroButton = rootView.findViewById<View>(R.id.styleRetro)
  private val styleSilverButton = rootView.findViewById<View>(R.id.styleSilver)
  private val styleNightButton = rootView.findViewById<View>(R.id.styleNight)
  private val styleDarkButton = rootView.findViewById<View>(R.id.styleDark)
  private val styleAubergineButton = rootView.findViewById<View>(R.id.styleAubergine)

  init {
    layersButton.setOnClickListener { toggleLayersCard() }

    normalLayerButton.setOnClickListener { onLayerClicked(GoogleMap.MAP_TYPE_NORMAL) }
    satelliteLayerButton.setOnClickListener { onLayerClicked(GoogleMap.MAP_TYPE_SATELLITE) }
    terrainLayerButton.setOnClickListener { onLayerClicked(GoogleMap.MAP_TYPE_HYBRID) }
    hybridLayerButton.setOnClickListener { onLayerClicked(GoogleMap.MAP_TYPE_TERRAIN) }

    styleDayButton.setOnClickListener { onStyleClicked(0) }
    styleRetroButton.setOnClickListener { onStyleClicked(1) }
    styleSilverButton.setOnClickListener { onStyleClicked(2) }
    styleNightButton.setOnClickListener { onStyleClicked(3) }
    styleDarkButton.setOnClickListener { onStyleClicked(4) }
    styleAubergineButton.setOnClickListener { onStyleClicked(5) }

    hideAllCards()
  }

  fun onOutsideClick() {
    hideAllCards()
  }

  fun isLayerVisible(): Boolean {
    return layersContainer.isVisible() || mapStyleContainer.isVisible()
  }

  private fun onStyleClicked(style: Int) {
    listener.onStyleChanged(style)
    hideStylesCard()
  }

  private fun onLayerClicked(type: Int) {
    listener.onLayerChanged(type)
    hideLayersCard()
    if (type == GoogleMap.MAP_TYPE_NORMAL) {
      showStylesCard()
    }
  }

  private fun hideAllCards() {
    hideLayersCard()
    hideStylesCard()
  }

  private fun toggleLayersCard() {
    listener.onLayerButtonClicked()
    if (mapStyleContainer.isVisible()) {
      hideStylesCard()
    } else {
      if (layersContainer.isVisible()) {
        hideLayersCard()
      } else {
        showLayersCard()
      }
    }
  }

  private fun showLayersCard() {
    layersContainer.visible()
  }

  private fun hideLayersCard() {
    if (layersContainer.isVisible()) {
      layersContainer.gone()
    }
  }

  private fun toggleStylesCard() {
    if (mapStyleContainer.isVisible()) {
      hideStylesCard()
    } else {
      showStylesCard()
    }
  }

  private fun showStylesCard() {
    mapStyleContainer.visible()
  }

  private fun hideStylesCard() {
    if (mapStyleContainer.isVisible()) {
      mapStyleContainer.gone()
    }
  }

  interface OnLayerStyleListener {
    fun onLayerChanged(type: Int)
    fun onStyleChanged(style: Int)
    fun onLayerButtonClicked()
  }
}
