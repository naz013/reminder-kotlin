package com.github.naz013.repository

import com.github.naz013.domain.reminder.v2.NotificationSettings

/** The base of the 3-level notification-customization hierarchy: always fully populated. */
interface ReminderSettingsRepository {
  fun getNotificationDefaults(): NotificationSettings
  fun setNotificationDefaults(settings: NotificationSettings)
}
