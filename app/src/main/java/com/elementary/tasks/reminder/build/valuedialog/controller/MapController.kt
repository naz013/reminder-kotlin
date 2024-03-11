package com.elementary.tasks.reminder.build.valuedialog.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.databinding.BuilderItemMapBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController
import com.elementary.tasks.simplemap.SimpleMapFragment

class MapController(
  builderItem: BuilderItem<Place>,
  private val parentFragment: Fragment,
  private val dateTimeManager: DateTimeManager
) : AbstractBindingValueController<Place, BuilderItemMapBinding>(builderItem) {

  private var simpleMapFragment: SimpleMapFragment? = null

  override fun isDraggable(): Boolean {
    return false
  }

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemMapBinding {
    return BuilderItemMapBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    val simpleMapFragment = SimpleMapFragment.newInstance(SimpleMapFragment.MapParams())

    simpleMapFragment.mapCallback = object : SimpleMapFragment.MapCallback {
      override fun onLocationSelected(markerState: SimpleMapFragment.MarkerState) {
        getPlace().copy(
          latitude = markerState.latLng.latitude,
          longitude = markerState.latLng.longitude,
          radius = markerState.radius,
          marker = markerState.style,
          address = markerState.address,
          name = markerState.title,
          dateTime = dateTimeManager.getNowGmtDateTime()
        ).also { updateValue(it) }
      }

      override fun onMapReady() {
        builderItem.modifier.getValue()?.also { showPlace(it) }
      }
    }

    parentFragment.childFragmentManager.beginTransaction()
      .replace(binding.mapFrameView.id, simpleMapFragment)
      .addToBackStack(null)
      .commit()

    this.simpleMapFragment = simpleMapFragment
  }

  private fun showPlace(place: Place) {
    simpleMapFragment?.addMarker(
      latLng = place.latLng(),
      title = place.name,
      markerStyle = place.marker,
      radius = place.radius,
      clear = true,
      animate = true
    )
  }

  override fun onDestroy() {
    super.onDestroy()
    simpleMapFragment?.onDestroy()
  }

  private fun getPlace(): Place {
    return builderItem.modifier.getValue() ?: Place()
  }
}
