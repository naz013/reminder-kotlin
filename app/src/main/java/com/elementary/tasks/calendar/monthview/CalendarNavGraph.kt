package com.elementary.tasks.calendar.monthview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.github.naz013.feature.birthday.BirthdaysNavKey
import com.elementary.tasks.calendar.dayview.WeekViewScreen
import com.elementary.tasks.calendar.dayview.WeekViewViewModel
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.elementary.tasks.navigation.nav3.rememberAppNavBridge
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.feature.reminder.preview.ReminderPreviewNavKey
import com.github.naz013.feature.settings.SettingsNavKey
import com.github.naz013.common.Permissions
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.calendarEntries(backStack: MutableList<NavKey>) {
  entry<CalendarNavKey.Month> { MonthEntry(backStack) }
  entry<CalendarNavKey.Day> { key -> DayEntry(key, backStack) }
}

@Composable
private fun MonthEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<CalendarViewModel>()

  val appNavBridge = rememberAppNavBridge()

  var pagerJumpRequest by remember { mutableStateOf<Int?>(null) }

  // CalendarViewModel is a plain ViewModel (not a lifecycle observer) - refresh() must be driven
  // explicitly on every ON_RESUME. Note this fires both on a genuine app foreground and whenever
  // this entry is recomposed after returning from another in-app screen (LocalLifecycleOwner here
  // is Activity-scoped and addObserver() replays ON_RESUME synchronously when already resumed) -
  // so this must stay limited to a data refresh and never reset the selected month/pager position.
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(viewModel, lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          viewModel.refresh()
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is CalendarViewModel.NavigationEvent.OpenDayView -> {
        backStack.add(CalendarNavKey.Day(event.dateMillis))
      }

      is CalendarViewModel.NavigationEvent.OpenNewReminder -> {
        appNavBridge.navigate(
          BuildReminderNavKey.Main(
            deepLinkDateTimeType = BuildReminderNavKey.Main.DateTimeType.Date,
            deepLinkDateTimeMillis = event.dateMillis,
          ),
        )
      }

      is CalendarViewModel.NavigationEvent.OpenNewBirthday -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit(prefillDateEpochDay = event.date.toEpochDay()))
      }

      is CalendarViewModel.NavigationEvent.OpenSettings -> {
        backStack.add(SettingsNavKey.Calendar(event.screenTitle))
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
    loadMonthHolidays = viewModel::loadMonthHolidays,
    onDayClick = viewModel::onDayClick,
    onAddReminderClick = viewModel::onAddReminderClick,
    onAddBirthdayClick = viewModel::onAddBirthdayClick,
    onSettingsClick = viewModel::onSettingsClick,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  )
}

@Composable
private fun DayEntry(
  key: CalendarNavKey.Day,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<WeekViewViewModel> { parametersOf(key.dateMillis) }

  val dialogDispatcher = rememberDialogDispatcher()
  val appNavBridge = rememberAppNavBridge()
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
            deepLinkDateTimeType = BuildReminderNavKey.Main.DateTimeType.Date,
            deepLinkDateTimeMillis = event.dateMillis,
          ),
        )
      }

      is WeekViewViewModel.NavigationEvent.OpenNewBirthday -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit(prefillDateEpochDay = event.date.toEpochDay()))
      }

      is WeekViewViewModel.NavigationEvent.ConfirmArchiveReminder -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.move_to_archive,
          textRes = R.string.are_you_sure,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
          onPositive = { viewModel.moveReminderToArchive(event.id) },
        )
      }

      is WeekViewViewModel.NavigationEvent.ConfirmDeleteBirthday -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.delete,
          textRes = R.string.are_you_sure,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
          onPositive = { viewModel.deleteBirthday(event.id) },
        )
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
    loadDayHoliday = viewModel::loadDayHoliday,
    onItemClick = viewModel::onItemClick,
    onAgendaMenuAction = viewModel::onAgendaMenuAction,
    onAddReminderClick = { viewModel.onAddReminderClick(state.selectedDate) },
    onAddBirthdayClick = { viewModel.onAddBirthdayClick(state.selectedDate) },
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  )
}
