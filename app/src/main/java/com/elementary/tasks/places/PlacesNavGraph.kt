package com.elementary.tasks.places

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.ads.AdBanner
import com.elementary.tasks.ads.NormalAdBanner
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.places.create.EditPlaceScreen
import com.elementary.tasks.places.create.EditPlaceState
import com.elementary.tasks.places.create.EditPlaceViewModel
import com.elementary.tasks.places.list.PlacesScreen
import com.elementary.tasks.places.list.PlacesScreenState
import com.elementary.tasks.places.list.PlacesViewModel
import com.elementary.tasks.settings.rememberSendIntentResolver
import com.elementary.tasks.simplemap.MapParams
import com.elementary.tasks.simplemap.SimpleMapController
import com.elementary.tasks.simplemap.SimpleMapView
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.placesEntries(backStack: MutableList<NavKey>) {
  entry<PlacesNavKey.List> { PlacesListEntry(backStack) }
  entry<PlacesNavKey.Edit> { key -> PlaceEditEntry(key, backStack) }
}

@Composable
private fun PlacesListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<PlacesViewModel>()

  val dialogDispatcher = rememberDialogDispatcher()
  val toastDispatcher = rememberToastDispatcher()
  val sendIntentResolver = rememberSendIntentResolver()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is PlacesViewModel.NavigationEvent.OpenEditPlace -> backStack.add(PlacesNavKey.Edit(event.id))

      is PlacesViewModel.NavigationEvent.ShareFile -> {
        sendIntentResolver.resolve(event.intent, event.name)
      }

      is PlacesViewModel.NavigationEvent.ConfirmDelete -> {
        dialogDispatcher.showDialog(
          textRes = R.string.are_you_sure,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.deletePlace(event.id) },
        )
      }

      is PlacesViewModel.NavigationEvent.MoveBack -> {
        backStack.removeLastOrNull()
      }

      is PlacesViewModel.NavigationEvent.ShowToast -> {
        toastDispatcher.showToast(messageRes = event.messageRes)
      }
    }
  }

  val state by viewModel.screenState.collectAsState(PlacesScreenState())
  PlacesScreen(
    state = state,
    onBackClick = viewModel::onBackClicked,
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onAddClick = viewModel::onAddClick,
    onPlaceClick = viewModel::onPlaceClick,
    onPlaceMenuAction = viewModel::onPlaceMenuAction,
  )
}

@Composable
private fun PlaceEditEntry(
  key: PlacesNavKey.Edit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<EditPlaceViewModel> { parametersOf(key) }
  val dialogDispatcher = rememberDialogDispatcher()
  val toastDispatcher = rememberToastDispatcher()

  var mapController by remember { mutableStateOf<SimpleMapController?>(null) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      EditPlaceViewModel.EditPlaceEvent.MoveBack -> backStack.removeLastOrNull()

      EditPlaceViewModel.EditPlaceEvent.NoLocationSelected -> {
        toastDispatcher.showToast(messageRes = R.string.you_dont_select_place)
      }

      EditPlaceViewModel.EditPlaceEvent.ConfirmDelete -> {
        dialogDispatcher.showDialog(
          textRes = R.string.are_you_sure,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.deletePlace() },
        )
      }

      EditPlaceViewModel.EditPlaceEvent.AskCopySaving -> {
        dialogDispatcher.showDialog(
          textRes = R.string.same_place_message,
          positiveButtonRes = R.string.keep,
          negativeButtonRes = R.string.replace,
          neutralButtonRes = R.string.cancel,
          onPositive = { viewModel.savePlace(newId = true) },
          onNegative = { viewModel.savePlace() },
        )
      }
    }
  }

  val state by viewModel.state.collectAsState(EditPlaceState())

  EditPlaceScreen(
    state = state,
    onBackClick = {
      if (mapController?.onBackPressed() != false) backStack.removeLastOrNull()
    },
    onNameChange = viewModel::onNameChange,
    onSaveClick = viewModel::onSaveClick,
    onDeleteClick = viewModel::onDeleteClick,
    mapContent = {
      SimpleMapView(
        mapParams = MapParams(
          isPlaces = false,
          isStyles = true,
          isRadius = true,
          rememberMarkerRadius = false,
          rememberMarkerStyle = false,
        ),
        markers = state.markers,
        onLocationSelected = { markerState ->
          viewModel.onMarkerPlaced(markerState = markerState)
        },
        onControllerReady = { mapController = it },
        modifier = Modifier.fillMaxSize(),
      )
    },
    adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), adBanner = AdBanner.Place) },
  )
}
