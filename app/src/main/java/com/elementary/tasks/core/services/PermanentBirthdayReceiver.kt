package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions

class PermanentBirthdayReceiver : BaseBroadcast() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (!prefs.isBirthdayPermanentEnabled) {
      notifier.cancel(BIRTHDAY_PERM_ID)
      return
    }
    if (intent != null) {
      val action = intent.action
      if (action != null && action.matches(ACTION_SHOW.toRegex())) {
        notifier.showBirthdayPermanent()
      } else {
        notifier.cancel(BIRTHDAY_PERM_ID)
      }
    } else {
      notifier.cancel(BIRTHDAY_PERM_ID)
    }
  }

  companion object {

    const val BIRTHDAY_PERM_ID = 356665
    const val ACTION_SHOW = Actions.Birthday.ACTION_SB_SHOW
    const val ACTION_HIDE = Actions.Birthday.ACTION_SB_HIDE
  }
}
