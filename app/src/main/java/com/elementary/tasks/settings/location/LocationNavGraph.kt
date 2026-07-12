package com.elementary.tasks.settings.location

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.navigation.nav3.AppNavBridge
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.places.PlacesNavKey
import com.elementary.tasks.settings.SettingsScaffold
import com.github.naz013.common.Module
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Contributes the Location Settings sub-tree's screens (Nav3 entries) into the app's single,
 * shared [androidx.navigation3.ui.NavDisplay] (see
 * [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
fun EntryProviderScope<NavKey>.locationEntries(backStack: MutableList<NavKey>) {
  entry<LocationNavKey.Location> { LocationEntry(backStack) }
  entry<LocationNavKey.MapStyle> { MapStyleEntry(backStack) }
}

@Composable
private fun LocationEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<LocationSettingsViewModel>()
  val context = LocalContext.current
  val activity = LocalActivity.current as FragmentActivity
  val dialogues = koinInject<Dialogues>()
  val appNavBridge = koinInject<AppNavBridge>()
  val hasLocation = remember { Module.hasLocation(context) }
  val state by viewModel.state.collectAsState()

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(viewModel, lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume() }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      LocationSettingsEvent.OpenMapStyle -> backStack.add(LocationNavKey.MapStyle)
      LocationSettingsEvent.OpenPlaces -> appNavBridge.navigate(PlacesNavKey.List)
      is LocationSettingsEvent.ShowMarkerColorPicker -> {
        dialogues.showColorDialog(
          activity,
          event.currentColorIndex,
          context.getString(R.string.style_of_marker),
          ThemeProvider.colorsForSlider(activity),
        ) { color -> viewModel.onMarkerColorSelected(color) }
      }
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.location),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
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
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun MapStyleEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<MapStyleViewModel>()
  val state by viewModel.state.collectAsState()

  SettingsScaffold(
    title = stringResource(R.string.map_style),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    MapStyleScreen(
      state = state,
      onOptionSelected = viewModel::onOptionSelected,
      modifier = Modifier.padding(padding),
    )
  }
}
