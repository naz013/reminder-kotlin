package com.elementary.tasks.reminder.preview.adapter

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.reminder.UiReminderPlace
import com.elementary.tasks.core.text.applyStyles
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.ui.common.view.inflater
import com.elementary.tasks.databinding.ListItemReminderPreviewMapBinding
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewMap
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.google.android.gms.maps.model.LatLng

class ReminderMapViewHolder(
  parent: ViewGroup,
  fragmentManager: FragmentManager,
  prefs: Prefs,
  private val onMapClick: (View) -> Unit,
  binding: ListItemReminderPreviewMapBinding =
    ListItemReminderPreviewMapBinding.inflate(parent.inflater(), parent, false)
) : HolderBinding<ListItemReminderPreviewMapBinding>(binding) {

  private var mapFragment: SimpleMapFragment? = null
  private var places: List<UiReminderPlace>? = null

  init {
    val simpleMapFragment = SimpleMapFragment.newInstance(
      SimpleMapFragment.MapParams(
        isTouch = false,
        isSearch = false,
        isRadius = false,
        isPlaces = false,
        isStyles = false,
        isLayers = false,
        mapStyleParams = SimpleMapFragment.MapStyleParams(
          mapType = prefs.mapType,
          mapStyle = prefs.mapStyle
        )
      )
    )

    simpleMapFragment.mapCallback = object : SimpleMapFragment.DefaultMapCallback() {
      override fun onMapReady() {
        simpleMapFragment.setOnMapClickListener { onMapClick(binding.mapContainer) }
        places?.also { showPlaceOnMap(it) }
      }
    }

    fragmentManager.beginTransaction()
      .replace(binding.mapContainer.id, simpleMapFragment)
      .addToBackStack(null)
      .commit()

    this.mapFragment = simpleMapFragment
  }

  fun bind(map: UiReminderPreviewMap) {
    binding.placeTextView.text = map.placesText.text
    binding.placeTextView.applyStyles(map.placesText.textFormat)

    places = map.places
    showPlaceOnMap(map.places)
  }

  private fun showPlaceOnMap(places: List<UiReminderPlace>) {
    places.forEach {
      val lat = it.latitude
      val lon = it.longitude
      mapFragment?.addMarker(
        latLng = LatLng(lat, lon),
        title = it.address,
        markerStyle = it.marker,
        radius = it.radius,
        clear = false,
        animate = false
      )
    }
    places.firstOrNull()?.run {
      mapFragment?.moveCamera(latLng(), 0, 0, 0, 0)
    }
  }
}
