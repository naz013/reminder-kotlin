package com.elementary.tasks.reminder.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import com.elementary.tasks.R
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.fragments.NavigationFragment
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.compose.composeView
import com.google.android.gms.maps.model.LatLng
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ReminderFullscreenMapFragment :
  NavigationFragment(),
  BackPressHandler {
  private val id: String by lazy { arguments?.getString(IntentKeys.INTENT_ID) ?: "" }
  private val viewModel by viewModel<FullScreenMapViewModel> { parametersOf(id) }
  private var simpleMapFragment: SimpleMapFragment? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the Map for reminder with id: $id")
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View =
    composeView {
      val reminder by viewModel.reminder.collectAsState()
      ReminderFullscreenMapScreen(
        isLoading = reminder == null,
        onMoveToPlaceClick = { moveToNextPlace() },
        mapContent = { reminder?.let { EmbeddedMap(reminder = it) } },
      )
    }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    lifecycle.addObserver(viewModel)
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  /** See [PreviewReminderFragment.onDestroyView]'s kdoc: the embedded [SimpleMapFragment] must be
   *  explicitly removed here, or the child FragmentManager tries to restore it into a container id
   *  Compose hasn't recreated yet the next time this fragment's view is built, crashing with
   *  "No view found for id ... for fragment SimpleMapFragment". */
  override fun onDestroyView() {
    simpleMapFragment?.let { mapFragment ->
      if (mapFragment.isAdded) {
        childFragmentManager.beginTransaction().remove(mapFragment).commitNowAllowingStateLoss()
      }
    }
    simpleMapFragment = null
    super.onDestroyView()
  }

  override fun canGoBack(): Boolean = simpleMapFragment?.onBackPressed() ?: true

  private fun moveToNextPlace() {
    val reminder = viewModel.reminder.value ?: return
    if (reminder.places.isEmpty()) return
    viewModel.placeIndex =
      if (viewModel.placeIndex < reminder.places.size - 1) {
        viewModel.placeIndex + 1
      } else {
        0
      }
    val place = reminder.places[viewModel.placeIndex]
    simpleMapFragment?.moveCamera(pos = LatLng(place.latitude, place.longitude))
  }

  @Composable
  private fun EmbeddedMap(reminder: Reminder) {
    var attached by remember { mutableStateOf(false) }
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = { context ->
        FragmentContainerView(context).apply {
          id = R.id.reminder_fullscreen_map_container
          layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
      },
      update = {
        if (attached) return@AndroidView
        attached = true
        val googleMap =
          SimpleMapFragment.newInstance(
            SimpleMapFragment.MapParams(
              isPlaces = false,
              isStyles = false,
              isRadius = false,
              isSearch = false,
              isTouch = false,
              customButtons =
                listOf(
                  SimpleMapFragment.MapCustomButton(R.drawable.ic_builder_arrow_left, 0),
                ),
            ),
          )
        googleMap.customButtonCallback =
          object : SimpleMapFragment.CustomButtonCallback {
            override fun onButtonClicked(buttonId: Int) {
              requireActivity().onBackPressedDispatcher.onBackPressed()
            }
          }
        googleMap.mapCallback =
          object : SimpleMapFragment.DefaultMapCallback() {
            override fun onMapReady() {
              googleMap.applyInsets()
              showMapData(googleMap, reminder)
            }
          }
        childFragmentManager
          .beginTransaction()
          .replace(R.id.reminder_fullscreen_map_container, googleMap)
          .commit()
        simpleMapFragment = googleMap
      },
    )
  }

  private fun showMapData(
    map: SimpleMapFragment,
    reminder: Reminder,
  ) {
    reminder.places.forEach { place ->
      map.addMarker(
        latLng = LatLng(place.latitude, place.longitude),
        title =
          place.name.takeIf { it.isNotEmpty() }
            ?: place.address.takeIf { it.isNotEmpty() }
            ?: reminder.summary,
        markerStyle = place.marker,
        radius = place.radius,
        clear = false,
        animate = false,
      )
    }
    reminder.places
      .firstOrNull()
      ?.let { LatLng(it.latitude, it.longitude) }
      ?.run { map.moveCamera(this) }
  }

  companion object {
    private const val TAG = "ReminderFullscreenMapFragment"
  }
}
