package com.elementary.tasks.core.services.action.missedcall.process

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.os.PendingIntentWrapper
import com.elementary.tasks.core.services.MissedCallActionReceiver
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.missedcall.MissedCallDataProvider
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.missed_calls.MissedCallDialog29Activity
import com.elementary.tasks.missed_calls.MissedCallDialogActivity
import timber.log.Timber

@Deprecated("After S")
class MissedCallHandlerSilent(
  private val missedCallDataProvider: MissedCallDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val wearNotification: WearNotification,
  private val contactsReader: ContactsReader
) : ActionHandler<MissedCall> {

  override fun handle(data: MissedCall) {
    showNotificationWithoutSound(data)
  }

  private fun showNotificationWithoutSound(missedCall: MissedCall) {
    Timber.d("showNotificationWithoutSound: ")
    val builder = NotificationCompat.Builder(contextProvider.context, Notifier.CHANNEL_REMINDER)

    val name = contactsReader.getNameFromNumber(missedCall.number) ?: missedCall.number

    val notificationIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      MissedCallDialog29Activity.getLaunchIntent(contextProvider.context, missedCall.number)
    } else {
      MissedCallDialogActivity.getLaunchIntent(contextProvider.context, missedCall.number)
    }
    val intent = PendingIntentWrapper.getActivity(
      contextProvider.context,
      missedCall.uniqueId,
      notificationIntent,
      PendingIntent.FLAG_CANCEL_CURRENT
    )
    builder.setContentIntent(intent)
    builder.setAutoCancel(false)
    builder.setOngoing(true)
    builder.priority = NotificationCompat.PRIORITY_LOW
    builder.setContentTitle(name)
    builder.setContentText(missedCallDataProvider.getAppName())
    builder.setSmallIcon(R.drawable.ic_twotone_call_white)
    builder.color = ThemeProvider.getPrimaryColor(contextProvider.context)
    builder.setCategory(NotificationCompat.CATEGORY_REMINDER)

    missedCallDataProvider.getVibrationPattern()?.also { builder.setVibrate(it) }
    missedCallDataProvider.getLedColor()?.also { builder.setLights(it, 500, 1000) }

    getActionReceiverIntent(MissedCallActionReceiver.ACTION_HIDE, missedCall.number).let {
      PendingIntentWrapper.getBroadcast(
        contextProvider.context,
        missedCall.uniqueId,
        it,
        PendingIntent.FLAG_CANCEL_CURRENT
      )
    }.also {
      builder.addAction(R.drawable.ic_twotone_done_white, textProvider.getText(R.string.ok), it)
    }

    if (missedCall.number.isNotEmpty()) {
      getActionReceiverIntent(MissedCallActionReceiver.ACTION_CALL, missedCall.number).let {
        PendingIntentWrapper.getBroadcast(
          contextProvider.context,
          missedCall.uniqueId,
          it,
          PendingIntent.FLAG_CANCEL_CURRENT
        )
      }.also {
        builder.addAction(
          R.drawable.ic_twotone_call_white,
          textProvider.getText(R.string.make_call),
          it
        )
      }

      getActionReceiverIntent(MissedCallActionReceiver.ACTION_SMS, missedCall.number).let {
        PendingIntentWrapper.getBroadcast(
          contextProvider.context,
          missedCall.uniqueId,
          it,
          PendingIntent.FLAG_CANCEL_CURRENT
        )
      }.also {
        builder.addAction(
          R.drawable.ic_twotone_message_white,
          textProvider.getText(R.string.send_sms),
          it
        )
      }
    }

    val isWear = prefs.isWearEnabled
    if (isWear) {
      builder.setOnlyAlertOnce(true)
      builder.setGroup("missed_call")
      builder.setGroupSummary(true)
    }

    notifier.notify(missedCall.uniqueId, builder.build())
    if (isWear) {
      wearNotification.show(
        missedCall.uniqueId,
        name,
        missedCallDataProvider.getAppName(),
        "missed_call"
      )
    }
  }

  private fun getActionReceiverIntent(action: String, number: String): Intent {
    return Intent(contextProvider.context, MissedCallActionReceiver::class.java).apply {
      this.action = action
      putExtra(Constants.INTENT_ID, number)
    }
  }
}
