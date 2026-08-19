package com.github.naz013.feature.calendar.monthview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.calendar.CalendarHostViewModel
import com.github.naz013.feature.calendar.CalendarViewMode
import com.github.naz013.feature.calendar.timeline.TimelineScreen
import com.github.naz013.feature.calendar.timeline.TimelineViewModel
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.calendarEntries(
  backStack: MutableList<NavKey>,
  onOpenNewReminder: (dateMillis: Long) -> Unit,
  onOpenReminderPreview: (id: String) -> Unit,
  onOpenNewBirthday: (epochDay: Long) -> Unit,
  onOpenBirthdayPreview: (id: String) -> Unit,
  onOpenSettings: (screenTitle: String) -> Unit,
) {
  entry<CalendarNavKey.Home> {
    CalendarHostEntry(
      initialDateMillis = System.currentTimeMillis(),
      forcedMode = null,
      backStack = backStack,
      onOpenNewReminder = onOpenNewReminder,
      onOpenReminderPreview = onOpenReminderPreview,
      onOpenNewBirthday = onOpenNewBirthday,
      onOpenBirthdayPreview = onOpenBirthdayPreview,
      onOpenSettings = onOpenSettings,
    )
  }
  entry<CalendarNavKey.DayAt> { key ->
    CalendarHostEntry(
      initialDateMillis = key.dateMillis,
      forcedMode = CalendarViewMode.DAY,
      backStack = backStack,
      onOpenNewReminder = onOpenNewReminder,
      onOpenReminderPreview = onOpenReminderPreview,
      onOpenNewBirthday = onOpenNewBirthday,
      onOpenBirthdayPreview = onOpenBirthdayPreview,
      onOpenSettings = onOpenSettings,
    )
  }
}

@Composable
private fun CalendarHostEntry(
  initialDateMillis: Long,
  forcedMode: CalendarViewMode?,
  backStack: MutableList<NavKey>,
  onOpenNewReminder: (dateMillis: Long) -> Unit,
  onOpenReminderPreview: (id: String) -> Unit,
  onOpenNewBirthday: (epochDay: Long) -> Unit,
  onOpenBirthdayPreview: (id: String) -> Unit,
  onOpenSettings: (screenTitle: String) -> Unit,
) {
  val hostViewModel = koinViewModel<CalendarHostViewModel> { parametersOf(initialDateMillis, forcedMode) }
  val mode by hostViewModel.mode.collectAsState()

  when (mode) {
    CalendarViewMode.MONTH ->
      MonthMode(
        host = hostViewModel,
        backStack = backStack,
        onOpenNewReminder = onOpenNewReminder,
        onOpenNewBirthday = onOpenNewBirthday,
        onOpenSettings = onOpenSettings,
      )

    CalendarViewMode.DAY, CalendarViewMode.THREE_DAY, CalendarViewMode.SEVEN_DAY ->
      // Keyed by mode: Day/3-day/7-day share this same call site, but each has its own pager
      // position semantics (different daySpan/window math), so switching between them must
      // discard the previous mode's remembered pager/scroll state rather than reuse it.
      key(mode) {
        TimelineMode(
          mode = mode,
          host = hostViewModel,
          backStack = backStack,
          onOpenNewReminder = onOpenNewReminder,
          onOpenReminderPreview = onOpenReminderPreview,
          onOpenNewBirthday = onOpenNewBirthday,
          onOpenBirthdayPreview = onOpenBirthdayPreview,
        )
      }
  }
}

@Composable
private fun MonthMode(
  host: CalendarHostViewModel,
  backStack: MutableList<NavKey>,
  onOpenNewReminder: (dateMillis: Long) -> Unit,
  onOpenNewBirthday: (epochDay: Long) -> Unit,
  onOpenSettings: (screenTitle: String) -> Unit,
) {
  val viewModel = koinViewModel<CalendarViewModel>()
  val anchorDate by host.anchorDate.collectAsState()

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
      is CalendarViewModel.NavigationEvent.OpenDayView -> host.openDay(event.dateMillis)
      is CalendarViewModel.NavigationEvent.OpenNewReminder -> onOpenNewReminder(event.dateMillis)
      is CalendarViewModel.NavigationEvent.OpenNewBirthday -> onOpenNewBirthday(event.date.toEpochDay())
      is CalendarViewModel.NavigationEvent.OpenSettings -> onOpenSettings(event.screenTitle)
    }
  }

  val state by viewModel.state.collectAsState()
  val refreshSignal by viewModel.refreshSignal.collectAsState()
  CalendarScreen(
    state = state,
    currentMode = CalendarViewMode.MONTH,
    onModeSelected = host::onModeSelected,
    initialPagerPosition = viewModel.positionForDate(anchorDate),
    pagerJumpRequest = pagerJumpRequest,
    onPagerJumpConsumed = { pagerJumpRequest = null },
    monthForPosition = viewModel::monthForPosition,
    onPageSettled = { position ->
      viewModel.updateLastPosition(position)
      viewModel.onPageSettled(position)
      host.onAnchorDateChanged(viewModel.monthForPosition(position))
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
private fun TimelineMode(
  mode: CalendarViewMode,
  host: CalendarHostViewModel,
  backStack: MutableList<NavKey>,
  onOpenNewReminder: (dateMillis: Long) -> Unit,
  onOpenReminderPreview: (id: String) -> Unit,
  onOpenNewBirthday: (epochDay: Long) -> Unit,
  onOpenBirthdayPreview: (id: String) -> Unit,
) {
  val daySpan = mode.daySpan
  // Keyed by span so Day/3-day/7-day each get their own retained view-model instance.
  val viewModel =
    koinViewModel<TimelineViewModel>(key = "timeline-$daySpan") { parametersOf(host.anchorMillis(), daySpan) }
  val anchorDate by host.anchorDate.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is TimelineViewModel.NavigationEvent.OpenReminderPreview -> onOpenReminderPreview(event.id)
      is TimelineViewModel.NavigationEvent.OpenBirthdayPreview -> onOpenBirthdayPreview(event.id)
      is TimelineViewModel.NavigationEvent.OpenNewReminder -> onOpenNewReminder(event.dateMillis)
      is TimelineViewModel.NavigationEvent.OpenNewBirthday -> onOpenNewBirthday(event.date.toEpochDay())
    }
  }

  val state by viewModel.state.collectAsState()
  val refreshSignal by viewModel.refreshSignal.collectAsState()
  TimelineScreen(
    state = state,
    currentMode = mode,
    onModeSelected = host::onModeSelected,
    initialPagerPosition = viewModel.positionForDate(anchorDate),
    pagerJumpRequest = null,
    onPagerJumpConsumed = {},
    windowStartForPosition = viewModel::windowStartForPosition,
    daysForWindow = viewModel::daysForWindow,
    onPageSettled = { position ->
      viewModel.updateLastPosition(position)
      viewModel.onPageSettled(position)
      host.onAnchorDateChanged(viewModel.middayForPosition(position))
    },
    refreshSignal = refreshSignal,
    loadWindowEvents = viewModel::loadWindowEvents,
    loadWindowHolidays = viewModel::loadWindowHolidays,
    onItemClick = viewModel::onItemClick,
    onDayHeaderClick = { date -> host.openDay(date) },
    onAddReminderClick = { viewModel.onAddReminderClick(anchorDate) },
    onAddBirthdayClick = { viewModel.onAddBirthdayClick(anchorDate) },
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    initialScrollOffset = viewModel.scrollOffset,
    onScrollOffsetChanged = viewModel::onScrollOffsetChanged,
  )
}
