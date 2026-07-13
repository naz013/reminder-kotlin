package com.elementary.tasks.places

import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.LocalActivity
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.place.UiPlaceEdit
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.places.create.EditPlaceEvent
import com.elementary.tasks.places.create.EditPlaceScreen
import com.elementary.tasks.places.create.EditPlaceViewModel
import com.elementary.tasks.places.list.PlacesScreen
import com.elementary.tasks.places.list.PlacesViewModel
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.google.android.gms.maps.model.LatLng
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val MAP_FRAGMENT_TAG = "edit_place_map"

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

  var mapFragment by remember { mutableStateOf<SimpleMapFragment?>(null) }

  LaunchedEffect(Unit) {
    if (key.fromIntentData) {
      viewModel.loadFromIntent()
    }
  }

  LaunchedEffect(mapFragment) {
    mapFragment?.mapCallback = mapCallbackFor(viewModel) { mapFragment }
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
      if (mapFragment?.onBackPressed() != false) backStack.removeLastOrNull()
    },
    onNameChange = viewModel::onNameChange,
    onSaveClick = viewModel::onSaveClick,
    onDeleteClick = viewModel::onDeleteClick,
  ) {
    EmbeddedMap(onReady = { mapFragment = it })
  }
}

private fun mapCallbackFor(
  viewModel: EditPlaceViewModel,
  currentMapFragment: () -> SimpleMapFragment?,
): SimpleMapFragment.MapCallback =
  object : SimpleMapFragment.MapCallback {
    override fun onMapReady() {
      if (viewModel.state.value.canDelete) {
        viewModel.getPlace()?.also { showPlaceOnMap(currentMapFragment(), it) }
      }
    }

    override fun onLocationSelected(markerState: SimpleMapFragment.MarkerState) {
      viewModel.lat = markerState.latLng.latitude
      viewModel.lng = markerState.latLng.longitude
      viewModel.address = markerState.address
      viewModel.markerStyle = markerState.style
      viewModel.markerRadius = markerState.radius

      if (viewModel.state.value.name
          .isEmpty()
      ) {
        viewModel.onNameChange(viewModel.address)
      }
    }
  }

private fun showPlaceOnMap(
  mapFragment: SimpleMapFragment?,
  place: UiPlaceEdit,
) {
  mapFragment?.addMarker(
    latLng = LatLng(place.lat, place.lng),
    title = place.name,
    markerStyle = place.marker,
    radius = place.radius,
    clear = true,
    animate = true,
  )
}

@Composable
private fun EmbeddedMap(
  onReady: (SimpleMapFragment) -> Unit,
  modifier: Modifier = Modifier,
) {
  val activity = LocalActivity.current as FragmentActivity
  // Scoped to this specific composition of the Edit entry (not to the FragmentManager as a whole)
  // — a leftover SimpleMapFragment from a *previous* visit to this screen can still be registered
  // under MAP_FRAGMENT_TAG when this composable is entered again, but its view was hosted in a
  // FragmentContainerView that Compose has since disposed. Re-using that stale fragment instead of
  // replacing it left the new container empty ("map is empty the second time"), so every fresh
  // entry into this screen must always run its own replace() — which also correctly detaches
  // whatever fragment occupied the container before.
  var attached by remember { mutableStateOf(false) }
  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { context ->
      FragmentContainerView(context).apply {
        id = R.id.edit_place_map_container
        layoutParams =
          ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
      }
    },
    update = {
      if (attached) return@AndroidView
      attached = true
      val mapFragment =
        SimpleMapFragment.newInstance(
          SimpleMapFragment.MapParams(
            isPlaces = false,
            isStyles = true,
            isRadius = true,
            rememberMarkerRadius = false,
            rememberMarkerStyle = false,
          ),
        )
      activity.supportFragmentManager
        .beginTransaction()
        .replace(R.id.edit_place_map_container, mapFragment, MAP_FRAGMENT_TAG)
        .commitNow()
      onReady(mapFragment)
    },
  )
}

@Composable
private fun bindLifecycle(observer: DefaultLifecycleObserver) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(observer, lifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
