package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.calendar.history.AddReminderToHistoryUseCase
import com.elementary.tasks.reminder.scheduling.behavior.BehaviorStrategyResolver
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger

/**
 * Completes the reminder by either scheduling the next occurrence or deactivating it.
 */
class CompleteReminderUseCase(
  private val strategyResolver: BehaviorStrategyResolver,
  private val deactivateReminderUseCase: DeactivateReminderUseCase,
  private val dateTimeManager: DateTimeManager,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val addReminderToHistoryUseCase: AddReminderToHistoryUseCase,
) {

  suspend operator fun invoke(reminder: Reminder): Reminder {
    reminder.delay = 0
    val strategy = strategyResolver.resolve(reminder)
    addReminderToHistoryUseCase(reminder)
    return if (strategy.canSkip(reminder)) {
      val fromDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
        ?: dateTimeManager.getCurrentDateTime()
      val nextDateTime = strategy.calculateNextOccurrence(reminder, fromDateTime)
      if (nextDateTime == null) {
        Logger.i(TAG, "No next occurrence found, deactivating reminder id=${reminder.uuId}")
        deactivateReminderUseCase(reminder)
        return reminder
      }
      Logger.i(TAG, "Scheduling next occurrence for reminder id=${reminder.uuId} at $nextDateTime")
      val reminder = reminder.copy(
        eventTime = dateTimeManager.getGmtFromDateTime(nextDateTime),
        eventCount = reminder.eventCount + 1
      )
      activateReminderUseCase(reminder)
    } else {
      Logger.i(TAG, "Going to deactivate reminder id=${reminder.uuId}")
      deactivateReminderUseCase(reminder)
    }
  }

  companion object {
    private const val TAG = "CompleteReminderUseCase"
  }
}
