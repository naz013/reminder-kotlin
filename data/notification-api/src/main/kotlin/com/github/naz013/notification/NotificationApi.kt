package com.github.naz013.notification

import com.github.naz013.domain.reminder.v2.NotificationSettings

interface NotificationApi {

  fun createChannels()

  fun reminderChannelId(settings: NotificationSettings): String

  fun sendShowReminderPermanent()

  fun cancel(id: Int)

  // Checked for Notification permission
  fun showReminderPermanent()

  fun showBirthdayPermanent()
}
