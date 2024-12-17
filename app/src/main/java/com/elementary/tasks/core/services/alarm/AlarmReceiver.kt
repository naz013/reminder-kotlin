package com.elementary.tasks.core.services.alarm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.core.services.BaseBroadcast
import com.elementary.tasks.core.services.action.reminder.ReminderActionProcessor
import com.elementary.tasks.core.services.action.reminder.ReminderRepeatProcessor
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.SuperUtil
import com.github.naz013.logging.Logger
import org.koin.core.component.inject
import org.threeten.bp.LocalDateTime

class AlarmReceiver : BaseBroadcast() {

  private val reminderActionProcessor by inject<ReminderActionProcessor>()
  private val reminderRepeatProcessor by inject<ReminderRepeatProcessor>()

  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null) return
    val action = intent?.action ?: return

    Logger.d("onReceive: action = $action")
    Logger.d("onReceive: date time = ${LocalDateTime.now()}")

    when (action) {
      ACTION_REMINDER -> processReminder(intent.extras)
      ACTION_REMINDER_GPS -> SuperUtil.startGpsTracking(context)
      ACTION_REMINDER_REPEAT -> processRepeat(intent.extras)
    }
  }

  private fun processReminder(extras: Bundle?) {
    val id = extras?.getString(Constants.INTENT_ID) ?: return

    Logger.d("processReminder: id = $id")

    reminderActionProcessor.process(id)
  }

  private fun processRepeat(extras: Bundle?) {
    val id = extras?.getString(Constants.INTENT_ID) ?: return

    Logger.d("processRepeat: id = $id")

    reminderRepeatProcessor.process(id)
  }

  companion object {
    const val ACTION_REMINDER = "com.elementary.tasks.core.services.alarm.REMINDER"
    const val ACTION_REMINDER_GPS =
      "com.elementary.tasks.core.services.alarm.REMINDER_START_TRACKING"
    const val ACTION_REMINDER_REPEAT = "com.elementary.tasks.core.services.alarm.REMINDER_REPEAT"
  }
}
