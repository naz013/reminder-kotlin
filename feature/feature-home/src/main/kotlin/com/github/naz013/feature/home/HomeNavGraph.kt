package com.github.naz013.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.home.scheduleview.ScheduleHomeViewModel
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.navigation.DetailPanePlaceholder
import com.github.naz013.ui.common.livedata.ObserveEvent
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
  onOpenRoutines: () -> Unit,
  onOpenWorkflowGallery: () -> Unit,
  onOpenPrivacyPolicy: () -> Unit,
  onOpenCloudDrives: () -> Unit,
  onOpenWhatsNew: () -> Unit,
  onOpenCreateNote: () -> Unit,
  onOpenCreateTodo: () -> Unit,
  onEventAction: (ResolvedEventAction) -> Unit,
) {
  entry<HomeNavKey.Main>(
    metadata = ListDetailSceneStrategy.listPane(
      detailPlaceholder = {
        DetailPanePlaceholder(
          text = stringResource(R.string.select_reminder_or_birthday_to_see_details),
          icon = AppIcons.Fluent.Calendar,
        )
      },
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
      onOpenRoutines = onOpenRoutines,
      onOpenWorkflowGallery = onOpenWorkflowGallery,
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
  onOpenRoutines: () -> Unit,
  onOpenWorkflowGallery: () -> Unit,
  onOpenPrivacyPolicy: () -> Unit,
  onOpenCloudDrives: () -> Unit,
  onOpenWhatsNew: () -> Unit,
  onOpenCreateNote: () -> Unit,
  onOpenCreateTodo: () -> Unit,
  onEventAction: (ResolvedEventAction) -> Unit,
) {
  val viewModel = koinViewModel<ScheduleHomeViewModel>()

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

      is ScheduleHomeViewModel.ViewModelEvent.OpenRoutines -> {
        onOpenRoutines()
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenWorkflowGallery -> {
        onOpenWorkflowGallery()
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
        onAddMenuItemClick = { viewModel.onEventTypeSelected(it) },
      )
    },
  )
}
