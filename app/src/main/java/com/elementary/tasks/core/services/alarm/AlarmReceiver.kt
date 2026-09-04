package com.elementary.tasks.core.services.alarm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.core.services.BaseBroadcast
import com.elementary.tasks.core.utils.SuperUtil
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import com.github.naz013.logic.notificationaction.calendarevent.GoogleCalendarEventActionProcessor
import com.github.naz013.logic.notificationaction.reminder.ReminderActionProcessor
import com.github.naz013.logic.notificationaction.reminder.ReminderRepeatProcessor
import org.koin.core.component.inject
import org.threeten.bp.LocalDateTime

class AlarmReceiver : BaseBroadcast() {
  private val reminderActionProcessor by inject<ReminderActionProcessor>()
  private val reminderRepeatProcessor by inject<ReminderRepeatProcessor>()
  private val googleCalendarEventActionProcessor by inject<GoogleCalendarEventActionProcessor>()

  override fun onReceive(
    context: Context?,
    intent: Intent?,
  ) {
    if (context == null) return
    val action = intent?.action ?: return

    Logger.d(TAG, "onReceive: action = $action")
    Logger.d(TAG, "onReceive: date time = ${LocalDateTime.now()}")

    when (action) {
      ACTION_REMINDER -> processReminder(intent.extras)
      ACTION_REMINDER_GPS -> SuperUtil.startGpsTracking(context)
      ACTION_REMINDER_REPEAT -> processRepeat(intent.extras)
      ACTION_CALENDAR_EVENT -> processCalendarEvent(intent.extras)
    }
  }

  private fun processReminder(extras: Bundle?) {
    val id = extras?.getString(IntentKeys.INTENT_ID) ?: return

    Logger.d(TAG, "processReminder: id = $id")

    reminderActionProcessor.process(id)
  }

  private fun processCalendarEvent(extras: Bundle?) {
    val id = extras?.getString(IntentKeys.INTENT_ID) ?: return

    Logger.d(TAG, "processCalendarEvent: id = $id")

    googleCalendarEventActionProcessor.process(id)
  }

  private fun processRepeat(extras: Bundle?) {
    val id = extras?.getString(IntentKeys.INTENT_ID) ?: return
    val repeatCount = extras.getInt(IntentKeys.INTENT_COUNT, 0)

    Logger.d(TAG, "processRepeat: id = $id, repeatCount = $repeatCount")

    reminderRepeatProcessor.process(id, repeatCount)
  }

  companion object {
    private const val TAG = "AlarmReceiver"
    const val ACTION_REMINDER = "com.elementary.tasks.core.services.alarm.REMINDER"
    const val ACTION_REMINDER_GPS =
      "com.elementary.tasks.core.services.alarm.REMINDER_START_TRACKING"
    const val ACTION_REMINDER_REPEAT = "com.elementary.tasks.core.services.alarm.REMINDER_REPEAT"
    const val ACTION_CALENDAR_EVENT = "com.elementary.tasks.core.services.alarm.CALENDAR_EVENT"
  }
}
