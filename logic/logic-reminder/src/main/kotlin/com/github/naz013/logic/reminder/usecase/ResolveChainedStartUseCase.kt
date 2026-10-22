package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.chain.ReminderChainValidator
import com.github.naz013.repository.ReminderV2Repository

/**
 * Decides whether a reminder that carries a "start after" link must wait for its predecessor.
 *
 * A usable link keeps the reminder inactive (waiting). A link that points to itself, closes a
 * cycle, or targets a predecessor that is missing, archived or already finished is dropped so the
 * reminder is treated as an ordinary one. A finished predecessor is one that is inactive and not
 * itself waiting.
 */
class ResolveChainedStartUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val chainValidator: ReminderChainValidator,
) {

  suspend operator fun invoke(reminder: ReminderV2): ReminderV2 {
    val predecessorId = reminder.startAfterReminderId ?: return reminder
    val predecessor = reminderV2Repository.getById(predecessorId)
    return when {
      predecessorId == reminder.uuId -> dropLink(reminder, "self reference")
      predecessor == null || predecessor.isRemoved -> dropLink(reminder, "predecessor missing or removed")
      !predecessor.isActive && predecessor.startAfterReminderId == null ->
        dropLink(reminder, "predecessor already finished")
      createsCycle(reminder, predecessor) -> dropLink(reminder, "cycle")
      else -> reminder.copy(isActive = false)
    }
  }

  private suspend fun createsCycle(reminder: ReminderV2, predecessor: ReminderV2): Boolean {
    val links = mutableMapOf<String, String?>()
    var current: ReminderV2? = predecessor
    while (current != null && links.size < MAX_CHAIN_DEPTH && !links.containsKey(current.uuId)) {
      links[current.uuId] = current.startAfterReminderId
      current = current.startAfterReminderId?.let { reminderV2Repository.getById(it) }
    }
    return chainValidator.wouldCreateCycle(reminder.uuId, predecessor.uuId) { links[it] }
  }

  private fun dropLink(reminder: ReminderV2, reason: String): ReminderV2 {
    Logger.w(TAG, "Dropping start-after link of reminder id=${reminder.uuId}: $reason")
    return reminder.copy(startAfterReminderId = null)
  }

  companion object {
    private const val TAG = "ResolveChainedStartUseCase"
    private const val MAX_CHAIN_DEPTH = 100
  }
}
