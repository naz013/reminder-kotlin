package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository

/**
 * Maps each waiting reminder's id to the title of the reminder it is waiting for, so list rows can
 * show "Waiting for ...". Reminders without a resolvable, non-archived predecessor are left out.
 */
class ResolveWaitingTitlesUseCase(
  private val reminderV2Repository: ReminderV2Repository,
) {

  suspend operator fun invoke(reminders: List<ReminderV2>): Map<String, String> {
    val waiting = reminders.filter { it.startAfterReminderId != null }
    if (waiting.isEmpty()) return emptyMap()
    val known = reminders.associateBy { it.uuId }
    val result = mutableMapOf<String, String>()
    waiting.forEach { reminder ->
      val predecessorId = reminder.startAfterReminderId ?: return@forEach
      val predecessor = known[predecessorId] ?: reminderV2Repository.getById(predecessorId)
      if (predecessor != null && !predecessor.isRemoved) {
        result[reminder.uuId] = predecessor.summary
      }
    }
    return result
  }
}
