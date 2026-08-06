package com.github.naz013.usecase.reminders

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import org.threeten.bp.LocalDateTime

class GetRemindersV2InRangeUseCase(
  private val reminderV2Repository: ReminderV2Repository
) {

  suspend operator fun invoke(from: LocalDateTime, to: LocalDateTime): List<ReminderV2> {
    return reminderV2Repository.getActiveInRange(removed = false, from = from, to = to)
  }
}
