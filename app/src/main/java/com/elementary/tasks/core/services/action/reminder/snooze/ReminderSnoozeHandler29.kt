package com.elementary.tasks.core.services.action.reminder.snooze

import android.os.Build
import androidx.annotation.RequiresApi
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs

@RequiresApi(Build.VERSION_CODES.Q)
class ReminderSnoozeHandler29(
  private val notifier: Notifier,
  private val eventControlFactory: EventControlFactory,
  private val prefs: Prefs
) : ActionHandler<Reminder> {

  override fun handle(data: Reminder) {
    eventControlFactory.getController(data).setDelay(prefs.snoozeTime)
    notifier.cancel(data.uniqueId)
  }
}
