package com.elementary.tasks.reminder.scheduling.usecase

import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger

/**
 * Toggles the reminder state between active and inactive.
 * Corresponds to EventControl.onOff() method.
 *
 * This use case provides a single operation to switch a reminder on or off,
 * commonly used in UI toggle switches or buttons.
 *
 * @param activateReminderUseCase Use case to activate the reminder
 * @param deactivateReminderUseCase Use case to deactivate the reminder
 */
class ToggleReminderStateUseCase(
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val deactivateReminderUseCase: DeactivateReminderUseCase
) {

  /**
   * Toggles the reminder state.
   *
   * If the reminder is currently active, it will be deactivated.
   * If the reminder is currently inactive, it will be activated.
   *
   * @param reminder The reminder to toggle
   * @return Pair of (new active state, updated reminder)
   */
  suspend operator fun invoke(reminder: Reminder): Pair<Boolean, Reminder> {
    Logger.d(TAG, "Toggling reminder state for id=${reminder.uuId}, currentState=${reminder.isActive}")

    return if (reminder.isActive) {
      Logger.i(TAG, "Deactivating reminder id=${reminder.uuId}")
      false to deactivateReminderUseCase(reminder)
    } else {
      Logger.i(TAG, "Activating reminder id=${reminder.uuId}")
      true to activateReminderUseCase(reminder)
    }
  }

  companion object {
    private const val TAG = "ToggleReminderStateUseCase"
  }
}

