package com.elementary.tasks.core.services.action

import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.common.ContextProvider
import com.github.naz013.logging.Logger

class WearNotification(
  private val contextProvider: ContextProvider,
  private val notifier: Notifier
) {

  fun show(
    id: Int,
    summary: String,
    secondaryText: String,
    groupName: String
  ) {
    Logger.d("showWearNotification: $secondaryText")
    val wearableNotificationBuilder =
      NotificationCompat.Builder(contextProvider.context, Notifier.CHANNEL_REMINDER)
    wearableNotificationBuilder.setSmallIcon(R.drawable.ic_fluent_alert)
    wearableNotificationBuilder.setContentTitle(summary)
    wearableNotificationBuilder.setContentText(secondaryText)
    wearableNotificationBuilder.color =
      ContextCompat.getColor(contextProvider.themedContext, R.color.secondaryBlue)
    wearableNotificationBuilder.setOngoing(false)
    wearableNotificationBuilder.setOnlyAlertOnce(true)
    wearableNotificationBuilder.setGroup(groupName)
    wearableNotificationBuilder.setGroupSummary(false)
    notifier.notify(id, wearableNotificationBuilder.build())
  }
}
