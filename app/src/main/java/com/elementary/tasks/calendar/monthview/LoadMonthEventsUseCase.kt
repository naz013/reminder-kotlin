package com.elementary.tasks.calendar.monthview

import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDateRangeUseCase
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.ui.common.theme.ThemeProvider
import org.threeten.bp.LocalDate

/**
 * Loads the color dot(s) to show on each day of [monthDate]'s month, one color per active
 * birthday/reminder occurrence on that day. Ports [OccurrenceType.CalendarEvent] being an
 * unhandled no-op from the legacy implementation (device/Google Calendar events aren't shown).
 */
class LoadMonthEventsUseCase(
  private val getOccurrencesByDateRangeUseCase: GetOccurrencesByDateRangeUseCase,
  private val birthdayRepository: BirthdayRepository,
  private val reminderV2Repository: ReminderV2Repository,
  private val contextProvider: ContextProvider,
  private val prefs: Prefs,
) {
  suspend operator fun invoke(monthDate: LocalDate): Map<LocalDate, List<Int>> {
    val startOfMonth = monthDate.withDayOfMonth(1)
    val endOfMonth = monthDate.withDayOfMonth(monthDate.lengthOfMonth())
    val occurrences = getOccurrencesByDateRangeUseCase(startOfMonth, endOfMonth)

    val birthdayIds = birthdayRepository.getAll().mapTo(HashSet()) { it.uuId }
    val reminderIds = reminderV2Repository.getAll(active = true, removed = false).mapTo(HashSet()) { it.uuId }

    val birthdayColor = ThemeProvider.colorBirthdayCalendar(contextProvider.themedContext, prefs.birthdayLedColor)
    val reminderColor = ThemeProvider.colorReminderCalendar(contextProvider.themedContext, prefs.reminderColor)

    val map = mutableMapOf<LocalDate, MutableList<Int>>()
    for (occurrence in occurrences) {
      val color =
        when (occurrence.type) {
          OccurrenceType.Birthday -> birthdayColor.takeIf { occurrence.eventId in birthdayIds }
          OccurrenceType.Reminder -> reminderColor.takeIf { occurrence.eventId in reminderIds }
          OccurrenceType.CalendarEvent -> null
        } ?: continue
      map.getOrPut(occurrence.date) { mutableListOf() }.add(color)
    }
    return map
  }
}
