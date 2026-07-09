package com.elementary.tasks.calendar.dayview

import com.elementary.tasks.calendar.history.GetHistoryByDayUseCase
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDayUseCase
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.home.eventsview.EventCategory
import com.elementary.tasks.home.eventsview.UiEventBirthday
import com.elementary.tasks.home.eventsview.UiEventItem
import com.elementary.tasks.home.eventsview.UiEventReminder
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.history.EventHistoricalRecordType
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderRepository
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
  private val reminderRepository: ReminderRepository,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(date: LocalDate): List<UiEventItem> {
    val birthdays = birthdayRepository.getAll().associateBy { it.uuId }
    val reminders = reminderRepository.getActive().associateBy { it.uuId }

    val occurrences =
      getOccurrencesByDayUseCase(date).mapNotNull {
        val dateTime = LocalDateTime.of(it.date, it.time)
        when (it.type) {
          OccurrenceType.Birthday -> birthdays[it.eventId]?.let { birthday -> toUiEventBirthday(birthday, dateTime) }
          OccurrenceType.Reminder -> reminders[it.eventId]?.let { reminder -> toUiEventReminder(reminder, dateTime) }
          else -> null
        }
      }

    val historyRecords =
      getHistoryByDayUseCase(date).mapNotNull { record ->
        val dateTime = LocalDateTime.of(record.date, record.time)
        when (record.type) {
          EventHistoricalRecordType.Birthday -> birthdays[record.eventId]?.let { toUiEventBirthday(it, dateTime) }
          EventHistoricalRecordType.Reminder -> reminders[record.eventId]?.let { toUiEventReminder(it, dateTime) }
          else -> null
        }
      }

    return (occurrences + historyRecords).sortedBy { it.dateTime }
  }

  private fun toUiEventReminder(
    reminder: Reminder,
    dateTime: LocalDateTime,
  ): UiEventReminder {
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
    val uiReminderList = uiReminderListAdapter.create(reminder)
    return UiEventReminder(
      id = uiReminderList.id,
      dateTime = dateTime,
      category = if (reminder.type == Reminder.BY_DATE_SHOP) EventCategory.SHOPPING else EventCategory.REMINDERS,
      mainText = uiReminderList.mainText,
      secondaryText = uiReminderList.secondaryText,
      tertiaryText = uiReminderList.tertiaryText,
      tags = uiReminderList.tags,
      actions = uiReminderList.actions,
      state = uiReminderList.state,
    )
  }

  private fun toUiEventBirthday(
    birthday: Birthday,
    dateTime: LocalDateTime,
  ): UiEventBirthday {
    val uiBirthdayList = uiBirthdayListAdapter.convert(birthday = birthday, nowDateTime = dateTime)
    return UiEventBirthday(
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
