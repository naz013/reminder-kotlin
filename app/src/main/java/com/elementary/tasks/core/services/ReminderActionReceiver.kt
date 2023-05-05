package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions
import com.elementary.tasks.core.services.action.reminder.ReminderActionProcessor
import com.elementary.tasks.core.utils.Constants
import org.koin.core.component.inject
import timber.log.Timber

class ReminderActionReceiver : BaseBroadcast() {

  private val reminderActionProcessor by inject<ReminderActionProcessor>()

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent != null) {
      val action = intent.action
      val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
      Timber.d("onReceive: $action, id=$id")
      if (action != null && id.isNotEmpty()) {
        when {
          action.matches(ACTION_HIDE.toRegex()) -> reminderActionProcessor.cancel(id)
          action.matches(ACTION_SNOOZE.toRegex()) -> reminderActionProcessor.snooze(id)
        }
      }
    }
  }

  companion object {
    const val ACTION_HIDE = Actions.Reminder.ACTION_HIDE_SIMPLE
    const val ACTION_SNOOZE = Actions.Reminder.ACTION_SNOOZE
  }
}
