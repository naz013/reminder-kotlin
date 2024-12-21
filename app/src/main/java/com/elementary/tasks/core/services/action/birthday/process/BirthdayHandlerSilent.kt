package com.elementary.tasks.core.services.action.birthday.process

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.dialog.ShowBirthday29Activity
import com.github.naz013.feature.common.android.ContextProvider
import com.elementary.tasks.core.os.PendingIntentWrapper
import com.elementary.tasks.core.services.BirthdayActionReceiver
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.birthday.BirthdayDataProvider
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.feature.common.android.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.domain.Birthday
import com.github.naz013.logging.Logger

class BirthdayHandlerSilent(
  private val birthdayDataProvider: BirthdayDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val wearNotification: WearNotification,
  private val dateTimeManager: DateTimeManager
) : ActionHandler<Birthday> {

  override suspend fun handle(data: Birthday) {
    showNotificationWithoutSound(data)
  }

  private fun showNotificationWithoutSound(birthday: Birthday) {
    Logger.d("showNotificationWithoutSound: ")
    val builder = NotificationCompat.Builder(contextProvider.context, Notifier.CHANNEL_REMINDER)
    builder.setSmallIcon(R.drawable.ic_fluent_alert)

    val notificationIntent = ShowBirthday29Activity.getLaunchIntent(
      contextProvider.context,
      birthday.uuId
    )
    val intent = PendingIntentWrapper.getActivity(
      contextProvider.context,
      birthday.uniqueId,
      notificationIntent,
      PendingIntent.FLAG_CANCEL_CURRENT
    )
    builder.setContentIntent(intent)
    builder.setAutoCancel(false)
    builder.setOngoing(true)
    builder.priority = NotificationCompat.PRIORITY_LOW
    builder.setContentTitle(birthday.name)
    if (!birthday.ignoreYear) {
      builder.setContentText(dateTimeManager.getAgeFormatted(birthday.date))
    }

    birthdayDataProvider.getVibrationPattern()?.also { builder.setVibrate(it) }
    if (Module.isPro && birthdayDataProvider.isBirthdayLed()) {
      builder.setLights(birthdayDataProvider.getLedColor(), 500, 1000)
    }

    getActionReceiverIntent(BirthdayActionReceiver.ACTION_HIDE, birthday.uuId).let {
      PendingIntentWrapper.getBroadcast(
        contextProvider.context,
        birthday.uniqueId,
        it,
        PendingIntent.FLAG_CANCEL_CURRENT
      )
    }.also {
      builder.addAction(R.drawable.ic_fluent_checkmark, textProvider.getText(R.string.ok), it)
    }

    if (birthday.number.isNotEmpty()) {
      getActionReceiverIntent(BirthdayActionReceiver.ACTION_CALL, birthday.uuId).let {
        PendingIntentWrapper.getBroadcast(
          contextProvider.context,
          birthday.uniqueId,
          it,
          PendingIntent.FLAG_CANCEL_CURRENT
        )
      }.also {
        builder.addAction(
          R.drawable.ic_fluent_phone,
          textProvider.getText(R.string.make_call),
          it
        )
      }

      getActionReceiverIntent(BirthdayActionReceiver.ACTION_SMS, birthday.uuId).let {
        PendingIntentWrapper.getBroadcast(
          contextProvider.context,
          birthday.uniqueId,
          it,
          PendingIntent.FLAG_CANCEL_CURRENT
        )
      }.also {
        builder.addAction(
          R.drawable.ic_fluent_chat,
          textProvider.getText(R.string.send_sms),
          it
        )
      }
    }

    val isWear = prefs.isWearEnabled
    if (isWear) {
      builder.setOnlyAlertOnce(true)
      builder.setGroup("birthday")
      builder.setGroupSummary(true)
    }

    notifier.notify(birthday.uniqueId, builder.build())
    if (isWear) {
      wearNotification.show(
        birthday.uniqueId,
        birthday.name,
        birthdayDataProvider.getAppName(),
        "birthday"
      )
    }
  }

  private fun getActionReceiverIntent(action: String, id: String): Intent {
    return Intent(contextProvider.context, BirthdayActionReceiver::class.java).apply {
      this.action = action
      putExtra(Constants.INTENT_ID, id)
    }
  }
}
