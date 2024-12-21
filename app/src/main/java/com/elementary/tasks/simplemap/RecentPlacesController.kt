package com.elementary.tasks.simplemap

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.github.naz013.feature.common.android.gone
import com.github.naz013.feature.common.android.isVisible
import com.github.naz013.feature.common.android.visible

class RecentPlacesController(
  rootView: View,
  private val placesAllowed: Boolean,
  private val listener: OnPlaceSelectedListener
) {

  private var placeRecyclerAdapter = RecentPlacesAdapter()

  private val placesButton = rootView.findViewById<View>(R.id.placesButtonCard)
  private val placesContainer = rootView.findViewById<View>(R.id.placesListCard)
  private val placesListView = rootView.findViewById<RecyclerView>(R.id.placesList)

  init {
    placesButton.gone()
    placesContainer.gone()

    placesListView.layoutManager = LinearLayoutManager(placesListView.context)
    placesListView.adapter = placeRecyclerAdapter

    placesButton.setOnClickListener { toggleCard() }

    hideCard()
  }

  fun onPlacesLoaded(places: List<UiPlaceList>) {
    if (placesAllowed && places.isNotEmpty()) {
      placesButton.visible()
      placeRecyclerAdapter.actionsListener = object : ActionsListener<UiPlaceList> {
        override fun onAction(view: View, position: Int, t: UiPlaceList?, actions: ListActions) {
          when (actions) {
            ListActions.OPEN, ListActions.MORE -> {
              hideCard()
              if (t != null) {
                listener.onPlaceSelected(t)
              }
            }

            else -> {
            }
          }
        }
      }
      placeRecyclerAdapter.submitList(places)
    }
  }

  fun onOutsideClick() {
    hideCard()
  }

  fun isLayerVisible(): Boolean {
    return placesContainer.isVisible()
  }

  private fun toggleCard() {
    listener.onPlaceButtonClicked()
    if (placesContainer.isVisible()) {
      hideCard()
    } else {
      showCard()
    }
  }

  private fun showCard() {
    placesContainer.visible()
  }

  private fun hideCard() {
    placesContainer.gone()
  }

  interface OnPlaceSelectedListener {
    fun onPlaceSelected(place: UiPlaceList)
    fun onPlaceButtonClicked()
  }
}
