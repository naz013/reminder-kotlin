package com.github.naz013.appfunctions.reminder

import com.github.naz013.appfunctions.toThreeTen
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import java.time.LocalDateTime as JavaLocalDateTime

/** Deliberately does not touch [ReminderV2.sync] - neither this module's [CompleteReminderUseCase]
 * nor the app's own canonical reminder-save path bump reminder sync bookkeeping on edit, unlike
 * Note/Birthday which explicitly version-bump and mark for upload. */
class UpdateReminderUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(
    id: String,
    title: String,
    dueDateTime: JavaLocalDateTime,
    notes: String?,
  ): ReminderV2? {
    val existing = reminderV2Repository.getById(id) ?: return null
    val utcDateTime = dateTimeManager.localToUtc(dueDateTime.toThreeTen())
    val updated =
      existing.copy(
        summary = title,
        description = notes,
        schedule = existing.schedule.copy(startDateTime = utcDateTime, eventDateTime = utcDateTime),
      )
    reminderV2Repository.save(updated)
    return updated
  }
}
