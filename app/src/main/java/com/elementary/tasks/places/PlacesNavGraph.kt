package com.elementary.tasks.places

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.places.create.EditPlaceEvent
import com.elementary.tasks.places.create.EditPlaceScreen
import com.elementary.tasks.places.create.EditPlaceViewModel
import com.elementary.tasks.places.list.PlacesScreen
import com.elementary.tasks.places.list.PlacesViewModel
import com.elementary.tasks.simplemap.MapParams
import com.elementary.tasks.simplemap.SimpleMapController
import com.elementary.tasks.simplemap.SimpleMapView
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Contributes the Places island's screens (Nav3 entries) and the routing between them into the
 * app's single, shared [androidx.navigation3.ui.NavDisplay] (see
 * [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
fun EntryProviderScope<NavKey>.placesEntries(backStack: MutableList<NavKey>) {
  entry<PlacesNavKey.List> { PlacesListEntry(backStack) }
  entry<PlacesNavKey.Edit> { key -> PlaceEditEntry(key, backStack) }
}

@Composable
private fun PlacesListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<PlacesViewModel>()
  bindLifecycle(viewModel)
  val dialogDispatcher = rememberDialogDispatcher()
  val context = LocalContext.current
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is PlacesViewModel.NavigationEvent.OpenEditPlace -> backStack.add(PlacesNavKey.Edit(event.id))

      is PlacesViewModel.NavigationEvent.ShareFile -> {
        TelephonyUtil.sendFile(event.file, context, event.name)
      }

      is PlacesViewModel.NavigationEvent.ConfirmDelete -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.delete,
          textRes = R.string.are_you_sure,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
          onPositive = { viewModel.deletePlace(event.id) },
        )
      }
    }
  }
  viewModel.errorEvent.ObserveEvent { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }

  val state by viewModel.screenState.collectAsState()
  PlacesScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
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
  val viewModel = koinViewModel<EditPlaceViewModel> { parametersOf(key.id) }
  bindLifecycle(viewModel)
  val dialogDispatcher = rememberDialogDispatcher()
  val context = LocalContext.current

  var mapController by remember { mutableStateOf<SimpleMapController?>(null) }

  LaunchedEffect(Unit) {
    if (key.fromIntentData) {
      viewModel.loadFromIntent()
    }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      EditPlaceEvent.Saved, EditPlaceEvent.Deleted -> backStack.removeLastOrNull()

      EditPlaceEvent.NoLocationSelected -> {
        Toast.makeText(context, R.string.you_dont_select_place, Toast.LENGTH_SHORT).show()
      }

      EditPlaceEvent.ConfirmDelete -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.delete,
          textRes = R.string.are_you_sure,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
          onPositive = { viewModel.deletePlace() },
        )
      }

      EditPlaceEvent.AskCopySaving -> {
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

  val state by viewModel.state.collectAsState()

  EditPlaceScreen(
    state = state,
    title = if (viewModel.hasId()) stringResource(R.string.edit_place) else stringResource(R.string.new_place),
    onBackClick = {
      if (mapController?.onBackPressed() != false) backStack.removeLastOrNull()
    },
    onNameChange = viewModel::onNameChange,
    onSaveClick = viewModel::onSaveClick,
    onDeleteClick = viewModel::onDeleteClick,
  ) {
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
        viewModel.lat = markerState.latLng.latitude
        viewModel.lng = markerState.latLng.longitude
        viewModel.address = markerState.address
        viewModel.markerStyle = markerState.styleIndex
        viewModel.markerRadius = markerState.radius

        if (viewModel.state.value.name.isEmpty()) {
          viewModel.onNameChange(viewModel.address)
        }
      },
      onControllerReady = { mapController = it },
      modifier = Modifier.fillMaxSize(),
    )
  }
}

@Composable
private fun bindLifecycle(observer: DefaultLifecycleObserver) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(observer, lifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
