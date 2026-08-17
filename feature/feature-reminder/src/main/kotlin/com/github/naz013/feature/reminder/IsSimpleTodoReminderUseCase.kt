package com.github.naz013.feature.reminder

import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.RecurrenceRule

/** Whether [reminder] can be fully represented by the lightweight Todo screen (title, checklist,
 *  group, tags) - used to decide whether editing it should open that screen instead of the full
 *  reminder builder. Deliberately does not gate on `notification`/priority/etc. - those aren't
 *  shown on the Todo screen and simply ride along untouched when it saves. */
internal class IsSimpleTodoReminderUseCase {
  operator fun invoke(reminder: ReminderV2): Boolean =
    reminder.action is ReminderAction.Shopping &&
      reminder.recurrence == RecurrenceRule.Once &&
      reminder.schedule.eventDateTime == null &&
      reminder.description == null &&
      reminder.noteId.isEmpty() &&
      reminder.calendarExport == null &&
      reminder.taskExport == null &&
      reminder.location == null &&
      reminder.attachmentFiles.isEmpty() &&
      reminder.places.isEmpty()
}
