package com.elementary.tasks.places

import android.view.ViewGroup
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.place.UiPlaceEdit
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.places.create.EditPlaceEvent
import com.elementary.tasks.places.create.EditPlaceScreen
import com.elementary.tasks.places.create.EditPlaceViewModel
import com.elementary.tasks.places.list.PlacesFragment
import com.elementary.tasks.places.list.PlacesScreen
import com.elementary.tasks.places.list.PlacesViewModel
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.fragment.toast
import com.google.android.gms.maps.model.LatLng
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Builds the Places island's [NavDisplay] — the "screens" (Nav3 entries) themselves and the
 * routing between them. [PlacesFragment] only owns the backstack and the Android-framework glue
 * (dialogs, the embedded classic map Fragment) that these entries react to.
 */
@Composable
internal fun PlacesFragment.PlacesNavGraph(backStack: MutableList<NavKey>) {
  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryDecorators =
      listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
    transitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_ENTER_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_EXIT_SCALE)
      )
    },
    popTransitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
      )
    },
    predictivePopTransitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
      )
    },
    entryProvider =
      entryProvider {
        entry<PlacesNavKey.List> { PlacesListEntry(backStack) }
        entry<PlacesNavKey.Edit> { key -> PlaceEditEntry(key, backStack) }
      },
  )
}

private fun navScreenSpring() =
  spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
  )

private const val NAV_ANIM_FADE_DURATION_MS = 250
private const val NAV_ANIM_ENTER_SCALE = 0.92f
private const val NAV_ANIM_EXIT_SCALE = 1.08f
private const val MAP_FRAGMENT_TAG = "edit_place_map"

@Composable
private fun PlacesFragment.PlacesListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<PlacesViewModel>()
  bindLifecycle(viewModel)
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is PlacesViewModel.NavigationEvent.OpenEditPlace -> backStack.add(PlacesNavKey.Edit(event.id))

      is PlacesViewModel.NavigationEvent.ShareFile -> {
        TelephonyUtil.sendFile(event.file, requireContext(), event.name)
      }

      is PlacesViewModel.NavigationEvent.ConfirmDelete -> {
        dialogues.askConfirmation(requireContext(), getString(R.string.delete)) { confirmed ->
          if (confirmed) viewModel.deletePlace(event.id)
        }
      }
    }
  }
  viewModel.errorEvent.ObserveEvent { toast(it) }

  val state by viewModel.screenState.collectAsState()
  PlacesScreen(
    state = state,
    onBackClick = { findNavController().popBackStack() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onAddClick = viewModel::onAddClick,
    onPlaceClick = viewModel::onPlaceClick,
    onPlaceMenuAction = viewModel::onPlaceMenuAction,
  )
}

@Composable
private fun PlacesFragment.PlaceEditEntry(
  key: PlacesNavKey.Edit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<EditPlaceViewModel> { parametersOf(key.id) }
  bindLifecycle(viewModel)

  var mapFragment by remember { mutableStateOf<SimpleMapFragment?>(null) }
  DisposableEffect(mapFragment) {
    activeGoogleMap = mapFragment
    onDispose { if (activeGoogleMap === mapFragment) activeGoogleMap = null }
  }

  LaunchedEffect(Unit) {
    if (arguments?.getBoolean(IntentKeys.INTENT_ITEM, false) == true) {
      viewModel.loadFromIntent()
    }
  }

  LaunchedEffect(mapFragment) {
    mapFragment?.mapCallback = mapCallbackFor(viewModel) { mapFragment }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      EditPlaceEvent.Saved, EditPlaceEvent.Deleted -> backStack.removeLastOrNull()

      EditPlaceEvent.NoLocationSelected -> toast(R.string.you_dont_select_place)

      EditPlaceEvent.ConfirmDelete -> {
        dialogues.askConfirmation(requireContext(), getString(R.string.delete)) { confirmed ->
          if (confirmed) viewModel.deletePlace()
        }
      }

      EditPlaceEvent.AskCopySaving -> {
        dialogues
          .getMaterialDialog(requireContext())
          .setMessage(R.string.same_place_message)
          .setPositiveButton(R.string.keep) { dialog, _ ->
            dialog.dismiss()
            viewModel.savePlace(newId = true)
          }.setNegativeButton(R.string.replace) { dialog, _ ->
            dialog.dismiss()
            viewModel.savePlace()
          }.setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
          .create()
          .show()
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

      if (viewModel.state.value.name.isEmpty()) {
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
private fun PlacesFragment.EmbeddedMap(
  onReady: (SimpleMapFragment) -> Unit,
  modifier: Modifier = Modifier,
) {
  val hostFragment = this
  // Scoped to this specific composition of the Edit entry (not to childFragmentManager as a
  // whole) — a leftover SimpleMapFragment from a *previous* visit to this screen can still be
  // registered under MAP_FRAGMENT_TAG when this composable is entered again, but its view was
  // hosted in a FragmentContainerView that Compose has since disposed. Re-using that stale
  // fragment instead of replacing it left the new container empty ("map is empty the second
  // time"), so every fresh entry into this screen must always run its own replace() — which
  // also correctly detaches whatever fragment occupied the container before.
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
      hostFragment.childFragmentManager
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
