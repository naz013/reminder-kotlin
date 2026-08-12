package com.github.naz013.appfunctions.reminder

import com.github.naz013.appfunctions.toThreeTen
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import java.time.LocalDateTime as JavaLocalDateTime

class CreateSimpleReminderUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(
    title: String,
    dueDateTime: JavaLocalDateTime,
    notes: String?,
  ): ReminderV2 {
    val utcDateTime = dateTimeManager.localToUtc(dueDateTime.toThreeTen())
    val reminder =
      ReminderV2(
        summary = title,
        description = notes,
        schedule = ReminderSchedule(startDateTime = utcDateTime, eventDateTime = utcDateTime),
      )
    reminderV2Repository.save(reminder)
    return reminder
  }
}
