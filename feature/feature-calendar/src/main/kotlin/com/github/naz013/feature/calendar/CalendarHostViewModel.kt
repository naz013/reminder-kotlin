package com.github.naz013.feature.calendar

import androidx.lifecycle.ViewModel
import com.github.naz013.datecalc.DateTimeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Owns the two pieces of state shared by the four calendar view modes: the currently selected
 * [mode] (persisted via [CalendarPreferences.lastViewMode] so it survives app restarts) and the
 * [anchorDate] the visible mode is centered on. Switching modes seeds the newly shown mode from
 * [anchorDate] so the user keeps their place; each mode reports its settled position back through
 * [onAnchorDateChanged].
 *
 * @param initialDateMillis the date the calendar opens on (today for the normal entry, or the
 *   deep-linked date).
 * @param forcedMode when non-null, overrides the persisted mode for this entry (used by the
 *   "open this day" deep link to force [CalendarViewMode.DAY]).
 */
internal class CalendarHostViewModel(
  initialDateMillis: Long,
  forcedMode: CalendarViewMode?,
  private val dateTimeManager: DateTimeManager,
  private val calendarPreferences: CalendarPreferences,
) : ViewModel() {

  val mode: StateFlow<CalendarViewMode> field = MutableStateFlow(forcedMode ?: calendarPreferences.lastViewMode)
  val anchorDate: StateFlow<LocalDate> field =
    MutableStateFlow(dateTimeManager.fromMillis(initialDateMillis).toLocalDate())

  /**
   * Switching into a day-based mode (Day/3-day/7-day) from the app-bar toggle always jumps to
   * today, regardless of what was previously being viewed; switching into Month preserves the
   * existing anchor so the month you were browsing elsewhere stays in view.
   */
  fun onModeSelected(newMode: CalendarViewMode) {
    if (newMode == mode.value) return
    calendarPreferences.lastViewMode = newMode
    if (newMode != CalendarViewMode.MONTH) {
      anchorDate.update { LocalDate.now() }
    }
    mode.update { newMode }
  }

  /** A day was picked in another mode (e.g. tapped in the month grid): jump into Day mode on it. */
  fun openDay(date: LocalDate) {
    calendarPreferences.lastViewMode = CalendarViewMode.DAY
    anchorDate.update { date }
    mode.update { CalendarViewMode.DAY }
  }

  fun openDay(dateMillis: Long) = openDay(dateTimeManager.fromMillis(dateMillis).toLocalDate())

  fun onAnchorDateChanged(date: LocalDate) {
    anchorDate.update { date }
  }

  /** Millis for the current anchor, used to seed a child view-model's start date via Koin. */
  fun anchorMillis(): Long = dateTimeManager.toMillis(LocalDateTime.of(anchorDate.value, LocalTime.now()))
}
