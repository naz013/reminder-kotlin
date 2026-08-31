package com.github.naz013.feature.places

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.navigation.DetailPanePlaceholder
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.feature.places.create.EditPlaceScreen
import com.github.naz013.feature.places.create.EditPlaceState
import com.github.naz013.feature.places.create.EditPlaceViewModel
import com.github.naz013.feature.places.list.PlacesScreen
import com.github.naz013.feature.places.list.PlacesScreenState
import com.github.naz013.feature.places.list.PlacesViewModel
import com.github.naz013.ui.common.compose.foundation.intent.rememberSendIntentResolver
import com.github.naz013.ui.map.MapParams
import com.github.naz013.ui.map.SimpleMapController
import com.github.naz013.ui.map.SimpleMapView
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Distinct from the default `sceneKey` (shared by every other two-pane flow in the app) for the
 * same reason as `WorkflowPaneSceneKey` in `feature-workflow`'s `WorkflowNavGraph.kt` - Places is
 * reached from within Settings' own two-pane detail chain (Reminders > Location > Places), so it
 * needs its own key to avoid the scene strategy pairing it with an unrelated list further down the
 * backstack.
 */
private const val PlacesPaneSceneKey: String = "com.github.naz013.feature.places.PlacesPane"

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.placesEntries(
  backStack: MutableList<NavKey>,
  isRenderedAsDetailPane: (NavKey) -> Boolean,
  adsContent: @Composable () -> Unit,
) {
  entry<PlacesNavKey.List>(
    metadata = ListDetailSceneStrategy.listPane(
      sceneKey = PlacesPaneSceneKey,
      detailPlaceholder = {
        DetailPanePlaceholder(
          text = stringResource(R.string.select_place_to_see_details),
          icon = AppIcons.Fluent.Place,
        )
      },
    ),
  ) { PlacesListEntry(backStack) }
  entry<PlacesNavKey.Edit>(
    metadata = ListDetailSceneStrategy.detailPane(sceneKey = PlacesPaneSceneKey),
  ) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    PlaceEditEntry(key, backStack, renderAsDetailPane, adsContent)
  }
}

@Composable
private fun PlacesListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<PlacesViewModel>()

  val dialogDispatcher = rememberDialogDispatcher()
  val toastDispatcher = rememberToastDispatcher()
  val sendIntentResolver = rememberSendIntentResolver()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is PlacesViewModel.NavigationEvent.OpenEditPlace -> {
        backStack.navigateToDetailPane(PlacesNavKey.Edit(event.id))
      }

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
        if (backStack.size > 1) backStack.removeLastOrNull()
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
  renderAsDetailPane: Boolean,
  adsContent: @Composable () -> Unit,
) {
  val viewModel = koinViewModel<EditPlaceViewModel> { parametersOf(key) }
  val dialogDispatcher = rememberDialogDispatcher()
  val toastDispatcher = rememberToastDispatcher()

  var mapController by remember { mutableStateOf<SimpleMapController?>(null) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      EditPlaceViewModel.EditPlaceEvent.MoveBack -> if (backStack.size > 1) backStack.removeLastOrNull()

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
      if (mapController?.onBackPressed() != false && backStack.size > 1) backStack.removeLastOrNull()
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
    adsContent = adsContent,
    renderAsDetailPane = renderAsDetailPane,
  )
}

/**
 * Navigation for the places two-pane list's detail pane: if the current top entry is itself an
 * edit form, replace it instead of stacking another one on top. Mirrors `GroupsNavGraph.kt`'s
 * identically-purposed private helper.
 */
private fun MutableList<NavKey>.navigateToDetailPane(key: NavKey) {
  if (lastOrNull() is PlacesNavKey.Edit) {
    removeLastOrNull()
  }
  add(key)
}
