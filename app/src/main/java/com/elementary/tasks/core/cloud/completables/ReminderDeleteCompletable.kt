package com.elementary.tasks.core.cloud.completables

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder

class ReminderDeleteCompletable : Completable<Reminder> {

  override suspend fun action(t: Reminder) {
    EventControlFactory.getController(t).stop()
  }
}