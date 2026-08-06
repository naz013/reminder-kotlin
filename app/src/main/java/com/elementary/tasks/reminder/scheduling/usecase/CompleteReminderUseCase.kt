package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.calendar.history.AddReminderToHistoryUseCase
import com.elementary.tasks.reminder.scheduling.behavior.v2.BehaviorStrategyResolverV2
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger

/**
 * Completes the reminder by either scheduling the next occurrence or deactivating it.
 */
class CompleteReminderUseCase(
  private val strategyResolver: BehaviorStrategyResolverV2,
  private val deactivateReminderUseCase: DeactivateReminderUseCase,
  private val dateTimeManager: DateTimeManager,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val addReminderToHistoryUseCase: AddReminderToHistoryUseCase,
) {
  suspend operator fun invoke(reminder: ReminderV2): ReminderV2 {
    val reminder = reminder.copy(notification = reminder.notification.copy(delayMinutes = 0), lastShownAt = null)
    val strategy = strategyResolver.resolve(reminder)
    Logger.v(TAG, "Completing reminder id=${reminder.uuId} with strategy=$strategy")
    addReminderToHistoryUseCase(reminder)
    return if (strategy.canSkip(reminder)) {
      val fromDateTime =
        reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) }
          ?: dateTimeManager.getCurrentDateTime()
      val nextDateTime = strategy.calculateNextOccurrence(reminder, fromDateTime)
      if (nextDateTime == null) {
        Logger.i(TAG, "No next occurrence found, deactivating reminder id=${reminder.uuId}")
        deactivateReminderUseCase(reminder)
      } else {
        Logger.i(TAG, "Scheduling next occurrence for reminder id=${reminder.uuId} at $nextDateTime")
        val reminder =
          reminder.copy(
            schedule = reminder.schedule.copy(eventDateTime = dateTimeManager.localToUtc(nextDateTime)),
            eventCount = reminder.eventCount + 1,
          )
        activateReminderUseCase(reminder)
      }
    } else {
      Logger.i(TAG, "Going to deactivate reminder id=${reminder.uuId}")
      deactivateReminderUseCase(reminder)
    }
  }

  companion object {
    private const val TAG = "CompleteReminderUseCase"
  }
}
