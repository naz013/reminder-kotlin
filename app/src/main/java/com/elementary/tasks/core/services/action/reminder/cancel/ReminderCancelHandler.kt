package com.elementary.tasks.core.services.action.reminder.cancel

import androidx.core.content.ContextCompat
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.EventOperationalService
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.utils.Notifier

class ReminderCancelHandler(
  private val notifier: Notifier,
  private val eventControlFactory: EventControlFactory,
  private val contextProvider: ContextProvider
) : ActionHandler<Reminder> {

  override fun handle(data: Reminder) {
    eventControlFactory.getController(data).next()

    ContextCompat.startForegroundService(
      contextProvider.context,
      EventOperationalService.getIntent(
        contextProvider.context,
        data.uuId,
        EventOperationalService.TYPE_REMINDER,
        EventOperationalService.ACTION_STOP,
        data.uniqueId
      )
    )

    notifier.cancel(data.uniqueId)
  }
}
