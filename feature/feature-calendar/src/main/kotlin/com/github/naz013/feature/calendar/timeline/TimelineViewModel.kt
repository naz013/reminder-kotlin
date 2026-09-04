package com.github.naz013.feature.calendar.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.calendar.CalendarPreferences
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaGoogleCalendarEvent
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.PublicHoliday
import com.github.naz013.domain.calendar.StartDayOfWeekProtocol
import com.github.naz013.feature.common.capitalizeFirstLetter
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit

/**
 * Drives the vertical hour-timeline shown by the 1-day, 3-day and 7-day calendar modes. All three
 * page by a fixed [daySpan]-sized block per swipe; they differ only in how that block is aligned:
 *  - 7-day (and any span >= a week) snaps each window to a whole calendar week, respecting the
 *    user's start-of-week preference, so it pages week-by-week like the month grid's rows.
 *  - 1-day and 3-day aren't calendar-aligned: the initial window is centered on the anchor date
 *    (the anchor lands in the middle day of the block, not the first), and every swipe shifts by
 *    a full [daySpan]-sized block from there.
 *
 * Shares the infinite-pager center-position math ([CENTER_POSITION]) already used by the month view.
 */
internal class TimelineViewModel(
  startDateMillis: Long,
  private val daySpan: Int,
  private val dispatcherProvider: DispatcherProvider,
  private val dateTimeManager: DateTimeManager,
  private val calendarPreferences: CalendarPreferences,
  private val getRangeEventItemsUseCase: GetRangeEventItemsUseCase,
  private val getRangeHolidaysUseCase: GetRangeHolidaysUseCase,
) : ViewModel() {

  private val startDate = dateTimeManager.fromMillis(startDateMillis).toLocalDate()
  private val weekAligned = daySpan >= DAYS_IN_WEEK
  private val windowStep = daySpan
  private val initWindowStart = if (weekAligned) weekStartOf(startDate) else startDate.minusDays((daySpan / 2).toLong())

  val state: StateFlow<TimelineScreenState> field = MutableStateFlow(TimelineScreenState(hourLabels = buildHourLabels()))
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()
  val refreshSignal: StateFlow<Int> field = MutableStateFlow(0)

  val initDate: LocalDate = startDate
  var lastPosition: Int = CENTER_POSITION
    private set

  /** Vertical scroll offset (px) last seen for this mode, or [NO_SCROLL_OFFSET] if it was never
   * set - i.e. this mode hasn't been opened yet this ViewModel's lifetime, so it should scroll to
   * the current time instead of restoring a position. Kept here (not plain Compose `remember`)
   * because navigating away to a reminder/birthday preview and back tears down and recreates the
   * timeline's composables while this ViewModel instance survives - only state held here (like
   * [lastPosition]) actually comes back with you. */
  var scrollOffset: Int = NO_SCROLL_OFFSET
    private set

  init {
    refreshSignal.update { it + 1 }
    viewModelScope.launch(dispatcherProvider.default()) {
      applyWindow(initWindowStart)
    }
  }

  fun windowStartForPosition(position: Int): LocalDate =
    initWindowStart.plusDays((position - CENTER_POSITION).toLong() * windowStep)

  /** The middle day of the window at [position] - what the host tracks as the "selected" anchor. */
  fun middayForPosition(position: Int): LocalDate = windowStartForPosition(position).plusDays((daySpan / 2).toLong())

  /** The start of the fixed-size block that contains [date], regardless of which block it falls in. */
  private fun windowStartContaining(date: LocalDate): LocalDate =
    if (weekAligned) {
      weekStartOf(date)
    } else {
      val daysBetween = ChronoUnit.DAYS.between(initWindowStart, date)
      initWindowStart.plusDays(Math.floorDiv(daysBetween, windowStep.toLong()) * windowStep)
    }

  fun positionForDate(date: LocalDate): Int {
    val targetWindowStart = windowStartContaining(date)
    val stepsBetween = ChronoUnit.DAYS.between(initWindowStart, targetWindowStart) / windowStep
    return CENTER_POSITION + stepsBetween.toInt()
  }

  fun daysForWindow(windowStart: LocalDate): List<TimelineDay> {
    val today = LocalDate.now()
    return (0 until daySpan).map { offset ->
      val date = windowStart.plusDays(offset.toLong())
      TimelineDay(
        date = date,
        weekdayLabel = dateTimeManager.formatCalendarWeekday(date),
        dayLabel = dateTimeManager.formatCalendarDay(date),
        isToday = date == today,
      )
    }
  }

  fun updateLastPosition(position: Int) {
    lastPosition = position
  }

  fun onScrollOffsetChanged(offset: Int) {
    scrollOffset = offset
  }

  fun onPageSettled(position: Int) {
    viewModelScope.launch(dispatcherProvider.default()) {
      applyWindow(windowStartForPosition(position))
    }
  }

  suspend fun loadWindowEvents(windowStart: LocalDate): Map<LocalDate, List<UiAgendaItem>> =
    withContext(dispatcherProvider.default()) {
      getRangeEventItemsUseCase(windowStart, windowStart.plusDays(daySpan - 1L))
    }

  suspend fun loadWindowHolidays(windowStart: LocalDate): Map<LocalDate, PublicHoliday> =
    withContext(dispatcherProvider.default()) {
      getRangeHolidaysUseCase(windowStart, windowStart.plusDays(daySpan - 1L))
    }

  fun onItemClick(item: UiAgendaItem) {
    when (item) {
      is UiAgendaReminder -> navigationEvent.value = Event(NavigationEvent.OpenReminderPreview(item.id))
      is UiAgendaBirthday -> navigationEvent.value = Event(NavigationEvent.OpenBirthdayPreview(item.id))
      is UiAgendaGoogleCalendarEvent -> navigationEvent.value = Event(NavigationEvent.OpenGoogleCalendarEventPreview(item.id))
      else -> Unit
    }
  }

  fun onAddReminderClick(date: LocalDate) {
    val millis = dateTimeManager.toMillis(LocalDateTime.of(date, LocalTime.now()))
    navigationEvent.emit(NavigationEvent.OpenNewReminder(millis))
  }

  fun onAddBirthdayClick(date: LocalDate) {
    navigationEvent.emit(NavigationEvent.OpenNewBirthday(date))
  }

  private fun applyWindow(windowStart: LocalDate) {
    val title =
      if (daySpan == 1) {
        "${dateTimeManager.formatCalendarDay(windowStart)} ${dateTimeManager.formatMonth(windowStart)}"
      } else {
        val windowEnd = windowStart.plusDays(daySpan - 1L)
        "${dateTimeManager.formatCalendarDay(windowStart)} – " +
          "${dateTimeManager.formatCalendarDay(windowEnd)} ${dateTimeManager.formatMonth(windowEnd)}"
      }
    state.update { it.copy(title = title.capitalizeFirstLetter()) }
  }

  private fun buildHourLabels(): List<String> =
    (0 until HOURS_IN_DAY).map { hour -> dateTimeManager.getTime(LocalTime.of(hour, 0)) }

  private fun weekStartOf(date: LocalDate): LocalDate {
    val startDayOfWeek = StartDayOfWeekProtocol(calendarPreferences.startDay).getForCalendar()
    var result = date
    while (result.dayOfWeek.value != startDayOfWeek) {
      result = result.minusDays(1)
    }
    return result
  }

  sealed interface NavigationEvent {
    data class OpenReminderPreview(
      val id: String,
    ) : NavigationEvent

    data class OpenBirthdayPreview(
      val id: String,
    ) : NavigationEvent

    data class OpenGoogleCalendarEventPreview(
      val id: String,
    ) : NavigationEvent

    data class OpenNewReminder(
      val dateMillis: Long,
    ) : NavigationEvent

    data class OpenNewBirthday(
      val date: LocalDate,
    ) : NavigationEvent
  }

  companion object {
    const val CENTER_POSITION = Int.MAX_VALUE / 2
    const val NO_SCROLL_OFFSET = -1
    private const val DAYS_IN_WEEK = 7
  }
}
