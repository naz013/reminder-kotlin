package com.github.naz013.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.home.dayoverview.DayOverviewPane
import com.github.naz013.feature.home.scheduleview.ScheduleHomeViewModel
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberUndoSnackbarDispatcher
import com.github.naz013.ui.common.livedata.ObserveEvent
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.homeEntries(
  backStack: MutableList<NavKey>,
  selectedEventId: String?,
  onOpenReminderPreview: (id: String) -> Unit,
  onOpenBirthdayPreview: (id: String) -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHeaderItemsSettings: () -> Unit,
  onOpenCreateReminder: () -> Unit,
  onOpenCreateBirthday: () -> Unit,
  onOpenCreateGoogleTask: () -> Unit,
  onOpenCalendar: () -> Unit,
  onOpenAgenda: () -> Unit,
  onOpenNotes: () -> Unit,
  onOpenBirthdays: () -> Unit,
  onOpenGoogleTasks: () -> Unit,
  onOpenGroups: () -> Unit,
  onOpenTags: () -> Unit,
  onOpenRoutines: () -> Unit,
  onOpenHabits: () -> Unit,
  onOpenWorkflowGallery: () -> Unit,
  onOpenPomodoro: () -> Unit,
  onOpenQuickAdd: () -> Unit,
  onOpenProVersion: () -> Unit,
  onOpenPrivacyPolicy: () -> Unit,
  onOpenCloudDrives: () -> Unit,
  onOpenWhatsNew: () -> Unit,
  onOpenCreateNote: () -> Unit,
  onOpenCreateTodo: () -> Unit,
  onEventAction: (ResolvedEventAction) -> Unit,
) {
  entry<HomeNavKey.Main>(
    metadata = ListDetailSceneStrategy.listPane(
      detailPlaceholder = { DayOverviewPane() },
    ),
  ) {
    HomeEntry(
      selectedEventId = selectedEventId,
      onOpenReminderPreview = onOpenReminderPreview,
      onOpenBirthdayPreview = onOpenBirthdayPreview,
      onOpenSettings = onOpenSettings,
      onOpenHeaderItemsSettings = onOpenHeaderItemsSettings,
      onOpenCreateReminder = onOpenCreateReminder,
      onOpenCreateBirthday = onOpenCreateBirthday,
      onOpenCreateGoogleTask = onOpenCreateGoogleTask,
      onOpenCalendar = onOpenCalendar,
      onOpenAgenda = onOpenAgenda,
      onOpenNotes = onOpenNotes,
      onOpenBirthdays = onOpenBirthdays,
      onOpenGoogleTasks = onOpenGoogleTasks,
      onOpenGroups = onOpenGroups,
      onOpenTags = onOpenTags,
      onOpenRoutines = onOpenRoutines,
      onOpenHabits = onOpenHabits,
      onOpenWorkflowGallery = onOpenWorkflowGallery,
      onOpenPomodoro = onOpenPomodoro,
      onOpenQuickAdd = onOpenQuickAdd,
      onOpenProVersion = onOpenProVersion,
      onOpenPrivacyPolicy = onOpenPrivacyPolicy,
      onOpenCloudDrives = onOpenCloudDrives,
      onOpenWhatsNew = onOpenWhatsNew,
      onOpenCreateNote = onOpenCreateNote,
      onOpenCreateTodo = onOpenCreateTodo,
      onEventAction = onEventAction,
    )
  }
}

@Composable
private fun HomeEntry(
  selectedEventId: String?,
  onOpenReminderPreview: (id: String) -> Unit,
  onOpenBirthdayPreview: (id: String) -> Unit,
  onOpenSettings: () -> Unit,
  onOpenHeaderItemsSettings: () -> Unit,
  onOpenCreateReminder: () -> Unit,
  onOpenCreateBirthday: () -> Unit,
  onOpenCreateGoogleTask: () -> Unit,
  onOpenCalendar: () -> Unit,
  onOpenAgenda: () -> Unit,
  onOpenNotes: () -> Unit,
  onOpenBirthdays: () -> Unit,
  onOpenGoogleTasks: () -> Unit,
  onOpenGroups: () -> Unit,
  onOpenTags: () -> Unit,
  onOpenRoutines: () -> Unit,
  onOpenHabits: () -> Unit,
  onOpenWorkflowGallery: () -> Unit,
  onOpenPomodoro: () -> Unit,
  onOpenQuickAdd: () -> Unit,
  onOpenProVersion: () -> Unit,
  onOpenPrivacyPolicy: () -> Unit,
  onOpenCloudDrives: () -> Unit,
  onOpenWhatsNew: () -> Unit,
  onOpenCreateNote: () -> Unit,
  onOpenCreateTodo: () -> Unit,
  onEventAction: (ResolvedEventAction) -> Unit,
) {
  val viewModel = koinViewModel<ScheduleHomeViewModel>()
  val snackbarHostState = remember { SnackbarHostState() }
  val undoSnackbarDispatcher = rememberUndoSnackbarDispatcher(snackbarHostState)
  val snackbarScope = rememberCoroutineScope()
  val undoLabel = stringResource(R.string.undo)

  LaunchedEffect(selectedEventId) { viewModel.onSelectedEventIdChanged(selectedEventId) }

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is ScheduleHomeViewModel.ViewModelEvent.OpenReminderDetails -> {
        onOpenReminderPreview(event.uuid)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenBirthdayDetails -> {
        onOpenBirthdayPreview(event.uuid)
      }

      is ScheduleHomeViewModel.ViewModelEvent.ShowEventTypeSelection -> Unit

      is ScheduleHomeViewModel.ViewModelEvent.OpenSettings -> {
        onOpenSettings()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenHeaderItemsSettings -> {
        onOpenHeaderItemsSettings()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCreateReminder -> {
        onOpenCreateReminder()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCreateBirthday -> {
        onOpenCreateBirthday()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCreateGoogleTask -> {
        onOpenCreateGoogleTask()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCalendar -> {
        onOpenCalendar()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenAgenda -> {
        onOpenAgenda()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenNotes -> {
        onOpenNotes()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenBirthdays -> {
        onOpenBirthdays()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenGoogleTasks -> {
        onOpenGoogleTasks()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenGroups -> {
        onOpenGroups()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenTags -> {
        onOpenTags()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenRoutines -> {
        onOpenRoutines()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenHabits -> {
        onOpenHabits()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenWorkflowGallery -> {
        onOpenWorkflowGallery()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenPomodoro -> {
        onOpenPomodoro()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenQuickAdd -> {
        onOpenQuickAdd()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenProVersion -> {
        onOpenProVersion()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenPrivacy -> {
        onOpenPrivacyPolicy()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCloudDrives -> {
        onOpenCloudDrives()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenWhatsNew -> {
        onOpenWhatsNew()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCreateNote -> {
        onOpenCreateNote()
      }

      is ScheduleHomeViewModel.ViewModelEvent.EventAction -> {
        onEventAction(event.value)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCreateTodo -> {
        onOpenCreateTodo()
      }

      is ScheduleHomeViewModel.ViewModelEvent.ShowRescheduleResult -> {
        if (event.canUndo) {
          undoSnackbarDispatcher.showUndoSnackbar(
            message = event.message,
            actionLabel = undoLabel,
            onUndo = { viewModel.onRescheduleUndoClicked() },
            onTimeout = {},
          )
        } else {
          snackbarScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message = event.message, duration = SnackbarDuration.Short)
          }
        }
      }
    }
  }

  val state by viewModel.state.collectAsState(HomeScreenState())
  // The persistent nav rail on Medium+ width is applied around this entry by
  // PersistentNavRailSceneDecoratorStrategy (registered in AppNavGraph), not here -
  // ChronologicalHomeScreen decides on its own whether to still show the header grid/row.
  HomeScreen(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    bannerState = state.bannerState,
    onPrivacyPolicyClick = { viewModel.onPrivacyPolicyClick() },
    onPrivacyAcceptClick = { viewModel.onPrivacyAcceptClick() },
    onLoginDismissClick = { viewModel.onLoginDismissClick() },
    onLoginClick = { viewModel.onLoginClick() },
    onWhatsNewDetailsClick = { viewModel.onWhatsNewDetailsClick() },
    onWhatsNewDismissClick = { viewModel.onWhatsNewDismissClick() },
    content = {
      ChronologicalHomeScreen(
        state = state,
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding(),
        onSettingsClick = { viewModel.onSettingsClicked() },
        onHeaderNavigationItemClick = { viewModel.onHeaderNavigationItemClicked(it) },
        onHeaderNavigationItemLongClick = { viewModel.onHeaderNavigationItemLongClicked() },
        onEventClick = { viewModel.onEventClicked(it) },
        onEventActionClick = { viewModel.onEventActionClicked(it) },
        onEarlierTodayClick = { viewModel.onEarlierTodayToggled() },
        onOverdueClick = { viewModel.onOverdueToggled() },
        onRescheduleAllClick = { viewModel.onRescheduleAllClicked() },
        onRescheduleDismiss = { viewModel.onRescheduleDismissed() },
        onRescheduleTodayClick = { viewModel.onRescheduleTodayClicked() },
        onRescheduleTomorrowClick = { viewModel.onRescheduleTomorrowClicked() },
        onReschedulePicked = { viewModel.onReschedulePicked(it) },
        onAddMenuItemClick = { viewModel.onEventTypeSelected(it) },
      )
      SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter),
      )
    },
  )
}
