package com.elementary.tasks.core.utils

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb

class EnableThread(
  private val appDb: AppDb,
  private val eventControlFactory: EventControlFactory
) {

  fun run() {
    launchDefault {
      try {
        appDb.reminderDao().getAll(active = true, removed = false)
      } catch (e: Exception) {
        listOf()
      }.forEach { eventControlFactory.getController(it).enable() }
    }
  }
}
