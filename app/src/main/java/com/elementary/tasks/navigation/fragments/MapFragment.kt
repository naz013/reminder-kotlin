package com.elementary.tasks.navigation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.github.naz013.feature.common.android.dp2px
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.databinding.FragmentEventsMapBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.elementary.tasks.places.google.LocationPlacesAdapter
import com.elementary.tasks.reminder.lists.active.ActiveGpsRemindersViewModel
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : BaseToolbarFragment<FragmentEventsMapBinding>() {

  private val viewModel by viewModel<ActiveGpsRemindersViewModel>()
  private val mAdapter = LocationPlacesAdapter(get())

  private var simpleMapFragment: SimpleMapFragment? = null
  private var behaviour: BottomSheetBehavior<LinearLayout>? = null

  private var clickedPosition: Int = 0
  private var pointer: Int = 0
  private var isDataShowed: Boolean = false

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentEventsMapBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    behaviour = BottomSheetBehavior.from(binding.placesListCard)
    initMap()
    initViews()
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.events.observe(viewLifecycleOwner) { reminders ->
      if (reminders != null && simpleMapFragment != null) {
        showData(reminders)
      }
    }
  }

  private fun initMap() {
    val map = SimpleMapFragment.newInstance(
      SimpleMapFragment.MapParams(
        isTouch = false,
        isPlaces = false,
        isSearch = false,
        isStyles = false,
        isLayers = true,
        isRadius = false,
        mapStyleParams = SimpleMapFragment.MapStyleParams(
          mapType = prefs.mapType,
          mapStyle = prefs.mapStyle
        )
      )
    )

    map.mapCallback = object : SimpleMapFragment.DefaultMapCallback() {
      override fun onMapReady() {
        super.onMapReady()
        viewModel.events.value?.also { showData(it) }
      }
    }
    map.setOnMarkerClick { marker ->
      simpleMapFragment?.moveCamera(
        pos = marker.position,
        paddingLeft = 0,
        paddingTop = 0,
        paddingRight = 0,
        paddingBottom = dp2px(192)
      )
      false
    }

    childFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, map)
      .addToBackStack(null)
      .commit()

    simpleMapFragment = map
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
    simpleMapFragment?.moveCamera(
      LatLng(place.latitude, place.longitude),
      0,
      0,
      0,
      dp2px(192)
    )
  }

  override fun getTitle(): String = getString(R.string.map)

  private fun showData(data: List<Reminder>) {
    val map = simpleMapFragment
    if (isDataShowed || map == null) {
      return
    }
    mAdapter.setData(data)
    if (simpleMapFragment?.isMapReady == true) {
      for (reminder in data) {
        for (place in reminder.places) {
          map.addMarker(
            latLng = LatLng(place.latitude, place.longitude),
            title = place.name,
            markerStyle = place.marker,
            radius = place.radius,
            clear = false,
            animate = false
          )
        }
      }
      isDataShowed = true
    }

    reloadView()
  }

  override fun canGoBack(): Boolean {
    return simpleMapFragment?.onBackPressed() == true
  }

  private fun reloadView() {
    if (mAdapter.itemCount > 0) {
      binding.recyclerView.visibility = View.VISIBLE
      binding.emptyItem.visibility = View.GONE
    } else {
      binding.recyclerView.visibility = View.GONE
      binding.emptyItem.visibility = View.VISIBLE
    }
  }
}
