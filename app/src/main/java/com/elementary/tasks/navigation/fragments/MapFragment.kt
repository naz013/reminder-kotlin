package com.elementary.tasks.navigation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.view_models.reminders.ActiveGpsRemindersViewModel
import com.elementary.tasks.databinding.BottomSheetLayoutBinding
import com.elementary.tasks.databinding.FragmentEventsMapBinding
import com.elementary.tasks.places.google.LocationPlacesAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

    private var binding: FragmentEventsMapBinding? = null
    private var viewModel: ActiveGpsRemindersViewModel? = null
    private val mAdapter = LocationPlacesAdapter()

    private var mGoogleMap: AdvancedMapFragment? = null
    private var mEventsList: RecyclerView? = null
    private var mEmptyItem: LinearLayout? = null

    private var clickedPosition: Int = 0
    private var pointer: Int = 0
    private var isDataShowed: Boolean = false

    private val mReadyCallback = MapCallback {
        mGoogleMap!!.setSearchEnabled(false)
        showData()
    }
    private val mSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {

        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }
    }
    private val mOnMarkerClick = GoogleMap.OnMarkerClickListener { marker ->
        mGoogleMap!!.moveCamera(marker.position, 0, 0, 0, MeasureUtils.dp2px(context!!, 192))
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
        mGoogleMap!!.moveCamera(LatLng(place.latitude, place.longitude), 0, 0, 0, MeasureUtils.dp2px(context!!, 192))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEventsMapBinding.inflate(inflater, container, false)
        initMap()
        initViews()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ActiveGpsRemindersViewModel::class.java)
        viewModel!!.events.observe(this, { reminders ->
            if (reminders != null && mGoogleMap != null) {
                showData()
            }
        })
    }

    private fun initMap() {
        mGoogleMap = AdvancedMapFragment.newInstance(false, false, false, false, false, false,
                ThemeUtil.getInstance(context).isDark)
        mGoogleMap!!.setCallback(mReadyCallback)
        mGoogleMap!!.setOnMarkerClick(mOnMarkerClick)
        fragmentManager!!.beginTransaction()
                .replace(R.id.fragment_container, mGoogleMap!!)
                .addToBackStack(null)
                .commit()
    }

    private fun initViews() {
        val bottomSheet = binding!!.bottomSheet
        mEventsList = bottomSheet.recyclerView
        mEventsList!!.layoutManager = LinearLayoutManager(context)

        mAdapter.actionsListener = { view, position, reminder, actions ->
            when (actions) {
                ListActions.OPEN -> showClickedPlace(position, reminder)
            }
        }
        mEventsList!!.adapter = mAdapter

        mEmptyItem = bottomSheet.emptyItem
        binding!!.sheetLayout.setBackgroundColor(ThemeUtil.getInstance(context).cardStyle)
        val mBottomSheetBehavior = BottomSheetBehavior.from<NestedScrollView>(binding!!.sheetLayout)
        mBottomSheetBehavior.setBottomSheetCallback(mSheetCallback)
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.map))
            callback!!.onFragmentSelect(this)
        }
    }

    private fun showData() {
        val data = viewModel!!.events.value
        if (isDataShowed || data == null) {
            return
        }
        mAdapter.setData(data)
        var mapReady = false
        for (reminder in data) {
            for (place in reminder.places) {
                mapReady = mGoogleMap!!.addMarker(LatLng(place.latitude, place.longitude),
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
        val adapter = mEventsList!!.adapter
        var size = 0
        if (adapter != null) {
            size = mEventsList!!.adapter!!.itemCount
        }
        if (size > 0) {
            mEventsList!!.visibility = View.VISIBLE
            mEmptyItem!!.visibility = View.GONE
        } else {
            mEventsList!!.visibility = View.GONE
            mEmptyItem!!.visibility = View.VISIBLE
        }
    }
}
