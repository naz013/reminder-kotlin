package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.reminder.scheduling.behavior.v2.BehaviorStrategyResolverV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.LocationBasedStrategyV2
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.datecalc.plusMillis
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger

/**
 * Use case to skip a reminder to its next occurrence.
 */
class SkipReminderUseCase(
  private val strategyResolver: BehaviorStrategyResolverV2,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(reminder: ReminderV2): ReminderV2 {
    val strategy = strategyResolver.resolve(reminder)
    if (strategy is LocationBasedStrategyV2) {
      Logger.w(TAG, "Cannot skip location-based reminder ${reminder.uuId}.")
      return reminder
    }
    return if (strategy.canSkip(reminder)) {
      val fromDateTime =
        reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) }?.plusMillis(1000L)
          ?: dateTimeManager.getCurrentDateTime()
      val nextDateTime = strategy.calculateNextOccurrence(reminder, fromDateTime)
      if (nextDateTime == null) {
        Logger.i(TAG, "No next occurrence found, deactivating reminder id=${reminder.uuId}")
        return reminder
      }
      Logger.i(TAG, "Skipping reminder id=${reminder.uuId} to $nextDateTime")
      val reminder =
        reminder.copy(
          schedule = reminder.schedule.copy(eventDateTime = dateTimeManager.localToUtc(nextDateTime)),
          eventCount = reminder.eventCount + 1,
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
