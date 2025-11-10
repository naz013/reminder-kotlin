package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.reminder.scheduling.behavior.BehaviorStrategyResolver
import com.elementary.tasks.reminder.scheduling.behavior.LocationBasedStrategy
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.datetime.plusMillis
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger

/**
 * Use case to skip a reminder to its next occurrence.
 */
class SkipReminderUseCase(
  private val strategyResolver: BehaviorStrategyResolver,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val dateTimeManager: DateTimeManager,
) {

  suspend operator fun invoke(reminder: Reminder): Reminder {
    val strategy = strategyResolver.resolve(reminder)
    if (strategy is LocationBasedStrategy) {
      Logger.w(TAG, "Cannot skip location-based reminder ${reminder.uuId}.")
      return reminder
    }
    return if (strategy.canSkip(reminder)) {
      reminder.delay = 0
      val fromDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)?.plusMillis(1000L)
        ?: dateTimeManager.getCurrentDateTime()
      val nextDateTime = strategy.calculateNextOccurrence(reminder, fromDateTime)
      if (nextDateTime == null) {
        Logger.i(TAG, "No next occurrence found, deactivating reminder id=${reminder.uuId}")
        return reminder
      }
      Logger.i(TAG, "Skipping reminder id=${reminder.uuId} to $nextDateTime")
      val reminder = reminder.copy(
        eventTime = dateTimeManager.getGmtFromDateTime(nextDateTime),
        eventCount = reminder.eventCount + 1
      )
      activateReminderUseCase(reminder)
    } else {
      Logger.w(TAG, "Cannot skip reminder id=${reminder.uuId}.")
      reminder
    }
  }

  companion object {
    private const val TAG = "SkipReminderUseCase"
  }
}
