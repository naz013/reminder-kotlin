package com.elementary.tasks.module.logicreminder

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.logic.reminder.ReminderNotifier
import com.github.naz013.ui.common.theme.ColorProvider

/**
 * `logic-reminder`/`feature-reminder` can't depend on `app`'s `Notifier` or its resources, so this
 * wraps the favorite/keep-as-notification flow behind [ReminderNotifier] instead.
 */
class AppReminderNotifier(
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val colorProvider: ColorProvider,
  private val textProvider: TextProvider,
  private val buildInfo: BuildInfo,
) : ReminderNotifier {
  override fun showFavoriteNotification(
    text: String,
    notificationId: Int,
  ) {
    val builder = notifier.getNotificationBuilder(Notifier.CHANNEL_REMINDER)
    builder.setContentTitle(text)
    val appName: String =
      if (buildInfo.isPro) {
        textProvider.getString(R.string.app_name_pro)
      } else {
        textProvider.getString(R.string.app_name)
      }
    builder.setContentText(appName)
    builder.setSmallIcon(R.drawable.ic_fluent_alert)
    builder.color = colorProvider.getColor(R.color.secondaryBlue)
    val isWear = prefs.isWearEnabled
    if (isWear) {
      builder.setOnlyAlertOnce(true)
      builder.setGroup("GROUP")
      builder.setGroupSummary(true)
    }
    notifier.notify(notificationId, builder.build())
    if (isWear) {
      showWearNotification(text, appName, notificationId)
    }
  }

  private fun showWearNotification(
    text: String,
    secondaryText: String,
    notificationId: Int,
  ) {
    val wearableNotificationBuilder = notifier.getNotificationBuilder(Notifier.CHANNEL_REMINDER)
    wearableNotificationBuilder.setSmallIcon(R.drawable.ic_fluent_alert)
    wearableNotificationBuilder.setContentTitle(text)
    wearableNotificationBuilder.setContentText(secondaryText)
    wearableNotificationBuilder.color = colorProvider.getColor(R.color.secondaryBlue)
    wearableNotificationBuilder.setOngoing(false)
    wearableNotificationBuilder.setOnlyAlertOnce(true)
    wearableNotificationBuilder.setGroup("reminder")
    wearableNotificationBuilder.setGroupSummary(false)
    notifier.notify(notificationId, wearableNotificationBuilder.build())
  }
}
