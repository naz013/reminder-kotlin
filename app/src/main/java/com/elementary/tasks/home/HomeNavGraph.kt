package com.elementary.tasks.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdaysNavKey
import com.elementary.tasks.calendar.monthview.CalendarNavKey
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.elementary.tasks.eventaction.rememberEventActionDispatcher
import com.github.naz013.feature.googletask.GoogleTasksNavKey
import com.elementary.tasks.groups.GroupsNavKey
import com.elementary.tasks.home.agenda.AgendaScreen
import com.elementary.tasks.home.agenda.AgendaScreenState
import com.elementary.tasks.home.agenda.AgendaViewModel
import com.elementary.tasks.home.scheduleview.ScheduleHomeViewModel
import com.elementary.tasks.navigation.nav3.rememberAppNavBridge
import com.elementary.tasks.notes.NotesNavKey
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.reminder.lists.removed.RemindersArchiveNavKey
import com.elementary.tasks.reminder.preview.ReminderPreviewNavKey
import com.elementary.tasks.reminder.todo.TodoEditNavKey
import com.elementary.tasks.settings.SettingsNavKey
import com.elementary.tasks.settings.export.ExportNavKey
import com.elementary.tasks.settings.other.OtherNavKey
import com.elementary.tasks.workflow.WorkflowNavKey
import com.github.naz013.common.Permissions
import com.github.naz013.tags.TagsNavKey
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.homeEntries(backStack: MutableList<NavKey>) {
  entry<HomeNavKey.Main> { HomeEntry(backStack) }
  entry<HomeNavKey.Agenda> { AgendaEntry(backStack) }
}

@Composable
private fun HomeEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<ScheduleHomeViewModel>()
  val appNavBridge = rememberAppNavBridge()
  val eventActionDispatcher = rememberEventActionDispatcher()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is ScheduleHomeViewModel.ViewModelEvent.OpenReminderDetails -> {
        backStack.add(ReminderPreviewNavKey.Preview(event.uuid))
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenBirthdayDetails -> {
        appNavBridge.navigate(BirthdaysNavKey.Preview(event.uuid))
      }

      is ScheduleHomeViewModel.ViewModelEvent.ShowEventTypeSelection -> Unit

      is ScheduleHomeViewModel.ViewModelEvent.OpenSettings -> {
        backStack.add(SettingsNavKey.Hub)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCreateReminder -> {
        appNavBridge.navigate(BuildReminderNavKey.Main())
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCreateBirthday -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit())
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCreateGoogleTask -> {
        appNavBridge.navigate(GoogleTasksNavKey.List, GoogleTasksNavKey.TaskEdit())
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCalendar -> {
        backStack.add(CalendarNavKey.Month)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenAgenda -> {
        backStack.add(HomeNavKey.Agenda)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenNotes -> {
        appNavBridge.navigate(NotesNavKey.List)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenGoogleTasks -> {
        appNavBridge.navigate(GoogleTasksNavKey.List)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenGroups -> {
        appNavBridge.navigate(GroupsNavKey.List)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenWorkflowGallery -> {
        appNavBridge.navigate(WorkflowNavKey.Gallery)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenPrivacy -> {
        backStack.add(OtherNavKey.PrivacyPolicy)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCloudDrives -> {
        backStack.add(ExportNavKey.CloudServices)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenWhatsNew -> {
        backStack.add(OtherNavKey.WhatsNew)
      }

      is ScheduleHomeViewModel.ViewModelEvent.OpenCreateNote -> {
        appNavBridge.navigate(NotesNavKey.List, NotesNavKey.Edit())
      }

      is ScheduleHomeViewModel.ViewModelEvent.EventAction -> {
        eventActionDispatcher.dispatch(event.value)
      }
    }
  }

  val state by viewModel.state.collectAsState(HomeScreenState())
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
        modifier =
          Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        onSettingsClick = { viewModel.onSettingsClicked() },
        onHeaderNavigationItemClick = { viewModel.onHeaderNavigationItemClicked(it) },
        onEventClick = { viewModel.onEventClicked(it) },
        onEventActionClick = { viewModel.onEventActionClicked(it) },
        onAddMenuItemClick = { viewModel.onEventTypeSelected(it) },
      )
    },
  )
}

@Composable
private fun AgendaEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<AgendaViewModel>()
  val appNavBridge = rememberAppNavBridge()
  val permissionRequester = rememberPermissionRequesterRationale()
  val dialogDispatcher = rememberDialogDispatcher()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is AgendaViewModel.NavigationEvent.OpenReminderPreview -> {
        backStack.add(ReminderPreviewNavKey.Preview(event.id))
      }

      is AgendaViewModel.NavigationEvent.OpenReminderEdit -> {
        appNavBridge.navigate(BuildReminderNavKey.Main(id = event.id))
      }

      AgendaViewModel.NavigationEvent.OpenNewReminder -> {
        appNavBridge.navigate(BuildReminderNavKey.Main())
      }

      AgendaViewModel.NavigationEvent.OpenNewTodo -> {
        appNavBridge.navigate(TodoEditNavKey.Main())
      }

      is AgendaViewModel.NavigationEvent.OpenBirthdayPreview -> {
        appNavBridge.navigate(BirthdaysNavKey.Preview(event.id))
      }

      is AgendaViewModel.NavigationEvent.OpenBirthdayEdit -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit(event.id))
      }

      AgendaViewModel.NavigationEvent.OpenNewBirthday -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit())
      }

      AgendaViewModel.NavigationEvent.OpenArchive -> {
        backStack.add(RemindersArchiveNavKey.List)
      }

      AgendaViewModel.NavigationEvent.OpenGroups -> {
        appNavBridge.navigate(GroupsNavKey.List)
      }

      AgendaViewModel.NavigationEvent.OpenTags -> {
        backStack.add(TagsNavKey.Manage)
      }

      is AgendaViewModel.NavigationEvent.RequestGpsPermission -> {
        permissionRequester.request(
          listOf(Permissions.FOREGROUND_SERVICE, Permissions.FOREGROUND_SERVICE_LOCATION),
          onGranted = { viewModel.toggleReminder(event.id) },
        )
      }

      is AgendaViewModel.NavigationEvent.ConfirmArchiveReminder -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.move_to_archive,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = {
            viewModel.moveReminderToArchive(event.id)
          }
        )
      }

      is AgendaViewModel.NavigationEvent.ConfirmDeleteReminder -> {
        dialogDispatcher.showDialog(
          textRes = R.string.delete_reminder_permanently,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = {
            viewModel.deleteReminder(event.id)
          }
        )
      }

      is AgendaViewModel.NavigationEvent.ConfirmDeleteBirthday -> {
        dialogDispatcher.showDialog(
          textRes = R.string.delete_birthday_permanently,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.deleteBirthday(event.id) }
        )
      }
    }
  }

  val state by viewModel.agendaScreenState.collectAsState(AgendaScreenState())
  AgendaScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onCategoryToggle = viewModel::onCategoryToggle,
    onSmartListSelected = viewModel::onSmartListSelected,
    onTagFilterSelected = viewModel::onTagFilterSelected,
    onGroupFilterSelected = viewModel::onGroupFilterSelected,
    onAddReminderClick = viewModel::onAddReminderClick,
    onAddTodoClick = viewModel::onAddTodoClick,
    onAddBirthdayClick = viewModel::onAddBirthdayClick,
    onArchiveClick = viewModel::onArchiveClick,
    onGroupsClick = viewModel::onGroupsClick,
    onTagsClick = viewModel::onTagsClick,
    onItemClick = viewModel::onItemClick,
    onAgendaMenuAction = viewModel::onAgendaMenuAction,
  )
}
