package com.elementary.tasks.reminder.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentReminderFullscreenMapBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseNonToolbarFragment
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.livedata.nullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.view.applyBottomInsetsMargin
import com.google.android.gms.maps.model.LatLng
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class FullscreenMapFragment : BaseNonToolbarFragment<FragmentReminderFullscreenMapBinding>() {

  private val viewModel by viewModel<FullScreenMapViewModel> { parametersOf(arguments) }
  private var simpleMapFragment: SimpleMapFragment? = null

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentReminderFullscreenMapBinding {
    return FragmentReminderFullscreenMapBinding.inflate(inflater, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the Map for reminder with id: ${viewModel.id}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.mapButton.applyBottomInsetsMargin()
    binding.mapButton.setOnClickListener {
      val reminder = viewModel.reminder.value ?: return@setOnClickListener
      if (viewModel.placeIndex < reminder.places.size - 1) {
        viewModel.placeIndex++
      } else {
        viewModel.placeIndex = 0
      }
      val place = reminder.places[viewModel.placeIndex]
      val lat = place.latitude
      val lon = place.longitude
      simpleMapFragment?.moveCamera(pos = LatLng(lat, lon))
    }

    initViewModel()
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.reminder.nullObserve(this) { initMap() }
  }

  private fun showMapData(reminder: Reminder) {
    reminder.places.forEach { place ->
      simpleMapFragment?.addMarker(
        latLng = LatLng(place.latitude, place.longitude),
        title = place.name.takeIf { it.isNotEmpty() }
          ?: place.address.takeIf { it.isNotEmpty() }
          ?: reminder.summary,
        markerStyle = place.marker,
        radius = place.radius,
        clear = false,
        animate = false
      )
    }

    reminder.places.firstOrNull()?.let {
      LatLng(it.latitude, it.longitude)
    }?.run {
      simpleMapFragment?.moveCamera(this)
    }
  }

  private fun initMap() {
    val googleMap = SimpleMapFragment.newInstance(
      SimpleMapFragment.MapParams(
        isPlaces = false,
        isStyles = false,
        isRadius = false,
        isSearch = false,
        isTouch = false,
        customButtons = listOf(
          SimpleMapFragment.MapCustomButton(R.drawable.ic_builder_arrow_left, 0)
        )
      )
    )
    googleMap.customButtonCallback = object : SimpleMapFragment.CustomButtonCallback {
      override fun onButtonClicked(buttonId: Int) {
        moveBack()
      }
    }
    googleMap.mapCallback = object : SimpleMapFragment.MapCallback {
      override fun onLocationSelected(markerState: SimpleMapFragment.MarkerState) {
      }

      override fun onMapReady() {
        simpleMapFragment?.applyInsets()
        viewModel.reminder.value?.also {
          showMapData(it)
        }
      }
    }

    childFragmentManager.beginTransaction()
      .replace(binding.mapContainer.id, googleMap)
      .commit()

    this.simpleMapFragment = googleMap
  }

  override fun canGoBack(): Boolean {
    val canCloseMap = simpleMapFragment?.onBackPressed()
    Logger.i(TAG, "Map can be closed: $canCloseMap")
    return canCloseMap == true
  }

  companion object {
    private const val TAG = "FullscreenMapFragment"
  }
}
