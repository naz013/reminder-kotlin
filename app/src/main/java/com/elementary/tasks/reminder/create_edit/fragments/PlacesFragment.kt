package com.elementary.tasks.reminder.create_edit.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.fragments.PlacesMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.databinding.FragmentReminderPlaceBinding
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.Reminder
import com.google.android.gms.maps.model.LatLng

/**
 * Copyright 2016 Nazar Suhovich
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

class PlacesFragment : RadiusTypeFragment() {

    private var placesMap: PlacesMapFragment? = null

    private val mCallback = MapCallback {
        if (`interface`!!.reminder != null) {
            val item = `interface`!!.reminder
            placesMap!!.selectMarkers(item.places)
        }
    }
    private val mListener = object : MapListener {
        override fun placeChanged(place: LatLng, address: String) {

        }

        override fun onZoomClick(isFull: Boolean) {
            `interface`!!.setFullScreenMode(isFull)
        }

        override fun onBackClick() {
            `interface`!!.setFullScreenMode(false)
        }
    }

    override fun recreateMarker() {
        if (placesMap != null) {
            placesMap!!.recreateMarker(radius)
        }
    }

    override fun prepare(): Reminder? {
        if (super.prepare() == null) return null
        if (`interface` == null) return null
        var reminder: Reminder? = `interface`!!.reminder
        val type = Reminder.BY_PLACES
        if (TextUtils.isEmpty(`interface`!!.summary)) {
            `interface`!!.showSnackbar(getString(R.string.task_summary_is_empty))
            return null
        }
        val places = placesMap!!.places
        if (places.size == 0) {
            `interface`!!.showSnackbar(getString(R.string.you_dont_select_place))
            return null
        }
        if (reminder == null) {
            reminder = Reminder()
        }
        reminder.places = places
        reminder.target = null
        reminder.type = type
        reminder.isExportToCalendar = false
        reminder.isExportToTasks = false
        reminder.setClear(`interface`)
        reminder.eventTime = null
        reminder.startTime = null
        LogUtil.d(TAG, "REC_TIME " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true, true))
        return reminder
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.fragment_location_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_custom_radius -> showRadiusPickerDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentReminderPlaceBinding.inflate(inflater, container, false)
        val prefs = Prefs.getInstance(activity)
        placesMap = PlacesMapFragment()
        placesMap!!.setListener(mListener)
        placesMap!!.setCallback(mCallback)
        placesMap!!.setRadius(prefs.radius)
        placesMap!!.setMarkerStyle(prefs.markerStyle)
        fragmentManager!!.beginTransaction()
                .replace(binding.mapPlace.id, placesMap!!)
                .addToBackStack(null)
                .commit()
        return binding.root
    }

    override fun onBackPressed(): Boolean {
        return placesMap == null || placesMap!!.onBackPressed()
    }

    companion object {

        private val TAG = "PlacesFragment"
    }
}
