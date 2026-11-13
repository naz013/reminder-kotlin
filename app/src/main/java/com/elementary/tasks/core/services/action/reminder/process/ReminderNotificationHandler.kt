package com.elementary.tasks.core.services.action.reminder.process

import android.app.PendingIntent
import android.content.BroadcastReceiver
import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.services.ReminderActionReceiver
import com.elementary.tasks.core.services.action.reminder.ReminderDataProvider
import com.elementary.tasks.reminder.dialog.ReminderActionActivity
import com.elementary.tasks.reminder.dialog.SnoozePickerActivity
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.notificationaction.NotificationAction
import com.github.naz013.logic.notificationaction.NotificationAlertActionHandler
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.logic.notificationaction.NotificationStyle
import com.github.naz013.logic.notificationaction.WearNotification
import com.github.naz013.logic.notificationaction.WearPreferences
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.notification.NotificationApi
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.common.R as UiCommonR

class ReminderNotificationHandler(
  private val reminderDataProvider: ReminderDataProvider,
  private val notificationSettings: NotificationSettings,
  contextProvider: ContextProvider,
  textProvider: TextProvider,
  notificationGateway: NotificationGateway,
  wearPreferences: WearPreferences,
  private val notificationApi: NotificationApi,
  private val reminderPreferences: ReminderPreferences,
  wearNotification: WearNotification,
  style: NotificationStyle,
) : NotificationAlertActionHandler<ReminderV2>(
    contextProvider = contextProvider,
    textProvider = textProvider,
    notificationGateway = notificationGateway,
    wearPreferences = wearPreferences,
    wearNotification = wearNotification,
    style = style,
  ) {

  override val groupKey = "reminder"
  override val logTag = "ReminderNotificationHandler"

  override fun receiverClass(): Class<out BroadcastReceiver> = ReminderActionReceiver::class.java

  override fun dismissActionKey(): String = ReminderActionReceiver.ACTION_HIDE

  public override fun isOngoing(data: ReminderV2): Boolean = !reminderPreferences.isDefaultSwipeToDismissEnabled

  public override fun useFullScreenIntent(data: ReminderV2): Boolean = notificationSettings.wakeScreen

  public override fun notificationCategory(data: ReminderV2): String? =
    when (notificationSettings.category) {
      ReminderNotificationCategory.ALARM -> NotificationCompat.CATEGORY_ALARM
      ReminderNotificationCategory.EVENT -> NotificationCompat.CATEGORY_EVENT
      ReminderNotificationCategory.CALL -> NotificationCompat.CATEGORY_CALL
      ReminderNotificationCategory.DEFAULT -> NotificationCompat.CATEGORY_REMINDER
    }

  override fun extraActions(data: ReminderV2): List<NotificationAction> =
    if (data.places.isEmpty()) {
      listOf(
        NotificationAction(
          icon = DrawableCatalog.Fluent.Snooze,
          label = textProvider.getText(R.string.acc_button_snooze),
          actionKey = ReminderActionReceiver.ACTION_SNOOZE,
        ),
        NotificationAction(
          icon = DrawableCatalog.Fluent.AlertSnooze,
          label = textProvider.getText(UiCommonR.string.action_snooze_picker),
          actionKey = ReminderActionReceiver.ACTION_SNOOZE_PICKER,
          activityIntent = SnoozePickerActivity.getLaunchIntent(contextProvider.context, data.uuId),
        ),
      )
    } else {
      emptyList()
    }

  override fun uniqueId(data: ReminderV2): Int = data.uniqueId

  override fun uuId(data: ReminderV2): String = data.uuId

  override fun contentTitle(data: ReminderV2): String = data.summary

  override fun contentText(data: ReminderV2): String = reminderDataProvider.getAppName()

  override fun domainIcon(data: ReminderV2): Int = R.drawable.ic_fluent_alert

  // Widened to public (base declarations are protected) so these - the resolved-settings ->
  // Android-API mapping this fix is about - are directly unit-testable.
  public override fun channelId(data: ReminderV2): String = notificationApi.reminderChannelId(notificationSettings)

  public override fun defaultPriority(data: ReminderV2): Int =
    reminderDataProvider.priority(notificationSettings.priority.ordinal)

  public override fun vibrationPattern(data: ReminderV2): LongArray? =
    if (notificationSettings.vibrate) notificationSettings.vibrationPattern?.toLongArray() else null

  public override fun ledColor(data: ReminderV2): Int? = reminderDataProvider.getLedColor(notificationSettings.color)

  override fun appName(data: ReminderV2): String = reminderDataProvider.getAppName()

  override fun contentPendingIntent(data: ReminderV2): PendingIntent =
    PendingIntentWrapper.getActivity(
      contextProvider.context,
      data.uniqueId,
      ReminderActionActivity.getLaunchIntent(contextProvider.context, data.uuId),
      PendingIntent.FLAG_CANCEL_CURRENT,
    )
}
