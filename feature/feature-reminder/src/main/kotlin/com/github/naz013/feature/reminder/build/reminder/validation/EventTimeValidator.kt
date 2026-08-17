package com.github.naz013.feature.reminder.build.reminder.validation

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2

class EventTimeValidator {
  operator fun invoke(reminder: ReminderV2): Boolean =
    when (reminder.recurrence) {
      is RecurrenceRule.ICalendar,
      is RecurrenceRule.Yearly,
      is RecurrenceRule.Countdown,
      is RecurrenceRule.Weekly,
      is RecurrenceRule.Monthly,
      is RecurrenceRule.RelativeMonthly,
      -> reminder.schedule.eventDateTime != null

      RecurrenceRule.Once, is RecurrenceRule.Daily ->
        if (reminder.action is ReminderAction.Shopping) {
          true
        } else {
          reminder.schedule.eventDateTime != null
        }

      RecurrenceRule.LocationEnter, RecurrenceRule.LocationExit ->
        if (reminder.location?.hasDelayedReminder == true) {
          reminder.schedule.eventDateTime != null
        } else {
          true
        }
    }
}
