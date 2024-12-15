package com.elementary.tasks.reminder.preview

import android.os.Bundle
import android.transition.Explode
import android.view.MenuItem
import android.view.Window
import androidx.activity.enableEdgeToEdge
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.nullObserve
import com.elementary.tasks.core.utils.ui.applyBottomInsetsMargin
import com.elementary.tasks.databinding.ActivityFullscreenMapBinding
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.google.android.gms.maps.model.LatLng
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class FullscreenMapActivity : BindingActivity<ActivityFullscreenMapBinding>() {

  private var simpleMapFragment: SimpleMapFragment? = null
  private val viewModel by viewModel<FullScreenMapViewModel> { parametersOf(getId()) }

  private var reminder: Reminder? = null
  private var placeIndex = 0

  override fun inflateBinding() = ActivityFullscreenMapBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    with(window) {
      requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
      exitTransition = Explode()
    }
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    binding.mapButton.applyBottomInsetsMargin()
    binding.mapButton.setOnClickListener {
      val reminder = reminder ?: return@setOnClickListener
      if (placeIndex < reminder.places.size - 1) {
        placeIndex++
      } else {
        placeIndex = 0
      }
      val place = reminder.places[placeIndex]
      val lat = place.latitude
      val lon = place.longitude
      simpleMapFragment?.moveCamera(pos = LatLng(lat, lon))
    }

    initViewModel()
  }

  private fun getId() = intent.getStringExtra(Constants.INTENT_ID) ?: ""

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.reminder.nullObserve(this) { showInfo(it) }
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

  private fun showInfo(reminder: Reminder) {
    this.reminder = reminder
    initMap()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        finishAfterTransition()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
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
        finishAfterTransition()
      }
    }
    googleMap.mapCallback = object : SimpleMapFragment.MapCallback {
      override fun onLocationSelected(markerState: SimpleMapFragment.MarkerState) {
      }

      override fun onMapReady() {
        reminder?.also { showMapData(it) }
      }
    }

    supportFragmentManager.beginTransaction()
      .replace(binding.mapContainer.id, googleMap)
      .addToBackStack(null)
      .commit()

    this.simpleMapFragment = googleMap
  }

  override fun handleBackPress(): Boolean {
    finishAfterTransition()
    return true
  }
}
