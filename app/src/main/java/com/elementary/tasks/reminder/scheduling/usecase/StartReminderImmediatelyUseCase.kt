package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.reminder.usecase.SaveReminderUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger

/**
 * Starts a reminder immediately without time validation checks.
 * Corresponds to EventControl.justStart() method.
 *
 * This use case bypasses the normal activation checks and starts the reminder
 * unconditionally. Use this when:
 * - Creating a new reminder that should start immediately
 * - Restarting reminders after system boot
 * - User explicitly clicks "start now" button
 * - Importing reminders that should be active
 *
 * The difference from ActivateReminderUseCase is that this doesn't check
 * if the event time is current - it starts regardless.
 *
 * @param saveReminderUseCase Use case to save the reminder
 * @param activateReminderUseCase Use case to activate the reminder
 */
class StartReminderImmediatelyUseCase(
  private val saveReminderUseCase: SaveReminderUseCase,
  private val activateReminderUseCase: ActivateReminderUseCase
) {

  /**
   * Starts the reminder immediately regardless of time checks.
   *
   * This will:
   * 1. Set the reminder to active state
   * 2. Clear removed, notification shown, and locked flags
   * 3. Update sync state
   * 4. Save the reminder
   * 5. Activate it (schedule jobs, export to services, etc.)
   *
   * @param reminder The reminder to start
   * @return The updated reminder
   */
  suspend operator fun invoke(reminder: Reminder): Reminder {
    Logger.d(TAG, "Starting reminder immediately for id=${reminder.uuId}")

    // Force the reminder to active state
    val updatedReminder = reminder.copy(
      isActive = true,
      isRemoved = false,
      isNotificationShown = false,
      isLocked = false,
      syncState = SyncState.WaitingForUpload,
      version = reminder.version + 1
    )

    // Save the state change
    saveReminderUseCase(updatedReminder)

    Logger.i(TAG, "Reminder id=${reminder.uuId} state prepared, now activating")

    // Now activate it (this will schedule jobs, export to calendar/tasks, etc.)
    return activateReminderUseCase(updatedReminder)
  }

  companion object {
    private const val TAG = "StartReminderImmediatelyUseCase"
  }
}

