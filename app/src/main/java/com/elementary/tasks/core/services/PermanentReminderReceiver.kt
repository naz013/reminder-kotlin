package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions
import com.elementary.tasks.core.utils.Notifier

class PermanentReminderReceiver : BaseBroadcast() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (!prefs.isSbNotificationEnabled) {
      Notifier.hideNotification(context, PERM_ID)
      return
    }
    if (intent != null) {
      val action = intent.action
      if (action != null && action.matches(ACTION_SHOW.toRegex())) {
        Notifier.showReminderPermanent(context, prefs)
      } else {
        Notifier.hideNotification(context, PERM_ID)
      }
    } else {
      Notifier.hideNotification(context, PERM_ID)
    }
  }

  companion object {

    const val PERM_ID = 356664
    const val ACTION_SHOW = Actions.Reminder.ACTION_SB_SHOW
    const val ACTION_HIDE = Actions.Reminder.ACTION_SB_HIDE
  }
}
