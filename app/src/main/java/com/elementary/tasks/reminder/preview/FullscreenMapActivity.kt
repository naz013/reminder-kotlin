package com.elementary.tasks.reminder.preview

import android.os.Bundle
import android.transition.Explode
import android.view.MenuItem
import android.view.Window
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel
import com.elementary.tasks.databinding.ActivityFullscreenMapBinding
import com.google.android.gms.maps.model.LatLng

class FullscreenMapActivity : ThemedActivity<ActivityFullscreenMapBinding>() {

    private var mGoogleMap: AdvancedMapFragment? = null
    private lateinit var viewModel: ReminderViewModel

    private var reminder: Reminder? = null
    private var placeIndex = 0

    override fun layoutRes(): Int = R.layout.activity_fullscreen_map

    override fun onCreate(savedInstanceState: Bundle?) {
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
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

        initViewModel(id)
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, ReminderViewModel.Factory(id)).get(ReminderViewModel::class.java)
        viewModel.reminder.observe(this, Observer { reminder ->
            if (reminder != null) {
                showInfo(reminder)
            }
        })
    }

    private fun showMapData(reminder: Reminder) {
        reminder.places.forEach {
            val lat = it.latitude
            val lon = it.longitude
            mGoogleMap?.addMarker(LatLng(lat, lon), reminder.summary, false, false, it.radius)
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
        val ids = item.itemId
        when (ids) {
            android.R.id.home -> {
                finishAfterTransition()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initMap() {
        val googleMap = AdvancedMapFragment.newInstance(false, false, true,
                false, prefs.markerStyle, themeUtil.isDark, false)
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
