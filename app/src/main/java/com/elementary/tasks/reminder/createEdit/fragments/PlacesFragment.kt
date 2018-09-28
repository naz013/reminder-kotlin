package com.elementary.tasks.reminder.createEdit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.PlacesMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_reminder_place.*

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

    private var mPlacesMap: PlacesMapFragment? = null
    private val mListener = object : MapListener {
        override fun placeChanged(place: LatLng, address: String) {
        }

        override fun onZoomClick(isFull: Boolean) {
            reminderInterface.setFullScreenMode(isFull)
        }

        override fun onBackClick() {
            reminderInterface.setFullScreenMode(false)
        }
    }

    override fun recreateMarker() {
        if (mPlacesMap != null) {
            mPlacesMap?.recreateMarker(0)
        }
    }

    override fun prepare(): Reminder? {
        if (super.prepare() == null || mPlacesMap == null) return null
        val iFace = reminderInterface ?: return null
        val type = Reminder.BY_PLACES
//        if (TextUtils.isEmpty(iFace.summary)) {
//            iFace.showSnackbar(getString(R.string.task_summary_is_empty))
//            return null
//        }
        val places = mPlacesMap!!.places
        if (places.isEmpty()) {
            iFace.showSnackbar(getString(R.string.you_dont_select_place))
            return null
        }
        var reminder = iFace.reminder
        if (reminder == null) {
            reminder = Reminder()
        }
        reminder.places = places
        reminder.target = ""
        reminder.type = type
        reminder.exportToCalendar = false
        reminder.exportToTasks = false
        reminder.eventTime = ""
        reminder.startTime = ""
        LogUtil.d(TAG, "REC_TIME " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true, true))
        return reminder
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val placesMap = PlacesMapFragment()
        placesMap.setListener(mListener)
        placesMap.setCallback(object : MapCallback {
            override fun onMapReady() {
                val iFace = reminderInterface ?: return
                val item = iFace.reminder
                if (item != null) {
                    mPlacesMap?.selectMarkers(item.places)
                }
            }
        })
        placesMap.setRadius(prefs.radius)
        placesMap.setMarkerStyle(prefs.markerStyle)
        fragmentManager!!.beginTransaction()
                .replace(mapPlace.id, placesMap)
                .addToBackStack(null)
                .commit()
        this.mPlacesMap = placesMap
    }

    override fun onBackPressed(): Boolean {
        return mPlacesMap == null || mPlacesMap!!.onBackPressed()
    }

    companion object {
        private const val TAG = "PlacesFragment"
    }
}
