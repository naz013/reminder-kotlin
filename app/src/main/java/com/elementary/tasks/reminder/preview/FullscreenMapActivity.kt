package com.elementary.tasks.reminder.preview

import android.os.Bundle
import android.transition.Explode
import android.view.MenuItem
import android.view.Window
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel
import com.elementary.tasks.databinding.ActivityFullscreenMapBinding
import com.google.android.gms.maps.model.LatLng
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class FullscreenMapActivity : BindingActivity<ActivityFullscreenMapBinding>() {

  private var mGoogleMap: AdvancedMapFragment? = null
  private val viewModel by viewModel<ReminderViewModel> { parametersOf(getId()) }

  private var reminder: Reminder? = null
  private var placeIndex = 0

  override fun inflateBinding() = ActivityFullscreenMapBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    with(window) {
      requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
      exitTransition = Explode()
    }
    super.onCreate(savedInstanceState)

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
      mGoogleMap?.moveCamera(LatLng(lat, lon), 0, 0, 0, 0)
    }

    initViewModel()
  }

  private fun getId() = intent.getStringExtra(Constants.INTENT_ID) ?: ""

  private fun initViewModel() {
    viewModel.reminder.observe(this, { showInfo(it) })
  }

  private fun showMapData(reminder: Reminder) {
    reminder.places.forEach {
      val lat = it.latitude
      val lon = it.longitude
      mGoogleMap?.addMarker(
        LatLng(lat, lon),
        reminder.summary,
        clear = false,
        animate = false,
        radius = it.radius
      )
    }
    val place = reminder.places[0]
    val lat = place.latitude
    val lon = place.longitude
    mGoogleMap?.moveCamera(LatLng(lat, lon), 0, 0, 0, 0)
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
    val googleMap = AdvancedMapFragment.newInstance(
      isPlaces = false,
      isStyles = false,
      isBack = true,
      isZoom = false,
      markerStyle = prefs.markerStyle,
      isDark = isDarkMode,
      isRadius = false
    )
    googleMap.setCallback(object : MapCallback {
      override fun onMapReady() {
        googleMap.setSearchEnabled(false)
        googleMap.setListener(object : MapListener {
          override fun onBackClick() {
            finishAfterTransition()
          }

          override fun onZoomClick(isFull: Boolean) {
          }

          override fun placeChanged(place: LatLng, address: String) {
          }
        })
        if (reminder != null) showMapData(reminder!!)
      }
    })
    googleMap.setOnMarkerClick(null)
    supportFragmentManager.beginTransaction()
      .replace(binding.mapContainer.id, googleMap)
      .addToBackStack(null)
      .commit()
    this.mGoogleMap = googleMap
  }

  override fun onBackPressed() {
    finishAfterTransition()
  }
}
