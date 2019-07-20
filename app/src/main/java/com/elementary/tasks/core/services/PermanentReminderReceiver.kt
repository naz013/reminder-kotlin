package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions

class PermanentReminderReceiver : BaseBroadcast() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (!prefs.isSbNotificationEnabled) {
            notifier.hideNotification(PERM_ID)
        }
        if (intent != null) {
            val action = intent.action
            if (action != null && action.matches(ACTION_SHOW.toRegex())) {
                notifier.showReminderPermanent()
            } else {
                notifier.hideNotification(PERM_ID)
            }
        } else {
            notifier.hideNotification(PERM_ID)
        }
    }

    companion object {

        const val PERM_ID = 356664
        const val ACTION_SHOW = Actions.Reminder.ACTION_SB_SHOW
        const val ACTION_HIDE = Actions.Reminder.ACTION_SB_HIDE
    }
}
