package com.github.naz013.logic.notificationaction

import android.app.Notification
import androidx.core.app.NotificationCompat

/**
 * Seam over app's `Notifier`, which this module can't depend on. Only the general
 * build-post-cancel mechanics needed by the alert/action pipeline are exposed here - channel
 * creation and the permanent-notification/reminder-channel-resolution behavior stay behind the
 * existing `NotificationApi` seam (`data:notification-api`). Implemented in `app` by delegating
 * to `Notifier` - see `NotificationGatewayImpl`.
 */
interface NotificationGateway {
  fun builder(channelId: String): NotificationCompat.Builder

  fun notify(id: Int, notification: Notification)

  fun cancel(id: Int)

  companion object {
    /**
     * Must stay equal to `Notifier.CHANNEL_REMINDER` in `app` - duplicated here because this
     * module can't depend on `app`'s `Notifier` to read the constant directly.
     */
    const val CHANNEL_REMINDER = "reminder.channel.events"

    /**
     * Must stay equal to `Notifier.CHANNEL_SYSTEM` in `app` - same reasoning as [CHANNEL_REMINDER].
     */
    const val CHANNEL_SYSTEM = "reminder.channel.system"
  }
}
