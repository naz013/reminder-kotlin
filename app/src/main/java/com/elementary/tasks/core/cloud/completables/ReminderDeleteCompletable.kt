package com.elementary.tasks.core.cloud.completables

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder

class ReminderDeleteCompletable(
  private val eventControlFactory: EventControlFactory
) : Completable<Reminder> {

  override suspend fun action(t: Reminder) {
    eventControlFactory.getController(t).stop()
  }
}
