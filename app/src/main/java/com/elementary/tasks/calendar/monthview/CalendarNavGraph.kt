package com.elementary.tasks.calendar.monthview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdaysNavKey
import com.elementary.tasks.calendar.dayview.WeekViewScreen
import com.elementary.tasks.calendar.dayview.WeekViewViewModel
import com.elementary.tasks.core.os.compose.rememberPermissionRequesterRationale
import com.elementary.tasks.navigation.nav3.AppNavBridge
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.reminder.preview.ReminderPreviewNavKey
import com.elementary.tasks.settings.SettingsNavKey
import com.github.naz013.common.Permissions
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.ui.common.Dialogues
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Contributes the Calendar island's screens (Nav3 entries) - month grid and single-day/week view -
 * and the routing between them into the app's single, shared [androidx.navigation3.ui.NavDisplay]
 * (see [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
fun EntryProviderScope<NavKey>.calendarEntries(backStack: MutableList<NavKey>) {
  entry<CalendarNavKey.Month> { MonthEntry(backStack) }
  entry<CalendarNavKey.Day> { key -> DayEntry(key, backStack) }
}

@Composable
private fun MonthEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<CalendarViewModel>()
  val dateTimeManager = koinInject<DateTimeManager>()
  val appNavBridge = koinInject<AppNavBridge>()
  val settingsTitle = stringResource(R.string.action_settings)

  var pagerJumpRequest by remember { mutableStateOf<Int?>(null) }

  // CalendarViewModel is a plain ViewModel (not a lifecycle observer) - resetToToday()/refresh()
  // must be driven explicitly on every ON_RESUME, matching the old CalendarFragment.onResume().
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(viewModel, lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          pagerJumpRequest = CalendarViewModel.CENTER_POSITION
          viewModel.resetToToday()
          viewModel.refresh()
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is CalendarViewModel.NavigationEvent.OpenDayView -> {
        backStack.add(
          CalendarNavKey.Day(dateTimeManager.toMillis(LocalDateTime.of(event.date, LocalTime.now()))),
        )
      }

      is CalendarViewModel.NavigationEvent.OpenNewReminder -> {
        appNavBridge.navigate(
          BuildReminderNavKey.Main(
            deepLinkDateTimeType = Reminder.BY_DATE,
            deepLinkDateTimeMillis = dateTimeManager.toMillis(LocalDateTime.of(event.date, LocalTime.now())),
          ),
        )
      }

      is CalendarViewModel.NavigationEvent.OpenNewBirthday -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit(prefillDateEpochDay = event.date.toEpochDay()))
      }

      CalendarViewModel.NavigationEvent.OpenSettings -> {
        backStack.add(SettingsNavKey.Calendar(settingsTitle))
      }
    }
  }

  val state by viewModel.state.collectAsState()
  val refreshSignal by viewModel.refreshSignal.collectAsState()
  CalendarScreen(
    state = state,
    initialPagerPosition = viewModel.lastPosition,
    pagerJumpRequest = pagerJumpRequest,
    onPagerJumpConsumed = { pagerJumpRequest = null },
    monthForPosition = viewModel::monthForPosition,
    onPageSettled = { position ->
      viewModel.updateLastPosition(position)
      viewModel.onPageSettled(position)
    },
    buildGrid = viewModel::buildGrid,
    refreshSignal = refreshSignal,
    loadMonthEvents = viewModel::loadMonthEvents,
    onDayClick = viewModel::onDayClick,
    onAddReminderClick = viewModel::onAddReminderClick,
    onAddBirthdayClick = viewModel::onAddBirthdayClick,
    onSettingsClick = viewModel::onSettingsClick,
    onBackClick = { backStack.removeLastOrNull() },
  )
}

@Composable
private fun DayEntry(
  key: CalendarNavKey.Day,
  backStack: MutableList<NavKey>,
) {
  val dateTimeManager = koinInject<DateTimeManager>()
  val startDate = remember(key.dateMillis) { dateTimeManager.fromMillis(key.dateMillis).toLocalDate() }
  val viewModel = koinViewModel<WeekViewViewModel> { parametersOf(startDate) }
  bindLifecycle(viewModel)

  val context = LocalContext.current
  val dialogues = koinInject<Dialogues>()
  val appNavBridge = koinInject<AppNavBridge>()
  val permissionRequester = rememberPermissionRequesterRationale()

  var pagerJumpRequest by remember { mutableStateOf<Int?>(null) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is WeekViewViewModel.NavigationEvent.MoveToDate -> {
        pagerJumpRequest = viewModel.positionForDate(event.date)
      }

      is WeekViewViewModel.NavigationEvent.OpenReminderPreview -> {
        backStack.add(ReminderPreviewNavKey.Preview(event.id))
      }

      is WeekViewViewModel.NavigationEvent.OpenReminderEdit -> {
        appNavBridge.navigate(BuildReminderNavKey.Main(id = event.id))
      }

      is WeekViewViewModel.NavigationEvent.OpenBirthdayPreview -> {
        appNavBridge.navigate(BirthdaysNavKey.Preview(event.id))
      }

      is WeekViewViewModel.NavigationEvent.OpenBirthdayEdit -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit(event.id))
      }

      is WeekViewViewModel.NavigationEvent.OpenNewReminder -> {
        appNavBridge.navigate(
          BuildReminderNavKey.Main(
            deepLinkDateTimeType = Reminder.BY_DATE,
            deepLinkDateTimeMillis = dateTimeManager.toMillis(LocalDateTime.of(event.date, LocalTime.now())),
          ),
        )
      }

      is WeekViewViewModel.NavigationEvent.OpenNewBirthday -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit(prefillDateEpochDay = event.date.toEpochDay()))
      }

      is WeekViewViewModel.NavigationEvent.ConfirmArchiveReminder -> {
        dialogues.askConfirmation(context, context.getString(R.string.move_to_archive)) { confirmed ->
          if (confirmed) viewModel.moveReminderToArchive(event.id)
        }
      }

      is WeekViewViewModel.NavigationEvent.ConfirmDeleteBirthday -> {
        dialogues.askConfirmation(context, context.getString(R.string.delete)) { confirmed ->
          if (confirmed) viewModel.deleteBirthday(event.id)
        }
      }

      is WeekViewViewModel.NavigationEvent.RequestGpsPermission -> {
        permissionRequester.request(
          listOf(Permissions.FOREGROUND_SERVICE, Permissions.FOREGROUND_SERVICE_LOCATION),
          onGranted = { viewModel.toggleReminder(event.id) },
        )
      }
    }
  }

  val state by viewModel.state.collectAsState()
  val refreshSignal by viewModel.refreshSignal.collectAsState()
  WeekViewScreen(
    state = state,
    initialPagerPosition = viewModel.lastPosition,
    pagerJumpRequest = pagerJumpRequest,
    onPagerJumpConsumed = { pagerJumpRequest = null },
    dateForPosition = viewModel::dateForPosition,
    onPageSettled = { position ->
      viewModel.updateLastPosition(position)
      viewModel.onDateSelected(viewModel.dateForPosition(position))
    },
    onDayClick = { day -> viewModel.selectDate(day.localDate) },
    refreshSignal = refreshSignal,
    loadDayEvents = viewModel::loadDayEvents,
    onItemClick = viewModel::onItemClick,
    onEventMenuAction = viewModel::onEventMenuAction,
    onAddReminderClick = { viewModel.onAddReminderClick(state.selectedDate) },
    onAddBirthdayClick = { viewModel.onAddBirthdayClick(state.selectedDate) },
    onBackClick = { backStack.removeLastOrNull() },
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
