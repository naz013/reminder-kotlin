package com.github.naz013.feature.calendar.agenda

import com.github.naz013.ui.agenda.AgendaCategory
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.ui.birthday.UiBirthdayListAdapter
import com.github.naz013.ui.reminder.UiReminderListAdapter
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/**
 * Maps a [ReminderV2]/[Birthday] domain object plus the concrete occurrence date/time onto the
 * `ui-agenda` presentation model. Shared by the single-day and multi-day (timeline) event use
 * cases so a recurring reminder/birthday renders identically wherever it appears.
 */
class CalendarAgendaItemFactory(
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  private val dateTimeManager: DateTimeManager,
) {
  fun toUiAgendaReminder(
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

  fun toUiAgendaBirthday(
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
