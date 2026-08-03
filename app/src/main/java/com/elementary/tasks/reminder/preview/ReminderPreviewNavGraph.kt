package com.elementary.tasks.reminder.preview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.ads.AdBanner
import com.elementary.tasks.ads.NormalAdBanner
import com.elementary.tasks.core.data.ui.reminder.UiReminderPlace
import com.elementary.tasks.googletasks.GoogleTasksNavKey
import com.elementary.tasks.navigation.nav3.rememberAppNavBridge
import com.elementary.tasks.notes.NotesNavKey
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.reminder.build.valuedialog.editor.ReminderMapMarker
import com.elementary.tasks.settings.rememberSendIntentResolver
import com.elementary.tasks.share.rememberFileIntentSender
import com.elementary.tasks.simplemap.MapCustomButton
import com.elementary.tasks.simplemap.MapParams
import com.elementary.tasks.simplemap.SimpleMapController
import com.elementary.tasks.simplemap.SimpleMapView
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.ui.common.compose.foundation.dialog.rememberListDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.google.android.gms.maps.model.LatLng
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.reminderPreviewEntries(backStack: MutableList<NavKey>) {
  entry<ReminderPreviewNavKey.Preview> { key -> PreviewEntry(key, backStack) }
  entry<ReminderPreviewNavKey.FullscreenMap> { key -> FullscreenMapEntry(key, backStack) }
}

@Composable
private fun PreviewEntry(
  key: ReminderPreviewNavKey.Preview,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<PreviewReminderViewModel> { parametersOf(key.id) }

  val listDialogDispatcher = rememberListDialogDispatcher()
  val appNavBridge = rememberAppNavBridge()
  val toastDispatcher = rememberToastDispatcher()
  val fileIntentSender = rememberFileIntentSender()
  val intentResolver = rememberSendIntentResolver()

  // ReminderActionActivity (the full-screen alarm popup) is a separate Activity launched on
  // top of this screen, so the composable is never disposed and the state flow's
  // WhileSubscribed subscription never restarts. Reload explicitly on every ON_RESUME so
  // snoozing/completing/deleting from the popup is reflected when we come back.
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(viewModel, lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          viewModel.refresh()
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  viewModel.event.ObserveEvent { event ->
    when (event) {
      PreviewReminderViewModel.ViewModelEvent.MoveBack -> {
        backStack.removeLastOrNull()
      }

      is PreviewReminderViewModel.ViewModelEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }

      is PreviewReminderViewModel.ViewModelEvent.ShareData -> {
        fileIntentSender.send(event.title, event.file)
      }

      is PreviewReminderViewModel.ViewModelEvent.OpenCalendar -> {
        intentResolver.resolve(event.intent, event.title)
      }

      is PreviewReminderViewModel.ViewModelEvent.ShowCopyTimeDialog -> {
        listDialogDispatcher.showDialog(
          titleRes = R.string.choose_time,
          items = event.titles,
          onItemClick = { which -> viewModel.copyReminder(event.times[which]) },
        )
      }
    }
  }

  val state by viewModel.state.collectAsState(PreviewReminderState())
  PreviewReminderScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onToggleClick = viewModel::onToggleClick,
    onEditClick = { appNavBridge.navigate(BuildReminderNavKey.Main(id = key.id)) },
    onShareClick = viewModel::shareReminder,
    onCopyClick = viewModel::onCopyClicked,
    onDeleteClick = viewModel::onDeleteClick,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    onSubTaskCheck = { viewModel.onSubTaskChecked(it) },
    onSubTaskRemove = { viewModel.onSubTaskRemoved(it) },
    onNoteClick = {
      val noteId = state.note?.id
      if (noteId != null) appNavBridge.navigate(NotesNavKey.List, NotesNavKey.Preview(noteId))
    },
    onGoogleTaskClick = {
      val taskId = state.googleTask?.id
      if (taskId != null) appNavBridge.navigate(GoogleTasksNavKey.List, GoogleTasksNavKey.TaskEdit(id = taskId))
    },
    onCalendarOpenClick = { viewModel.onOpenCalendarClicked(it.id) },
    onCalendarRemoveClick = { viewModel.deleteEvent(it) },
    mapContent = {
      EmbeddedMap(
        places = state.places,
        onMapClick = { backStack.add(ReminderPreviewNavKey.FullscreenMap(key.id)) },
      )
    },
    adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.ReminderPreview) },
  )
}

@Composable
private fun FullscreenMapEntry(
  key: ReminderPreviewNavKey.FullscreenMap,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<FullScreenMapViewModel> { parametersOf(key.id) }
  var mapController by remember { mutableStateOf<SimpleMapController?>(null) }

  BackHandler {
    if (mapController?.onBackPressed() != false) backStack.removeLastOrNull()
  }

  val reminder by viewModel.reminder.collectAsState()
  ReminderFullscreenMapScreen(
    isLoading = reminder == null,
    onMoveToPlaceClick = {
      val currentReminder = viewModel.reminder.value ?: return@ReminderFullscreenMapScreen
      if (currentReminder.places.isEmpty()) return@ReminderFullscreenMapScreen
      viewModel.placeIndex =
        if (viewModel.placeIndex < currentReminder.places.size - 1) viewModel.placeIndex + 1 else 0
      val place = currentReminder.places[viewModel.placeIndex]
      mapController?.moveCamera(LatLng(place.latitude, place.longitude))
    },
    mapContent = {
      reminder?.let {
        FullscreenEmbeddedMap(
          reminder = it,
          onBackClick = { backStack.removeLastOrNull() },
          onControllerReady = { mapController = it },
        )
      }
    },
  )
}

@Composable
private fun EmbeddedMap(
  places: List<UiReminderPlace>,
  onMapClick: () -> Unit,
) {
  SimpleMapView(
    mapParams = MapParams(
      isTouch = false,
      isSearch = false,
      isRadius = false,
      isPlaces = false,
      isStyles = false,
      isLayers = false,
    ),
    markers = places.map {
      ReminderMapMarker(
        latLng = it.latLng(),
        style = it.marker,
        radius = it.radius,
        title = it.address
      )
    },
    onMapClick = onMapClick,
    modifier = Modifier.fillMaxSize(),
  )
}

@Composable
private fun FullscreenEmbeddedMap(
  reminder: ReminderV2,
  onBackClick: () -> Unit,
  onControllerReady: (SimpleMapController) -> Unit,
) {
  SimpleMapView(
    mapParams = MapParams(
      isPlaces = false,
      isStyles = false,
      isRadius = false,
      isSearch = false,
      isTouch = false,
      customButtons = listOf(MapCustomButton(R.drawable.ic_builder_arrow_left, id = 0)),
    ),
    markers = reminder.places.map { place ->
      ReminderMapMarker(
        latLng = LatLng(place.latitude, place.longitude),
        style = place.marker,
        radius = place.radius,
        title = place.name.takeIf { it.isNotEmpty() }
          ?: place.address.takeIf { it.isNotEmpty() }
          ?: reminder.summary,
      )
    },
    onCustomButtonClick = { id -> if (id == 0) onBackClick() },
    onControllerReady = onControllerReady,
    edgeToEdge = true,
    modifier = Modifier.fillMaxSize(),
  )
}
