package com.github.naz013.logic.reminder.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.reminderLocalToUtc
import com.github.naz013.logic.reminder.reminderNow
import com.github.naz013.repository.ReminderV2Repository

/**
 * Starts every reminder that was waiting for [predecessorId] and unlinks it, so the link is
 * one-shot. Called by every path that finishes a reminder (complete, delete, archive).
 */
class ActivateChainedRemindersUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val strategyResolver: BehaviorStrategyResolverV2,
  private val dateTimeManager: DateTimeManager,
) {

  suspend operator fun invoke(predecessorId: String) {
    val successors = reminderV2Repository.getByStartAfterReminderId(predecessorId)
    if (successors.isEmpty()) return
    Logger.i(TAG, "Starting ${successors.size} reminder(s) chained after id=$predecessorId")
    successors.forEach { successor ->
      val unlinked = successor.copy(startAfterReminderId = null)
      if (unlinked.isRemoved) {
        saveReminderUseCase(unlinked)
      } else {
        activateReminderUseCase(withFreshFireTime(unlinked), startAnyway = true)
      }
    }
  }

  private fun withFreshFireTime(reminder: ReminderV2): ReminderV2 {
    val strategy = strategyResolver.resolve(reminder)
    if (!strategy.canSkip(reminder) || strategy.canStartImmediately(reminder)) return reminder
    val now = dateTimeManager.reminderNow(reminder)
    val next = strategy.calculateNextOccurrence(reminder, now) ?: return reminder
    val nextUtc = dateTimeManager.reminderLocalToUtc(reminder, next)
    return reminder.copy(schedule = reminder.schedule.copy(eventDateTime = nextUtc))
  }

  companion object {
    private const val TAG = "ActivateChainedRemindersUseCase"
  }
}
