package com.elementary.tasks.navigation.fragments

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.view_models.reminders.ActiveGpsRemindersViewModel
import com.elementary.tasks.databinding.FragmentEventsMapBinding
import com.elementary.tasks.places.google.LocationPlacesAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MapFragment : BaseNavigationFragment<FragmentEventsMapBinding>() {

    private val viewModel: ActiveGpsRemindersViewModel by lazy {
        ViewModelProvider(this).get(ActiveGpsRemindersViewModel::class.java)
    }
    private val mAdapter = LocationPlacesAdapter()

    private var mGoogleMap: AdvancedMapFragment? = null
    private var behaviour: BottomSheetBehavior<LinearLayout>? = null

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

    override fun layoutRes(): Int = R.layout.fragment_events_map

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        behaviour = BottomSheetBehavior.from(binding.placesListCard)
        initMap()
        initViews()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.events.observe(viewLifecycleOwner, Observer { reminders ->
            if (reminders != null && mGoogleMap != null) {
                showData(reminders)
            }
        })
    }

    private fun initMap() {
        val map = AdvancedMapFragment.newInstance(false, isPlaces = false, isSearch = false,
                isStyles = false, isBack = false, isZoom = false, isDark = isDark)
        map.setCallback(mReadyCallback)
        map.setOnMarkerClick(mOnMarkerClick)
        parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, map)
                .addToBackStack(null)
                .commit()
        mGoogleMap = map
    }

    private fun initViews() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.actionsListener = object : ActionsListener<Reminder> {
            override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
                when (actions) {
                    ListActions.OPEN -> if (t != null) showClickedPlace(position, t)
                    else -> {
                    }
                }
            }

        }
        binding.recyclerView.adapter = mAdapter
        reloadView()
    }

    private fun showClickedPlace(position: Int, reminder: Reminder) {
        behaviour?.state = BottomSheetBehavior.STATE_COLLAPSED
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

    override fun canGoBack(): Boolean {
        return mGoogleMap?.onBackPressed() == true
    }

    private fun reloadView() {
        if (mAdapter.itemCount > 0) {
            binding.recyclerView?.visibility = View.VISIBLE
            binding.emptyItem?.visibility = View.GONE
        } else {
            binding.recyclerView?.visibility = View.GONE
            binding.emptyItem?.visibility = View.VISIBLE
        }
    }
}
