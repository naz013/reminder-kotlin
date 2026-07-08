package com.elementary.tasks.reminder.preview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.reminder.UiReminderPlace
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.googletasks.GoogleTasksFragment
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.composeView
import com.github.naz013.ui.common.fragment.toast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalTime

class PreviewReminderFragment : Fragment() {
  private val id: String by lazy { arguments?.getString(IntentKeys.INTENT_ID) ?: "" }
  private val viewModel by viewModel<PreviewReminderViewModel> { parametersOf(id) }
  private val dialogues by inject<Dialogues>()
  private val dateTimeManager by inject<DateTimeManager>()
  private val prefs by inject<Prefs>()
  private val adsProvider = AdsProvider()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the reminder preview screen for id: $id")
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View =
    composeView {
      val state by viewModel.state.collectAsState()
      PreviewReminderScreen(
        state = state,
        onBackClick = { requireActivity().onBackPressedDispatcher.onBackPressed() },
        onToggleClick = viewModel::onToggleClick,
        onEditClick = { editReminder() },
        onShareClick = { viewModel.shareReminder() },
        onCopyClick = { showCopyTimeDialog() },
        onDeleteClick = viewModel::onDeleteClick,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onSubTaskCheck = { viewModel.onSubTaskChecked(it) },
        onSubTaskRemove = { viewModel.onSubTaskRemoved(it) },
        onNoteClick = { openNote() },
        onGoogleTaskClick = { openGoogleTask() },
        onCalendarOpenClick = { openCalendar(it.id) },
        onCalendarRemoveClick = { viewModel.deleteEvent(it) },
        mapContent = { EmbeddedMap(places = state.places) },
        adsContent = { ReminderAdBanner() },
      )
    }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    lifecycle.addObserver(viewModel)
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) { commands ->
      when (commands) {
        Commands.DELETED -> requireActivity().onBackPressedDispatcher.onBackPressed()
        Commands.FAILED -> toast(getString(R.string.reminder_is_outdated))
        else -> {}
      }
    }
    viewModel.sharedFile.nonNullObserve(viewLifecycleOwner) {
      TelephonyUtil.sendFile(requireContext(), it)
    }
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  /** The embedded [SimpleMapFragment] is added to [childFragmentManager] once and left there —
   *  when this fragment's own view is torn down (e.g. navigating to the fullscreen map and back),
   *  the child fragment survives but its container [FragmentContainerView] does not. Without this,
   *  the child FragmentManager tries to restore that orphaned fragment into a container id that
   *  Compose hasn't recreated yet on the next [onCreateView], crashing with
   *  "No view found for id ... for fragment SimpleMapFragment". Explicitly removing it here lets
   *  [EmbeddedMap] create a fresh instance on the next composition instead. */
  override fun onDestroyView() {
    childFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG)?.let { mapFragment ->
      childFragmentManager.beginTransaction().remove(mapFragment).commitNowAllowingStateLoss()
    }
    super.onDestroyView()
  }

  private fun editReminder() {
    safeNavigation(
      R.id.buildReminderFragment,
      Bundle().apply { putString(IntentKeys.INTENT_ID, id) },
      NavigationAnimations.inDepthNavOptions(),
    )
  }

  private fun openNote() {
    val state = viewModel.state.value
    val noteId = state.note?.id ?: return
    safeNavigation(
      R.id.actionNotes,
      Bundle().apply { putString(IntentKeys.INTENT_ID, noteId) },
      NavigationAnimations.inDepthNavOptions(),
    )
  }

  private fun openGoogleTask() {
    val state = viewModel.state.value
    val taskId = state.googleTask?.id ?: return
    safeNavigation(
      R.id.actionGoogle,
      Bundle().apply {
        putString(IntentKeys.INTENT_ID, taskId)
        putBoolean(GoogleTasksFragment.ARG_OPEN_EDIT, true)
      },
      NavigationAnimations.inDepthNavOptions(),
    )
  }

  private fun openCalendar(id: Long) {
    if (id <= 0L) return
    val uri = Uri.parse("content://com.android.calendar/events/$id")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    runCatching { startActivity(intent) }
  }

  private fun openFullMap() {
    safeNavigation(
      R.id.reminderFullscreenMapFragment,
      Bundle().apply { putString(IntentKeys.INTENT_ID, id) },
      NavigationAnimations.inDepthNavOptions(),
    )
  }

  private fun showCopyTimeDialog() {
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
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(R.string.choose_time)
    builder.setItems(times.toTypedArray()) { dialog, which ->
      dialog.dismiss()
      viewModel.copyReminder(list[which])
    }
    builder.create().show()
  }

  @Composable
  private fun EmbeddedMap(places: List<UiReminderPlace>) {
    val hostFragment = this
    var attached by remember { mutableStateOf(false) }
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = { context ->
        FragmentContainerView(context).apply {
          id = R.id.reminder_preview_map_container
          layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
      },
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
              simpleMapFragment.setOnMapClickListener { openFullMap() }
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
        hostFragment.childFragmentManager
          .beginTransaction()
          .replace(R.id.reminder_preview_map_container, simpleMapFragment, MAP_FRAGMENT_TAG)
          .commitNow()
      },
    )
  }

  @Composable
  private fun ReminderAdBanner() {
    if (BuildParams.isPro || !AdsProvider.hasAds()) return
    val context = LocalContext.current
    AndroidView(
      modifier = Modifier.fillMaxWidth(),
      factory = { FrameLayout(context) },
      update = { viewGroup ->
        adsProvider.showNativeBanner(
          viewGroup,
          AdsProvider.REMINDER_PREVIEW_BANNER_ID,
          R.layout.list_item_ads_hor,
        )
      },
    )
  }

  companion object {
    private const val TAG = "PreviewReminderFragment"
    private const val MAP_FRAGMENT_TAG = "reminder_preview_map"
  }
}
