package com.github.naz013.feature.calendar.timeline

import com.github.naz013.feature.calendar.agenda.CalendarAgendaItemFactory
import com.github.naz013.feature.calendar.history.GetHistoryByDateRangeUseCase
import com.github.naz013.feature.calendar.occurrence.GetOccurrencesByDateRangeUseCase
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.domain.history.EventHistoricalRecordType
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Multi-day sibling of [com.github.naz013.feature.calendar.dayview.GetDayEventItemsUseCase]:
 * resolves every reminder/birthday occurrence (active occurrences + already-fired history) across
 * the inclusive [[startDate], [endDate]] window in a single pair of range queries, grouped by the
 * day each one falls on so the timeline can render one column per day. Items within a day stay
 * sorted by time.
 */
class GetRangeEventItemsUseCase(
  private val getOccurrencesByDateRangeUseCase: GetOccurrencesByDateRangeUseCase,
  private val getHistoryByDateRangeUseCase: GetHistoryByDateRangeUseCase,
  private val birthdayRepository: BirthdayRepository,
  private val reminderV2Repository: ReminderV2Repository,
  private val groupV2Repository: GroupV2Repository,
  private val agendaItemFactory: CalendarAgendaItemFactory,
) {
  suspend operator fun invoke(
    startDate: LocalDate,
    endDate: LocalDate,
  ): Map<LocalDate, List<UiAgendaItem>> {
    val birthdays = birthdayRepository.getAll().associateBy { it.uuId }
    val reminders = reminderV2Repository.getAll(active = true, removed = false).associateBy { it.uuId }
    val groupsById = groupV2Repository.getAll().associateBy { it.uuId }

    val occurrences =
      getOccurrencesByDateRangeUseCase(startDate, endDate).mapNotNull {
        val dateTime = LocalDateTime.of(it.date, it.time)
        when (it.type) {
          OccurrenceType.Birthday -> birthdays[it.eventId]?.let { birthday -> agendaItemFactory.toUiAgendaBirthday(birthday, dateTime) }
          OccurrenceType.Reminder -> reminders[it.eventId]?.let { reminder -> agendaItemFactory.toUiAgendaReminder(reminder, dateTime, groupsById) }
          else -> null
        }
      }

    val historyRecords =
      getHistoryByDateRangeUseCase(startDate, endDate).mapNotNull { record ->
        val dateTime = LocalDateTime.of(record.date, record.time)
        when (record.type) {
          EventHistoricalRecordType.Birthday -> birthdays[record.eventId]?.let { agendaItemFactory.toUiAgendaBirthday(it, dateTime) }
          EventHistoricalRecordType.Reminder -> reminders[record.eventId]?.let { agendaItemFactory.toUiAgendaReminder(it, dateTime, groupsById) }
          else -> null
        }
      }

    return (occurrences + historyRecords)
      .distinctBy { Triple(it::class, it.id, it.dateTime) }
      .groupBy { it.dateTime.toLocalDate() }
      .mapValues { (_, items) -> items.sortedBy { it.dateTime } }
  }
}
