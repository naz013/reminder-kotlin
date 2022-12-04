package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions

class PermanentReminderReceiver : BaseBroadcast() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (!prefs.isSbNotificationEnabled) {
      notifier.cancel(PERM_ID)
      return
    }
    if (intent != null) {
      val action = intent.action
      if (action != null && action.matches(ACTION_SHOW.toRegex())) {
        notifier.showReminderPermanent()
      } else {
        notifier.cancel(PERM_ID)
      }
    } else {
      notifier.cancel(PERM_ID)
    }
  }

  companion object {

    const val PERM_ID = 356664
    private const val ACTION_SHOW = Actions.Reminder.ACTION_SB_SHOW
    private const val ACTION_HIDE = Actions.Reminder.ACTION_SB_HIDE

    fun show(context: Context) {
      context.sendBroadcast(
        Intent(context, PermanentReminderReceiver::class.java)
          .setAction(ACTION_SHOW)
      )
    }

    fun hide(context: Context) {
      context.sendBroadcast(
        Intent(context, PermanentReminderReceiver::class.java)
          .setAction(ACTION_HIDE)
      )
    }
  }
}
