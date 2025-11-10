package com.elementary.tasks.reminder.scheduling.usecase.notification

import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logging.Logger

/**
 * Updates the permanent reminder notification in the status bar if enabled in preferences.
 */
class UpdatePermanentReminderNotificationUseCase(
  private val prefs: Prefs,
  private val notifier: Notifier,
) {

  suspend operator fun invoke() {
    if (prefs.isSbNotificationEnabled) {
      notifier.sendShowReminderPermanent()
      Logger.i(TAG, "Permanent reminder notification updated.")
    }
  }

  companion object {
    private const val TAG = "UpdatePermanentReminderNotificationUseCase"
  }
}
