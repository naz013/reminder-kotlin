package com.elementary.tasks.core.services.action.reminder.process

import android.app.PendingIntent
import android.content.BroadcastReceiver
import com.elementary.tasks.R
import com.elementary.tasks.core.services.ReminderActionReceiver
import com.elementary.tasks.core.services.action.NotificationAction
import com.elementary.tasks.core.services.action.NotificationAlertActionHandler
import com.elementary.tasks.core.services.action.NotificationStyle
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.reminder.ReminderDataProvider
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.dialog.ReminderActionActivity
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderV2

class ReminderNotificationHandler(
  private val reminderDataProvider: ReminderDataProvider,
  private val notificationSettings: NotificationSettings,
  contextProvider: ContextProvider,
  textProvider: TextProvider,
  private val notifier: Notifier,
  private val prefs: Prefs,
  wearNotification: WearNotification,
  style: NotificationStyle,
) : NotificationAlertActionHandler<ReminderV2>(
    contextProvider = contextProvider,
    textProvider = textProvider,
    notifier = notifier,
    prefs = prefs,
    wearNotification = wearNotification,
    style = style,
  ) {

  override val groupKey = "reminder"
  override val logTag = "ReminderNotificationHandler"

  override fun receiverClass(): Class<out BroadcastReceiver> = ReminderActionReceiver::class.java

  override fun dismissActionKey(): String = ReminderActionReceiver.ACTION_HIDE

  public override fun isOngoing(data: ReminderV2): Boolean = !prefs.isDefaultSwipeToDismissEnabled

  override fun extraActions(data: ReminderV2): List<NotificationAction> =
    if (data.places.isEmpty()) {
      listOf(
        NotificationAction(
          icon = R.drawable.ic_fluent_snooze,
          label = textProvider.getText(R.string.acc_button_snooze),
          actionKey = ReminderActionReceiver.ACTION_SNOOZE,
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
  public override fun channelId(data: ReminderV2): String = notifier.reminderChannelId(notificationSettings)

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
