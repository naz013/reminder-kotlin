package com.github.naz013.feature.settings.location

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.settings.SettingsDetailPane
import com.github.naz013.feature.settings.SettingsScaffold
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.dialog.rememberColorPickerDialogDispatcher
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.locationEntries(
  backStack: MutableList<NavKey>,
  onOpenPlaces: () -> Unit,
) {
  entry<LocationNavKey.Location>(metadata = SettingsDetailPane) { LocationEntry(backStack, onOpenPlaces) }
  entry<LocationNavKey.MapStyle>(metadata = SettingsDetailPane) { MapStyleEntry(backStack) }
}

@Composable
private fun LocationEntry(
  backStack: MutableList<NavKey>,
  onOpenPlaces: () -> Unit,
) {
  val viewModel = koinViewModel<LocationSettingsViewModel>()
  val colorPickerDialogDispatcher = rememberColorPickerDialogDispatcher()
  val state by viewModel.state.collectAsState(LocationSettingsState())
  val hapticFeedback = LocalHapticFeedback.current

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      LocationSettingsEvent.OpenMapStyle -> backStack.add(LocationNavKey.MapStyle)
      LocationSettingsEvent.OpenPlaces -> onOpenPlaces()
      is LocationSettingsEvent.ShowMarkerColorPicker -> {
        colorPickerDialogDispatcher.showDialog(
          title = event.title,
          colors = event.colors,
          selectedIndex = event.currentColorIndex,
          hapticFeedbackEnabled = event.hapticFeedbackEnabled,
          onColorSelected = { color -> viewModel.onMarkerColorSelected(color) },
        )
      }

      LocationSettingsEvent.HapticFeedback -> {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      }
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.location),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    LocationSettingsScreen(
      state = state,
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
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    MapStyleScreen(
      state = state,
      onOptionSelected = viewModel::onOptionSelected,
      modifier = Modifier.padding(padding),
    )
  }
}
