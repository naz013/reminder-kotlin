package com.github.naz013.feature.calendar.monthview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.naz013.ui.common.R
import com.github.naz013.feature.calendar.CalendarPreferences
import com.github.naz013.feature.calendar.monthview.monthgrid.MonthGridCell
import com.github.naz013.feature.calendar.monthview.monthgrid.MonthGridFactory
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.PublicHoliday
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit

class CalendarViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val dateTimeManager: DateTimeManager,
  private val calendarPreferences: CalendarPreferences,
  private val monthGridFactory: MonthGridFactory,
  private val loadMonthEventsUseCase: LoadMonthEventsUseCase,
  private val loadMonthHolidaysUseCase: LoadMonthHolidaysUseCase,
  private val analyticsEventSender: AnalyticsEventSender,
  private val textProvider: TextProvider,
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
      var date = if (calendarPreferences.startDay == 0) LocalDate.of(2022, 12, 25) else LocalDate.of(2022, 12, 26)
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

  fun positionForDate(date: LocalDate): Int {
    val monthsBetween = ChronoUnit.MONTHS.between(initDate.withDayOfMonth(1), date.withDayOfMonth(1))
    return CENTER_POSITION + monthsBetween.toInt()
  }

  fun updateLastPosition(position: Int) {
    lastPosition = position
  }

  fun onPageSettled(position: Int) {
    applyMonth(monthForPosition(position))
  }

  fun buildGrid(monthDate: LocalDate): List<MonthGridCell> = monthGridFactory.buildGrid(monthDate)

  suspend fun loadMonthEvents(monthDate: LocalDate): Map<LocalDate, List<Int>> =
    withContext(dispatcherProvider.default()) { loadMonthEventsUseCase(monthDate) }

  suspend fun loadMonthHolidays(monthDate: LocalDate): Map<LocalDate, PublicHoliday> =
    withContext(dispatcherProvider.default()) { loadMonthHolidaysUseCase(monthDate) }

  /** Called explicitly on every `ON_RESUME` from `CalendarNavGraph.kt`'s `MonthEntry` since this
   *  is a plain [ViewModel], not a lifecycle observer. */
  fun refresh() {
    if (hasResumedBefore) {
      refreshSignal.update { it + 1 }
    }
    hasResumedBefore = true
  }

  fun onDayClick(date: LocalDate) {
    val millis = dateTimeManager.toMillis(LocalDateTime.of(date, LocalTime.now()))
    navigationEvent.value = Event(NavigationEvent.OpenDayView(millis))
  }

  fun onAddReminderClick(date: LocalDate) {
    val millis = dateTimeManager.toMillis(LocalDateTime.of(date, LocalTime.now()))
    navigationEvent.value = Event(NavigationEvent.OpenNewReminder(millis))
  }

  fun onAddBirthdayClick(date: LocalDate) {
    navigationEvent.value = Event(NavigationEvent.OpenNewBirthday(date))
  }

  fun onSettingsClick() {
    navigationEvent.emit(NavigationEvent.OpenSettings(textProvider.getString(R.string.action_settings)))
  }

  private fun applyMonth(monthDate: LocalDate) {
    val title = StringUtils.capitalize(dateTimeManager.formatCalendarMonthYear(monthDate))
    state.update { it.copy(title = title) }
  }

  sealed interface NavigationEvent {
    data class OpenDayView(
      val dateMillis: Long,
    ) : NavigationEvent

    data class OpenNewReminder(
      val dateMillis: Long,
    ) : NavigationEvent

    data class OpenNewBirthday(
      val date: LocalDate,
    ) : NavigationEvent

    data class OpenSettings(
      val screenTitle: String,
    ) : NavigationEvent
  }

  companion object {
    const val CENTER_POSITION = Int.MAX_VALUE / 2
  }
}
