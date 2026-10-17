package com.github.naz013.feature.home.dayoverview

import androidx.lifecycle.ViewModel
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.feature.home.scheduleview.ObserveTodayScheduleUseCase
import com.github.naz013.feature.home.scheduleview.TodayItemHomeEventMapper
import com.github.naz013.feature.home.scheduleview.TodaySnapshot
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.today.TodayBucket
import com.github.naz013.logic.reminder.today.TodayItem
import com.github.naz013.ui.common.R
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.TextStyle
import java.util.Locale

internal class DayOverviewViewModel(
  dispatcherProvider: DispatcherProvider,
  observeTodayScheduleUseCase: ObserveTodayScheduleUseCase,
  private val todayItemHomeEventMapper: TodayItemHomeEventMapper,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
) : ViewModel() {

  val state: StateFlow<DayOverviewState> = observeTodayScheduleUseCase()
    .map { buildState(it) }
    .flowOn(dispatcherProvider.default())
    .stateInWhileSubscribed(DayOverviewState())

  private suspend fun buildState(snapshot: TodaySnapshot): DayOverviewState {
    val (allItems, now) = snapshot
    Logger.d(TAG, "Building day overview for ${allItems.size} items")
    val today = now.toLocalDate()
    val overdueCount = allItems.count { it is TodayItem.Reminder && it.isOverdue }
    val todayItems = allItems.filter { !(it is TodayItem.Reminder && it.isOverdue) }
    val timed = todayItems
      .filter { it.bucket == TodayBucket.Timed && it.dateTime.toLocalDate() == today }
      .sortedBy { it.dateTime }
    val freeTime = DayOverviewCalculator.calculateFreeTime(
      itemStartMinutes = timed.map { it.dateTime.toLocalTime().toMinuteOfDay() },
      nowMinute = now.toLocalTime().toMinuteOfDay(),
    )
    return DayOverviewState(
      dateHeading = dateTimeManager.formatCalendarDate(today),
      summary = buildSummary(todayItems, timed, now, overdueCount),
      nextItem = findNextItem(timed, now),
      freeTime = buildFreeTimeLabels(freeTime),
      weekDays = buildWeekDays(today),
    )
  }

  private fun buildSummary(
    todayItems: List<TodayItem>,
    timed: List<TodayItem>,
    now: LocalDateTime,
    overdueCount: Int,
  ): String {
    val done = timed.count { it.dateTime.isBefore(now) }
    val left = todayItems.size - done
    return listOf(
      R.plurals.home_summary_left to left,
      R.plurals.home_summary_done to done,
      R.plurals.home_summary_overdue to overdueCount,
    )
      .filter { (_, count) -> count > 0 }
      .joinToString(SUMMARY_SEPARATOR) { (id, count) -> textProvider.getQuantityString(id, count) }
  }

  private suspend fun findNextItem(
    timed: List<TodayItem>,
    now: LocalDateTime,
  ): DayOverviewState.NextItem? {
    val index = DayOverviewCalculator.findNextIndex(timed.map { it.dateTime }, now) ?: return null
    val item = timed[index]
    val title = todayItemHomeEventMapper(listOf(item), now).firstOrNull()?.text.orEmpty()
    return DayOverviewState.NextItem(
      time = dateTimeManager.getTime(item.dateTime.toLocalTime()),
      title = title,
    )
  }

  private fun buildFreeTimeLabels(freeTime: FreeTime): DayOverviewState.FreeTimeLabels {
    val freeUntil = freeTime.freeUntilMinute
    val current = when {
      !freeTime.isFreeNow -> textProvider.getString(R.string.day_overview_busy_now)
      freeUntil != null -> textProvider.getString(R.string.day_overview_free_until, formatMinute(freeUntil))
      else -> textProvider.getString(R.string.day_overview_free_rest_of_day)
    }
    val largest = freeTime.largestBlock?.let {
      textProvider.getString(
        R.string.day_overview_largest_free,
        formatMinute(it.startMinute),
        formatMinute(it.endMinute),
      )
    }
    return DayOverviewState.FreeTimeLabels(current = current, largest = largest)
  }

  private fun buildWeekDays(today: LocalDate): List<DayOverviewState.WeekDay> {
    val monday = today.with(DayOfWeek.MONDAY)
    val locale = Locale.getDefault()
    return (0L until DAYS_IN_WEEK).map { offset ->
      val date = monday.plusDays(offset)
      DayOverviewState.WeekDay(
        weekDayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
        dayOfMonth = date.dayOfMonth.toString(),
        isToday = date == today,
      )
    }
  }

  private fun formatMinute(minute: Int): String {
    val time = if (minute >= DayOverviewCalculator.DAY_END_MINUTE) {
      LocalTime.of(LAST_HOUR, LAST_MINUTE)
    } else {
      LocalTime.of(minute / MINUTES_IN_HOUR, minute % MINUTES_IN_HOUR)
    }
    return dateTimeManager.getTime(time)
  }

  private fun LocalTime.toMinuteOfDay(): Int = hour * MINUTES_IN_HOUR + minute

  companion object {
    private const val TAG = "DayOverviewViewModel"
    private const val SUMMARY_SEPARATOR = " · "
    private const val DAYS_IN_WEEK = 7L
    private const val MINUTES_IN_HOUR = 60
    private const val LAST_HOUR = 23
    private const val LAST_MINUTE = 59
  }
}
