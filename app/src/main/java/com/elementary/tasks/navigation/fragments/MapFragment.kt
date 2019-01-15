package com.elementary.tasks.navigation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.viewModels.reminders.ActiveGpsRemindersViewModel
import com.elementary.tasks.places.google.LocationPlacesAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_events_map.*

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
class MapFragment : BaseNavigationFragment() {

    private lateinit var viewModel: ActiveGpsRemindersViewModel
    private val mAdapter = LocationPlacesAdapter()

    private var mGoogleMap: AdvancedMapFragment? = null

    private var clickedPosition: Int = 0
    private var pointer: Int = 0
    private var isDataShowed: Boolean = false

    private val mReadyCallback = object : MapCallback {
        override fun onMapReady() {
            mGoogleMap?.setSearchEnabled(false)
            val data = viewModel.events.value
            if (data != null) showData(data)
        }
    }
    private val mOnMarkerClick = GoogleMap.OnMarkerClickListener { marker ->
        mGoogleMap?.moveCamera(marker.position, 0, 0, 0, MeasureUtils.dp2px(context!!, 192))
        false
    }

    private fun showClickedPlace(position: Int, reminder: Reminder) {
        val maxPointer = reminder.places.size - 1
        if (position != clickedPosition) {
            pointer = 0
        } else {
            if (pointer == maxPointer) {
                pointer = 0
            } else {
                pointer++
            }
        }
        clickedPosition = position
        val place = reminder.places[pointer]
        mGoogleMap?.moveCamera(LatLng(place.latitude, place.longitude), 0, 0, 0, MeasureUtils.dp2px(context!!, 192))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_events_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()
        initViews()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ActiveGpsRemindersViewModel::class.java)
        viewModel.events.observe(this, Observer{ reminders ->
            if (reminders != null && mGoogleMap != null) {
                showData(reminders)
            }
        })
    }

    private fun initMap() {
        val map = AdvancedMapFragment.newInstance(false, false, false,
                false, false, false, themeUtil.isDark)
        map.setCallback(mReadyCallback)
        map.setOnMarkerClick(mOnMarkerClick)
        fragmentManager!!.beginTransaction()
                .replace(R.id.fragment_container, map)
                .addToBackStack(null)
                .commit()
        mGoogleMap = map
    }

    private fun initViews() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.actionsListener = object : ActionsListener<Reminder> {
            override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
                when (actions) {
                    ListActions.OPEN -> if (t != null) showClickedPlace(position, t)
                    else -> {
                    }
                }
            }

        }
        recyclerView.adapter = mAdapter
        reloadView()
    }

    override fun getTitle(): String = getString(R.string.map)

    private fun showData(data: List<Reminder>) {
        val map = mGoogleMap
        if (isDataShowed || map == null) {
            return
        }
        mAdapter.setData(data)
        var mapReady = false
        for (reminder in data) {
            for (place in reminder.places) {
                mapReady = map.addMarker(LatLng(place.latitude, place.longitude),
                        place.name, false, place.marker, false, place.radius)
                if (!mapReady) {
                    break
                }
            }
            if (!mapReady) {
                break
            }
        }
        isDataShowed = mapReady
        reloadView()
    }

    private fun reloadView() {
        if (mAdapter.itemCount > 0) {
            recyclerView?.visibility = View.VISIBLE
            emptyItem?.visibility = View.GONE
        } else {
            recyclerView?.visibility = View.GONE
            emptyItem?.visibility = View.VISIBLE
        }
    }
}
