package com.github.naz013.logic.reminder.usecase

import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.notification.NotificationApi

/**
 * Updates the permanent reminder notification in the status bar if enabled in preferences.
 */
class UpdatePermanentReminderNotificationUseCase(
  private val reminderPreferences: ReminderPreferences,
  private val notificationApi: NotificationApi,
) {
  suspend operator fun invoke() {
    if (reminderPreferences.isSbNotificationEnabled) {
      notificationApi.sendShowReminderPermanent()
      Logger.i(TAG, "Permanent reminder notification updated.")
    }
  }

  companion object {
    private const val TAG = "UpdatePermanentReminderNotificationUseCase"
  }
}
