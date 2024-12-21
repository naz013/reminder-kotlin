package com.elementary.tasks.notes.preview.reminders

import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.utils.datetime.DateTimeManager

class ReminderToUiNoteAttachedReminder(
  private val dateTimeManager: DateTimeManager
) {

  operator fun invoke(reminder: Reminder): UiNoteAttachedReminder {
    return UiNoteAttachedReminder(
      id = reminder.uuId,
      summary = reminder.summary,
      dateTime = dateTimeManager.getFullDateTime(reminder.eventTime)
    )
  }
}
