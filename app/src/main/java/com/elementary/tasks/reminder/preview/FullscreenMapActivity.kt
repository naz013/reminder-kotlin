package com.elementary.tasks.reminder.preview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_fullscreen_map.*

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class FullscreenMapActivity : ThemedActivity() {

    private var mGoogleMap: AdvancedMapFragment? = null
    private lateinit var viewModel: ReminderViewModel

    private var reminder: Reminder? = null
    private val mUiHandler = Handler(Looper.getMainLooper())
    private var placeIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        setContentView(R.layout.activity_fullscreen_map)

        mapButton.setOnClickListener {
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
        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
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
                closeWindow()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun closeWindow() {
        mUiHandler.post { this.finishAfterTransition() }
    }

    private fun initMap() {
        val googleMap = AdvancedMapFragment.newInstance(false, false, true,
                false, prefs.markerStyle, themeUtil.isDark, false)
        googleMap.setCallback(object : MapCallback {
            override fun onMapReady() {
                googleMap.setSearchEnabled(false)
                googleMap.setListener(object : MapListener {
                    override fun onBackClick() {
                        closeWindow()
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
                .replace(mapContainer.id, googleMap)
                .addToBackStack(null)
                .commit()
        this.mGoogleMap = googleMap
    }

    override fun onBackPressed() {
        closeWindow()
    }
}
