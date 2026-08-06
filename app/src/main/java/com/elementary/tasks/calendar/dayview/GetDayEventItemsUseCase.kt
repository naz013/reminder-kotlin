package com.elementary.tasks.calendar.dayview

import com.elementary.tasks.calendar.history.GetHistoryByDayUseCase
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDayUseCase
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.home.agenda.AgendaCategory
import com.elementary.tasks.home.agenda.UiAgendaBirthday
import com.elementary.tasks.home.agenda.UiAgendaItem
import com.elementary.tasks.home.agenda.UiAgendaReminder
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.history.EventHistoricalRecordType
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Resolves a single day's reminders/birthdays for the week-view pager: active reminders/birthdays
 * occurring that day (via [GetOccurrencesByDayUseCase]) plus already-fired historical records for
 * that day (via [GetHistoryByDayUseCase]), projected onto the occurrence's own date/time so a
 * recurring reminder renders correctly on every day it recurs.
 */
class GetDayEventItemsUseCase(
  private val getOccurrencesByDayUseCase: GetOccurrencesByDayUseCase,
  private val getHistoryByDayUseCase: GetHistoryByDayUseCase,
  private val birthdayRepository: BirthdayRepository,
  private val reminderV2Repository: ReminderV2Repository,
  private val groupV2Repository: GroupV2Repository,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(date: LocalDate): List<UiAgendaItem> {
    val birthdays = birthdayRepository.getAll().associateBy { it.uuId }
    val reminders = reminderV2Repository.getAll(active = true, removed = false).associateBy { it.uuId }
    val groupsById = groupV2Repository.getAll().associateBy { it.uuId }

    val occurrences =
      getOccurrencesByDayUseCase(date).mapNotNull {
        val dateTime = LocalDateTime.of(it.date, it.time)
        when (it.type) {
          OccurrenceType.Birthday -> birthdays[it.eventId]?.let { birthday -> toUiAgendaBirthday(birthday, dateTime) }
          OccurrenceType.Reminder -> reminders[it.eventId]?.let { reminder -> toUiAgendaReminder(reminder, dateTime, groupsById) }
          else -> null
        }
      }

    val historyRecords =
      getHistoryByDayUseCase(date).mapNotNull { record ->
        val dateTime = LocalDateTime.of(record.date, record.time)
        when (record.type) {
          EventHistoricalRecordType.Birthday -> birthdays[record.eventId]?.let { toUiAgendaBirthday(it, dateTime) }
          EventHistoricalRecordType.Reminder -> reminders[record.eventId]?.let { toUiAgendaReminder(it, dateTime, groupsById) }
          else -> null
        }
      }

    return (occurrences + historyRecords).sortedBy { it.dateTime }
  }

  private fun toUiAgendaReminder(
    reminder: ReminderV2,
    dateTime: LocalDateTime,
    groupsById: Map<String, GroupV2>,
  ): UiAgendaReminder {
    val projected = reminder.copy(schedule = reminder.schedule.copy(eventDateTime = dateTimeManager.localToUtc(dateTime)))
    val uiReminderList = uiReminderListAdapter.createV2(projected, projected.groupId?.let { groupsById[it] })
    return UiAgendaReminder(
      id = uiReminderList.id,
      dateTime = dateTime,
      category = if (reminder.action is ReminderAction.Shopping) AgendaCategory.SHOPPING else AgendaCategory.REMINDERS,
      mainText = uiReminderList.mainText,
      secondaryText = uiReminderList.secondaryText,
      tertiaryText = uiReminderList.tertiaryText,
      tags = uiReminderList.tags,
      actions = uiReminderList.actions.copy(canSkip = false),
      state = uiReminderList.state,
    )
  }

  private fun toUiAgendaBirthday(
    birthday: Birthday,
    dateTime: LocalDateTime,
  ): UiAgendaBirthday {
    val uiBirthdayList = uiBirthdayListAdapter.convert(birthday = birthday, nowDateTime = dateTime)
    return UiAgendaBirthday(
      id = uiBirthdayList.uuId,
      dateTime = dateTime,
      name = uiBirthdayList.name,
      ageFormatted = uiBirthdayList.ageFormatted,
      remainingTimeFormatted = uiBirthdayList.remainingTimeFormatted,
      color = uiBirthdayList.color,
      contrastColor = uiBirthdayList.contrastColor,
      dateFormatted = uiBirthdayList.nextBirthdayDateFormatted,
    )
  }
}
