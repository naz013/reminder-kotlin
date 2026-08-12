package com.github.naz013.appfunctions.reminder

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository

class ListUpcomingRemindersUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(withinDays: Int): List<ReminderV2> {
    val fromUtc = dateTimeManager.localToUtc(dateTimeManager.getCurrentDateTime())
    val toUtc = fromUtc.plusDays(withinDays.toLong())
    return reminderV2Repository
      .getActiveInRange(removed = false, from = fromUtc, to = toUtc)
      .sortedBy { it.schedule.eventDateTime ?: it.schedule.startDateTime }
  }
}
