package com.elementary.tasks.core.services.action.reminder.process

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.github.naz013.domain.Reminder
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.intent.PendingIntentWrapper
import com.elementary.tasks.core.services.ReminderActionReceiver
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.reminder.ReminderDataProvider
import com.github.naz013.common.intent.IntentKeys
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.common.TextProvider
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.dialog.ReminderDialog29Activity
import com.github.naz013.logging.Logger

class ReminderHandlerSilent(
  private val reminderDataProvider: ReminderDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val wearNotification: WearNotification
) : ActionHandler<Reminder> {

  override suspend fun handle(data: Reminder) {
    showNotificationWithoutSound(data)
  }

  private fun showNotificationWithoutSound(reminder: Reminder) {
    Logger.d("showNotificationWithoutSound: ")
    val builder = NotificationCompat.Builder(contextProvider.context, Notifier.CHANNEL_REMINDER)
    builder.setSmallIcon(R.drawable.ic_fluent_alert)

    val notificationIntent = ReminderDialog29Activity.getLaunchIntent(
      contextProvider.context,
      reminder.uuId
    )
    val intent = PendingIntentWrapper.getActivity(
      contextProvider.context,
      reminder.uniqueId,
      notificationIntent,
      PendingIntent.FLAG_CANCEL_CURRENT
    )
    builder.setContentIntent(intent)
    builder.setAutoCancel(false)
    builder.setOngoing(true)
    builder.priority = NotificationCompat.PRIORITY_LOW
    builder.setContentTitle(reminder.summary)
    builder.setContentText(reminderDataProvider.getAppName())

    reminderDataProvider.getVibrationPattern()?.also { builder.setVibrate(it) }
    reminderDataProvider.getLedColor(reminder.color)?.also { builder.setLights(it, 500, 1000) }

    val dismissIntent = getActionReceiverIntent(ReminderActionReceiver.ACTION_HIDE, reminder.uuId)
    val piDismiss = PendingIntentWrapper.getBroadcast(
      contextProvider.context,
      reminder.uniqueId,
      dismissIntent,
      PendingIntent.FLAG_CANCEL_CURRENT
    )
    builder.addAction(
      R.drawable.ic_fluent_checkmark,
      textProvider.getText(R.string.ok),
      piDismiss
    )

    if (!Reminder.isGpsType(reminder.type)) {
      val snoozeIntent =
        getActionReceiverIntent(ReminderActionReceiver.ACTION_SNOOZE, reminder.uuId)
      val piSnooze = PendingIntentWrapper.getBroadcast(
        contextProvider.context,
        reminder.uniqueId,
        snoozeIntent,
        PendingIntent.FLAG_CANCEL_CURRENT
      )
      builder.addAction(
        R.drawable.ic_fluent_snooze,
        textProvider.getText(R.string.acc_button_snooze),
        piSnooze
      )
    }

    val isWear = prefs.isWearEnabled
    if (isWear) {
      builder.setOnlyAlertOnce(true)
      builder.setGroup("reminder")
      builder.setGroupSummary(true)
    }

    notifier.notify(reminder.uniqueId, builder.build())
    if (isWear) {
      wearNotification.show(
        reminder.uniqueId,
        reminder.summary,
        reminderDataProvider.getAppName(),
        "reminder"
      )
    }
  }

  private fun getActionReceiverIntent(action: String, id: String): Intent {
    return Intent(contextProvider.context, ReminderActionReceiver::class.java).apply {
      this.action = action
      putExtra(IntentKeys.INTENT_ID, id)
    }
  }
}
