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
import com.elementary.tasks.googletasks.GoogleTasksNavKey
import com.elementary.tasks.groups.GroupsNavKey
import com.elementary.tasks.home.scheduleview.ScheduleHomeViewModel
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.nav3.AppNavBridge
import com.elementary.tasks.notes.NotesNavKey
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.reminder.preview.ReminderPreviewNavKey
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
        appNavBridge.navigateLegacy(R.id.settingsFragment, null, NavigationAnimations.inDepthNavOptions())
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
        appNavBridge.navigateLegacy(R.id.actionEvents, null, NavigationAnimations.inDepthNavOptions())
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

      is ScheduleHomeViewModel.NavigationEvent.OpenPrivacy -> {
        appNavBridge.navigateLegacy(R.id.privacyPolicyFragment, null, NavigationAnimations.modalNavOptions())
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenCloudDrives -> {
        appNavBridge.navigateLegacy(R.id.fragmentCloudDrives, null, NavigationAnimations.modalNavOptions())
      }

      is ScheduleHomeViewModel.NavigationEvent.OpenWhatsNew -> {
        appNavBridge.navigateLegacy(R.id.changesFragment, null, NavigationAnimations.modalNavOptions())
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
private fun bindLifecycle(observer: DefaultLifecycleObserver) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(observer, lifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
