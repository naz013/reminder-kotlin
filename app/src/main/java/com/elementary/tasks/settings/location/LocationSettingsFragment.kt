package com.elementary.tasks.settings.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.elementary.tasks.R
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.common.Module
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.androidx.viewmodel.ext.android.viewModel

class LocationSettingsFragment : BaseComposeToolbarFragment() {

  private val viewModel by viewModel<LocationSettingsViewModel>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }
    val hasLocation = remember { Module.hasLocation(requireContext()) }

    LocationSettingsScreen(
      state = state,
      hasLocation = hasLocation,
      onNotificationToggle = viewModel::onNotificationToggle,
      onRadiusClick = viewModel::onRadiusClick,
      onRadiusPreviewChange = viewModel::onRadiusPreviewChange,
      onRadiusConfirm = viewModel::onRadiusConfirm,
      onMapTypeClick = viewModel::onMapTypeClick,
      onMapTypeOptionSelected = viewModel::onMapTypeOptionSelected,
      onMapStyleClick = viewModel::onMapStyleClick,
      onMarkerStyleClick = viewModel::onMarkerStyleClick,
      onTrackerClick = viewModel::onTrackerClick,
      onTrackerPreviewChange = viewModel::onTrackerPreviewChange,
      onTrackerConfirm = viewModel::onTrackerConfirm,
      onPlacesClick = viewModel::onPlacesClick,
      onDialogDismiss = viewModel::onDialogDismiss,
    )
  }

  private fun handleEvent(event: LocationSettingsEvent) {
    when (event) {
      LocationSettingsEvent.OpenMapStyle -> {
        safeNavigation { LocationSettingsFragmentDirections.actionLocationSettingsFragmentToMapStyleFragment() }
      }

      LocationSettingsEvent.OpenPlaces -> {
        safeNavigation { LocationSettingsFragmentDirections.actionLocationSettingsFragmentToPlacesFragment() }
      }

      is LocationSettingsEvent.ShowMarkerColorPicker -> {
        withActivity { act ->
          dialogues.showColorDialog(
            act,
            event.currentColorIndex,
            getString(R.string.style_of_marker),
            ThemeProvider.colorsForSlider(act),
          ) { color -> viewModel.onMarkerColorSelected(color) }
        }
      }
    }
  }

  override fun onBackStackResumed() {
    super.onBackStackResumed()
    viewModel.onResume()
  }

  override fun getTitle(): String = getString(R.string.location)
}
