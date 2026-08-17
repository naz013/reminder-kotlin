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
import com.github.naz013.feature.agenda.AgendaNavKey
import com.github.naz013.feature.birthday.BirthdaysNavKey
import com.github.naz013.feature.calendar.monthview.CalendarNavKey
import com.elementary.tasks.eventaction.rememberEventActionDispatcher
import com.github.naz013.feature.googletask.GoogleTasksNavKey
import com.github.naz013.group.GroupsNavKey
import com.elementary.tasks.home.scheduleview.ScheduleHomeViewModel
import com.elementary.tasks.navigation.nav3.rememberAppNavBridge
import com.github.naz013.feature.note.NotesNavKey
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.feature.reminder.preview.ReminderPreviewNavKey
import com.github.naz013.feature.reminder.todo.TodoEditNavKey
import com.github.naz013.feature.settings.SettingsNavKey
import com.github.naz013.feature.settings.export.ExportNavKey
import com.github.naz013.feature.settings.other.OtherNavKey
import com.github.naz013.feature.workflow.WorkflowNavKey
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.homeEntries(backStack: MutableList<NavKey>) {
  entry<HomeNavKey.Main> { HomeEntry(backStack) }
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
        backStack.add(AgendaNavKey.List)
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

      is ScheduleHomeViewModel.ViewModelEvent.OpenCreateTodo -> {
        appNavBridge.navigate(TodoEditNavKey.Main())
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
