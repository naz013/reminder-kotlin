package com.elementary.tasks.calendar.monthview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.calendar.monthview.monthgrid.MonthGridCell
import com.elementary.tasks.calendar.monthview.monthgrid.MonthGridFactory
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.threeten.bp.LocalDate

class CalendarViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val dateTimeManager: DateTimeManager,
  private val prefs: Prefs,
  private val monthGridFactory: MonthGridFactory,
  private val loadMonthEventsUseCase: LoadMonthEventsUseCase,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {

  val state: StateFlow<CalendarScreenState> field = MutableStateFlow(CalendarScreenState())
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()
  val refreshSignal: StateFlow<Int> field = MutableStateFlow(0)

  val initDate: LocalDate = LocalDate.now().withDayOfMonth(15)
  var lastPosition: Int = CENTER_POSITION
    private set

  private var hasResumedBefore = false

  private val weekdayLabels: List<String> =
    run {
      var date = if (prefs.startDay == 0) LocalDate.of(2022, 12, 25) else LocalDate.of(2022, 12, 26)
      (0 until 7).map {
        val label = dateTimeManager.formatCalendarWeekday(date).uppercase()
        date = date.plusDays(1)
        label
      }
    }

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.CALENDAR))
    state.update { it.copy(weekdayLabels = weekdayLabels) }
    applyMonth(initDate)
  }

  fun monthForPosition(position: Int): LocalDate = initDate.plusMonths((position - CENTER_POSITION).toLong())

  fun updateLastPosition(position: Int) {
    lastPosition = position
  }

  fun onPageSettled(position: Int) {
    applyMonth(monthForPosition(position))
  }

  fun buildGrid(monthDate: LocalDate): List<MonthGridCell> = monthGridFactory.buildGrid(monthDate)

  suspend fun loadMonthEvents(monthDate: LocalDate): Map<LocalDate, List<Int>> =
    withContext(dispatcherProvider.default()) { loadMonthEventsUseCase(monthDate) }

  /** Called explicitly from [CalendarFragment.onResume] since this is a plain [ViewModel]. */
  fun refresh() {
    if (hasResumedBefore) {
      refreshSignal.update { it + 1 }
    }
    hasResumedBefore = true
  }

  /** Snaps the header/title back to the current month; the pager reset itself is driven by the Fragment. */
  fun resetToToday() {
    lastPosition = CENTER_POSITION
    applyMonth(initDate)
  }

  fun onDayClick(date: LocalDate) {
    navigationEvent.value = Event(NavigationEvent.OpenDayView(date))
  }

  fun onAddReminderClick(date: LocalDate) {
    navigationEvent.value = Event(NavigationEvent.OpenNewReminder(date))
  }

  fun onAddBirthdayClick(date: LocalDate) {
    navigationEvent.value = Event(NavigationEvent.OpenNewBirthday(date))
  }

  fun onSettingsClick() {
    navigationEvent.value = Event(NavigationEvent.OpenSettings)
  }

  private fun applyMonth(monthDate: LocalDate) {
    val title = StringUtils.capitalize(dateTimeManager.formatCalendarMonthYear(monthDate))
    state.update { it.copy(title = title) }
  }

  sealed interface NavigationEvent {
    data class OpenDayView(
      val date: LocalDate,
    ) : NavigationEvent

    data class OpenNewReminder(
      val date: LocalDate,
    ) : NavigationEvent

    data class OpenNewBirthday(
      val date: LocalDate,
    ) : NavigationEvent

    data object OpenSettings : NavigationEvent
  }

  companion object {
    const val CENTER_POSITION = Int.MAX_VALUE / 2
  }
}
