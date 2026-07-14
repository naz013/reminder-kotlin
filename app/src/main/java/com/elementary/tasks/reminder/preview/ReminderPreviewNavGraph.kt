package com.elementary.tasks.reminder.preview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.compose.rememberDateTimeManager
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.reminder.UiReminderPlace
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.googletasks.GoogleTasksNavKey
import com.elementary.tasks.navigation.nav3.rememberAppNavBridge
import com.elementary.tasks.notes.NotesNavKey
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.notes.ObserveNonNull
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.simplemap.MapCallerEvent
import com.elementary.tasks.simplemap.MapCustomButton
import com.elementary.tasks.simplemap.MapParams
import com.elementary.tasks.simplemap.MapViewModel
import com.elementary.tasks.simplemap.MarkerState
import com.elementary.tasks.simplemap.SimpleMapView
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.ui.common.compose.foundation.dialog.ListDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.dialog.rememberListDialogDispatcher
import com.google.android.gms.maps.model.LatLng
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalTime

/**
 * Contributes the reminder preview + fullscreen-map screens (Nav3 entries) and the routing between
 * them into the app's single, shared [androidx.navigation3.ui.NavDisplay] (see
 * [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
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
  bindLifecycle(viewModel)

  val context = LocalContext.current
  val listDialogDispatcher = rememberListDialogDispatcher()
  val dateTimeManager = rememberDateTimeManager()
  val appNavBridge = rememberAppNavBridge()
  val adsProvider = remember { AdsProvider() }

  viewModel.resultEvent.ObserveEvent { commands ->
    when (commands) {
      Commands.DELETED -> backStack.removeLastOrNull()
      Commands.FAILED -> {
        Toast.makeText(context, context.getString(R.string.reminder_is_outdated), Toast.LENGTH_SHORT).show()
      }

      else -> {}
    }
  }
  viewModel.sharedFile.ObserveNonNull { TelephonyUtil.sendFile(context, it) }

  val state by viewModel.state.collectAsState()
  PreviewReminderScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onToggleClick = viewModel::onToggleClick,
    onEditClick = { appNavBridge.navigate(BuildReminderNavKey.Main(id = key.id)) },
    onShareClick = { viewModel.shareReminder() },
    onCopyClick = {
      showCopyTimeDialog(listDialogDispatcher, dateTimeManager) { time -> viewModel.copyReminder(time) }
    },
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
    onCalendarOpenClick = { openSystemCalendarEvent(context, it.id) },
    onCalendarRemoveClick = { viewModel.deleteEvent(it) },
    mapContent = {
      EmbeddedMap(
        places = state.places,
        onMapClick = { backStack.add(ReminderPreviewNavKey.FullscreenMap(key.id)) },
      )
    },
    adsContent = { ReminderAdBanner(adsProvider) },
  )
}

@Composable
private fun FullscreenMapEntry(
  key: ReminderPreviewNavKey.FullscreenMap,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<FullScreenMapViewModel> { parametersOf(key.id) }
  bindLifecycle(viewModel)

  val mapParams = remember {
    MapParams(
      isPlaces = false,
      isStyles = false,
      isRadius = false,
      isSearch = false,
      isTouch = false,
      customButtons = listOf(MapCustomButton(R.drawable.ic_builder_arrow_left, id = 0)),
    )
  }
  val mapViewModel = koinViewModel<MapViewModel> { parametersOf(mapParams) }

  mapViewModel.callerEvent.ObserveEvent { event ->
    if (event is MapCallerEvent.CustomButtonClicked && event.id == 0) {
      backStack.removeLastOrNull()
    }
  }

  BackHandler {
    if (mapViewModel.onBackPressed()) backStack.removeLastOrNull()
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
      mapViewModel.moveCamera(LatLng(place.latitude, place.longitude))
    },
    mapContent = {
      reminder?.let {
        FullscreenEmbeddedMap(viewModel = mapViewModel, reminder = it)
      }
    },
  )
}

@Composable
private fun EmbeddedMap(
  places: List<UiReminderPlace>,
  onMapClick: () -> Unit,
) {
  val mapParams = remember {
    MapParams(isTouch = false, isSearch = false, isRadius = false, isPlaces = false, isStyles = false, isLayers = false)
  }
  val mapViewModel = koinViewModel<MapViewModel> { parametersOf(mapParams) }

  mapViewModel.callerEvent.ObserveEvent { event ->
    if (event is MapCallerEvent.MapClicked) onMapClick()
  }

  SimpleMapView(
    viewModel = mapViewModel,
    markers = places.map {
      MarkerState(latLng = it.latLng(), style = it.marker, radius = it.radius, title = it.address)
    },
    modifier = Modifier.fillMaxSize(),
  )
}

@Composable
private fun FullscreenEmbeddedMap(
  viewModel: MapViewModel,
  reminder: Reminder,
) {
  SimpleMapView(
    viewModel = viewModel,
    markers = reminder.places.map { place ->
      MarkerState(
        latLng = LatLng(place.latitude, place.longitude),
        style = place.marker,
        radius = place.radius,
        title = place.name.takeIf { it.isNotEmpty() }
          ?: place.address.takeIf { it.isNotEmpty() }
          ?: reminder.summary,
      )
    },
    edgeToEdge = true,
    modifier = Modifier.fillMaxSize(),
  )
}

private fun openSystemCalendarEvent(
  context: Context,
  id: Long,
) {
  if (id <= 0L) return
  val uri = Uri.parse("content://com.android.calendar/events/$id")
  val intent = Intent(Intent.ACTION_VIEW, uri)
  runCatching { context.startActivity(intent) }
}

private fun showCopyTimeDialog(
  listDialogDispatcher: ListDialogDispatcher,
  dateTimeManager: DateTimeManager,
  onTimePicked: (LocalTime) -> Unit,
) {
  var time = LocalTime.of(0, 0)
  val list = mutableListOf<LocalTime>()
  val times = mutableListOf<String>()
  var isRunning = true
  do {
    if (time.hour == 23 && time.minute == 30) {
      isRunning = false
    } else {
      list.add(time)
      times.add(dateTimeManager.getTime(time))
      time = time.plusMinutes(30)
    }
  } while (isRunning)
  listDialogDispatcher.showDialog(
    titleRes = R.string.choose_time,
    items = times,
    onItemClick = { which -> onTimePicked(list[which]) },
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

@Composable
private fun ReminderAdBanner(adsProvider: AdsProvider) {
  if (BuildParams.isPro || !AdsProvider.hasAds()) return
  val context = LocalContext.current
  AndroidView(
    modifier = Modifier.fillMaxWidth(),
    factory = { FrameLayout(context) },
    update = { viewGroup ->
      adsProvider.showNativeBanner(viewGroup, AdsProvider.REMINDER_PREVIEW_BANNER_ID, R.layout.list_item_ads_hor)
    },
  )
}
