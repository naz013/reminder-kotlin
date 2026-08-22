package com.elementary.tasks.module.logicnotificationaction

import android.app.Notification
import androidx.core.app.NotificationCompat
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.logic.notificationaction.NotificationGateway

class NotificationGatewayImpl(
  private val notifier: Notifier,
) : NotificationGateway {
  override fun builder(channelId: String): NotificationCompat.Builder = notifier.getNotificationBuilder(channelId)

  override fun notify(id: Int, notification: Notification) = notifier.notify(id, notification)

  override fun cancel(id: Int) = notifier.cancel(id)
}
