package com.elementary.tasks.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdaysNavKey
import com.elementary.tasks.calendar.monthview.CalendarNavKey
import com.elementary.tasks.core.os.compose.PermissionRationaleDialog
import com.elementary.tasks.core.os.compose.rememberPermissionRequester
import com.elementary.tasks.googletasks.GoogleTasksNavKey
import com.elementary.tasks.groups.GroupsNavKey
import com.elementary.tasks.home.eventsview.EventsScreen
import com.elementary.tasks.home.eventsview.EventsViewModel
import com.elementary.tasks.home.scheduleview.ScheduleHomeViewModel
import com.elementary.tasks.navigation.nav3.AppNavBridge
import com.elementary.tasks.notes.NotesNavKey
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.reminder.lists.removed.RemindersArchiveNavKey
import com.elementary.tasks.reminder.preview.ReminderPreviewNavKey
import com.elementary.tasks.settings.SettingsNavKey
import com.elementary.tasks.settings.export.ExportNavKey
import com.elementary.tasks.settings.other.OtherNavKey
import com.elementary.tasks.workflow.WorkflowNavKey
import com.github.naz013.common.Permissions
import com.github.naz013.ui.common.Dialogues
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Contributes the Home island's screen (Nav3 entry) into the app's single, shared
 * [androidx.navigation3.ui.NavDisplay] (see [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 * [HomeNavKey.Main] is the graph's own start destination - the `LegacyHomeNavKey`/`home_nav.xml`
 * shim no longer occupies that role once Home is promoted.
 */
fun EntryProviderScope<NavKey>.homeEntries(backStack: MutableList<NavKey>) {
  entry<HomeNavKey.Main> { HomeEntry(backStack) }
  entry<HomeNavKey.Events> { EventsEntry(backStack) }
}

@Composable
private fun HomeEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<ScheduleHomeViewModel>()
  bindLifecycle(viewModel)
  val context = LocalContext.current
  val appNavBridge = koinInject<AppNavBridge>()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is ScheduleHomeViewModel.NavigationEvent.OpenReminderDetails -> {
        backStack.add(ReminderPreviewNavKey.Preview(event.uuid))
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenBirthdayDetails -> {
        appNavBridge.navigate(BirthdaysNavKey.Preview(event.uuid))
      }

      is ScheduleHomeViewModel.NavigationEvent.ShowEventTypeSelection -> Unit

      is ScheduleHomeViewModel.NavigationEvent.OpenSettings -> {
        backStack.add(SettingsNavKey.Hub)
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenCreateReminder -> {
        appNavBridge.navigate(BuildReminderNavKey.Main())
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenCreateBirthday -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit())
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenCreateGoogleTask -> {
        appNavBridge.navigate(GoogleTasksNavKey.List, GoogleTasksNavKey.TaskEdit())
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenCalendar -> {
        backStack.add(CalendarNavKey.Month)
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenEvents -> {
        backStack.add(HomeNavKey.Events)
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenNotes -> {
        appNavBridge.navigate(NotesNavKey.List)
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenGoogleTasks -> {
        appNavBridge.navigate(GoogleTasksNavKey.List)
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenGroups -> {
        appNavBridge.navigate(GroupsNavKey.List)
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenWorkflowGallery -> {
        appNavBridge.navigate(WorkflowNavKey.Gallery)
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenPrivacy -> {
        backStack.add(OtherNavKey.PrivacyPolicy)
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenCloudDrives -> {
        backStack.add(ExportNavKey.CloudServices)
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenWhatsNew -> {
        backStack.add(OtherNavKey.WhatsNew)
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenCreateNote -> {
        appNavBridge.navigate(NotesNavKey.List, NotesNavKey.Edit())
      }
    }
  }

  val state by viewModel.homeScreenState.collectAsState()
  HomeScreen(
    modifier = Modifier.fillMaxSize(),
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
        onEventActionClick = { viewModel.onEventActionClicked(context, it) },
        onAddMenuItemClick = { viewModel.onEventTypeSelected(it) },
      )
    },
  )
}

@Composable
private fun EventsEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<EventsViewModel>()
  bindLifecycle(viewModel)
  val context = LocalContext.current
  val dialogues = koinInject<Dialogues>()
  val appNavBridge = koinInject<AppNavBridge>()
  val permissionRequester = rememberPermissionRequester()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is EventsViewModel.NavigationEvent.OpenReminderPreview -> {
        backStack.add(ReminderPreviewNavKey.Preview(event.id))
      }

      is EventsViewModel.NavigationEvent.OpenReminderEdit -> {
        appNavBridge.navigate(BuildReminderNavKey.Main(id = event.id))
      }

      EventsViewModel.NavigationEvent.OpenNewReminder -> {
        appNavBridge.navigate(BuildReminderNavKey.Main())
      }

      EventsViewModel.NavigationEvent.OpenNewShoppingReminder -> {
        appNavBridge.navigate(BuildReminderNavKey.Main(deepLinkTodo = true))
      }

      is EventsViewModel.NavigationEvent.OpenBirthdayPreview -> {
        appNavBridge.navigate(BirthdaysNavKey.Preview(event.id))
      }

      is EventsViewModel.NavigationEvent.OpenBirthdayEdit -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit(event.id))
      }

      EventsViewModel.NavigationEvent.OpenNewBirthday -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit())
      }

      EventsViewModel.NavigationEvent.OpenArchive -> {
        backStack.add(RemindersArchiveNavKey.List)
      }

      EventsViewModel.NavigationEvent.OpenGroups -> {
        appNavBridge.navigate(GroupsNavKey.List)
      }

      is EventsViewModel.NavigationEvent.RequestGpsPermission -> {
        permissionRequester.request(
          listOf(Permissions.FOREGROUND_SERVICE, Permissions.FOREGROUND_SERVICE_LOCATION),
          onGranted = { viewModel.toggleReminder(event.id) },
        )
      }

      is EventsViewModel.NavigationEvent.ConfirmArchiveReminder -> {
        dialogues.askConfirmation(context, context.getString(R.string.move_to_archive)) { confirmed ->
          if (confirmed) viewModel.moveReminderToArchive(event.id)
        }
      }

      is EventsViewModel.NavigationEvent.ConfirmDeleteReminder -> {
        dialogues.askConfirmation(context, context.getString(R.string.delete)) { confirmed ->
          if (confirmed) viewModel.deleteReminder(event.id)
        }
      }

      is EventsViewModel.NavigationEvent.ConfirmDeleteBirthday -> {
        dialogues.askConfirmation(context, context.getString(R.string.delete)) { confirmed ->
          if (confirmed) viewModel.deleteBirthday(event.id)
        }
      }
    }
  }

  val state by viewModel.eventsScreenState.collectAsState()
  PermissionRationaleDialog(permissionRequester)
  EventsScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onCategoryToggle = viewModel::onCategoryToggle,
    onAddReminderClick = viewModel::onAddReminderClick,
    onAddShoppingClick = viewModel::onAddShoppingClick,
    onAddBirthdayClick = viewModel::onAddBirthdayClick,
    onArchiveClick = viewModel::onArchiveClick,
    onGroupsClick = viewModel::onGroupsClick,
    onItemClick = viewModel::onItemClick,
    onEventMenuAction = viewModel::onEventMenuAction,
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
