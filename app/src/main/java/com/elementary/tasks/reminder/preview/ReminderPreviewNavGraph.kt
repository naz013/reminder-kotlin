package com.elementary.tasks.reminder.preview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.reminder.UiReminderPlace
import com.elementary.tasks.core.compose.rememberDateTimeManager
import com.elementary.tasks.core.compose.rememberPrefs
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.googletasks.GoogleTasksNavKey
import com.elementary.tasks.navigation.nav3.rememberAppNavBridge
import com.elementary.tasks.notes.NotesNavKey
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.notes.ObserveNonNull
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.simplemap.SimpleMapFragment
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
  val activity = LocalActivity.current as FragmentActivity
  val listDialogDispatcher = rememberListDialogDispatcher()
  val dateTimeManager = rememberDateTimeManager()
  val prefs = rememberPrefs()
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
        prefs = prefs,
        fragmentManager = activity.supportFragmentManager,
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
  val activity = LocalActivity.current as FragmentActivity
  var simpleMapFragment by remember { mutableStateOf<SimpleMapFragment?>(null) }

  BackHandler {
    val canPopNormally = simpleMapFragment?.onBackPressed() ?: true
    if (canPopNormally) backStack.removeLastOrNull()
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
      simpleMapFragment?.moveCamera(pos = LatLng(place.latitude, place.longitude))
    },
    mapContent = {
      reminder?.let {
        FullscreenEmbeddedMap(
          reminder = it,
          fragmentManager = activity.supportFragmentManager,
          onBackClick = { backStack.removeLastOrNull() },
          onMapFragmentReady = { simpleMapFragment = it },
        )
      }
    },
  )
}

@Composable
private fun EmbeddedMap(
  places: List<UiReminderPlace>,
  prefs: Prefs,
  fragmentManager: FragmentManager,
  onMapClick: () -> Unit,
) {
  val containerId = remember { View.generateViewId() }
  var attached by remember { mutableStateOf(false) }
  AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { context -> FragmentContainerView(context).apply { id = containerId } },
    update = {
      if (attached) return@AndroidView
      attached = true
      val simpleMapFragment =
        SimpleMapFragment.newInstance(
          SimpleMapFragment.MapParams(
            isTouch = false,
            isSearch = false,
            isRadius = false,
            isPlaces = false,
            isStyles = false,
            isLayers = false,
            mapStyleParams =
              SimpleMapFragment.MapStyleParams(
                mapType = prefs.mapType,
                mapStyle = prefs.mapStyle,
              ),
          ),
        )
      simpleMapFragment.mapCallback =
        object : SimpleMapFragment.DefaultMapCallback() {
          override fun onMapReady() {
            simpleMapFragment.setOnMapClickListener { onMapClick() }
            places.forEach { place ->
              simpleMapFragment.addMarker(
                latLng = place.latLng(),
                title = place.address,
                markerStyle = place.marker,
                radius = place.radius,
                clear = false,
                animate = false,
              )
            }
            places.firstOrNull()?.run {
              simpleMapFragment.moveCamera(latLng(), 0, 0, 0, 0)
            }
          }
        }
      fragmentManager
        .beginTransaction()
        .replace(containerId, simpleMapFragment, MAP_FRAGMENT_TAG)
        .commitNowAllowingStateLoss()
    },
  )
  DisposableEffect(Unit) {
    onDispose {
      if (!fragmentManager.isDestroyed) {
        fragmentManager.findFragmentById(containerId)?.also { existing ->
          fragmentManager.beginTransaction().remove(existing).commitNowAllowingStateLoss()
        }
      }
    }
  }
}

@Composable
private fun FullscreenEmbeddedMap(
  reminder: Reminder,
  fragmentManager: FragmentManager,
  onBackClick: () -> Unit,
  onMapFragmentReady: (SimpleMapFragment) -> Unit,
) {
  val containerId = remember { View.generateViewId() }
  var attached by remember { mutableStateOf(false) }
  AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { context -> FragmentContainerView(context).apply { id = containerId } },
    update = {
      if (attached) return@AndroidView
      attached = true
      val googleMap =
        SimpleMapFragment.newInstance(
          SimpleMapFragment.MapParams(
            isPlaces = false,
            isStyles = false,
            isRadius = false,
            isSearch = false,
            isTouch = false,
            customButtons = listOf(SimpleMapFragment.MapCustomButton(R.drawable.ic_builder_arrow_left, 0)),
          ),
        )
      googleMap.customButtonCallback =
        object : SimpleMapFragment.CustomButtonCallback {
          override fun onButtonClicked(buttonId: Int) = onBackClick()
        }
      googleMap.mapCallback =
        object : SimpleMapFragment.DefaultMapCallback() {
          override fun onMapReady() {
            googleMap.applyInsets()
            reminder.places.forEach { place ->
              googleMap.addMarker(
                latLng = LatLng(place.latitude, place.longitude),
                title =
                  place.name.takeIf { it.isNotEmpty() }
                    ?: place.address.takeIf { it.isNotEmpty() }
                    ?: reminder.summary,
                markerStyle = place.marker,
                radius = place.radius,
                clear = false,
                animate = false,
              )
            }
            reminder.places
              .firstOrNull()
              ?.let { LatLng(it.latitude, it.longitude) }
              ?.run { googleMap.moveCamera(this) }
          }
        }
      fragmentManager
        .beginTransaction()
        .replace(containerId, googleMap, MAP_FRAGMENT_TAG)
        .commitNowAllowingStateLoss()
      onMapFragmentReady(googleMap)
    },
  )
  DisposableEffect(Unit) {
    onDispose {
      if (!fragmentManager.isDestroyed) {
        fragmentManager.findFragmentById(containerId)?.also { existing ->
          fragmentManager.beginTransaction().remove(existing).commitNowAllowingStateLoss()
        }
      }
    }
  }
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

private const val MAP_FRAGMENT_TAG = "reminder_preview_map"
