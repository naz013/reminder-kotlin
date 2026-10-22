package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.chain.ReminderChainValidator
import com.github.naz013.repository.ReminderV2Repository

/**
 * Lists the reminders a given reminder may be told to start after: active or still-waiting,
 * not archived, not the reminder itself and not anything that would close a cycle.
 */
class FindStartAfterCandidatesUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val chainValidator: ReminderChainValidator,
) {

  suspend operator fun invoke(reminderId: String): List<ReminderV2> {
    val active = reminderV2Repository.getAll(active = true, removed = false)
    val waiting = reminderV2Repository.getAll(active = false, removed = false)
      .filter { it.startAfterReminderId != null }
    val candidates = active + waiting
    val links = candidates.associate { it.uuId to it.startAfterReminderId }
    return candidates
      .filter { it.uuId != reminderId }
      .filterNot { chainValidator.wouldCreateCycle(reminderId, it.uuId) { id -> links[id] } }
      .sortedBy { it.summary.lowercase() }
  }
}
