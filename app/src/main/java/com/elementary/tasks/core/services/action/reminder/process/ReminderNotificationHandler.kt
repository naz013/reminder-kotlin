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
import com.github.naz013.domain.Reminder

class ReminderNotificationHandler(
  private val reminderDataProvider: ReminderDataProvider,
  contextProvider: ContextProvider,
  textProvider: TextProvider,
  notifier: Notifier,
  prefs: Prefs,
  wearNotification: WearNotification,
  style: NotificationStyle,
) : NotificationAlertActionHandler<Reminder>(
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

  override fun extraActions(data: Reminder): List<NotificationAction> =
    if (!Reminder.isGpsType(data.type)) {
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

  override fun uniqueId(data: Reminder): Int = data.uniqueId

  override fun uuId(data: Reminder): String = data.uuId

  override fun contentTitle(data: Reminder): String = data.summary

  override fun contentText(data: Reminder): String = reminderDataProvider.getAppName()

  override fun domainIcon(data: Reminder): Int = R.drawable.ic_fluent_alert

  override fun defaultPriority(data: Reminder): Int = reminderDataProvider.priority(data.priority)

  override fun vibrationPattern(data: Reminder): LongArray? = reminderDataProvider.getVibrationPattern()

  override fun ledColor(data: Reminder): Int? = reminderDataProvider.getLedColor(data.color)

  override fun appName(data: Reminder): String = reminderDataProvider.getAppName()

  override fun contentPendingIntent(data: Reminder): PendingIntent =
    PendingIntentWrapper.getActivity(
      contextProvider.context,
      data.uniqueId,
      ReminderActionActivity.getLaunchIntent(contextProvider.context, data.uuId),
      PendingIntent.FLAG_CANCEL_CURRENT,
    )
}
