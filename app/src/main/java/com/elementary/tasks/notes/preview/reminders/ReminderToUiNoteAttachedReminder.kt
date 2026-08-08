package com.elementary.tasks.notes.preview.reminders

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2

class ReminderToUiNoteAttachedReminder(
  private val dateTimeManager: DateTimeManager,
) {
  operator fun invoke(reminder: ReminderV2): UiNoteAttachedReminder =
    UiNoteAttachedReminder(
      id = reminder.uuId,
      summary = reminder.summary,
      dateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.getFullDateTime(dateTimeManager.utcToLocal(it)) } ?: "",
    )
}
