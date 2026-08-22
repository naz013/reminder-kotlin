package com.github.naz013.logic.notificationaction

import androidx.core.content.ContextCompat
import com.github.naz013.common.ContextProvider
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.common.R as UiCommonR

class WearNotification(
  private val contextProvider: ContextProvider,
  private val notificationGateway: NotificationGateway,
) {
  fun show(
    id: Int,
    summary: String,
    secondaryText: String,
    groupName: String,
  ) {
    Logger.d(TAG, "showWearNotification: $secondaryText")
    val wearableNotificationBuilder = notificationGateway.builder(NotificationGateway.CHANNEL_REMINDER)
    wearableNotificationBuilder.setSmallIcon(DrawableCatalog.Fluent.Alert)
    wearableNotificationBuilder.setContentTitle(summary)
    wearableNotificationBuilder.setContentText(secondaryText)
    wearableNotificationBuilder.color =
      ContextCompat.getColor(contextProvider.themedContext, UiCommonR.color.secondaryBlue)
    wearableNotificationBuilder.setOngoing(false)
    wearableNotificationBuilder.setOnlyAlertOnce(true)
    wearableNotificationBuilder.setGroup(groupName)
    wearableNotificationBuilder.setGroupSummary(false)
    notificationGateway.notify(id, wearableNotificationBuilder.build())
  }

  companion object {
    private const val TAG = "WearNotification"
  }
}
