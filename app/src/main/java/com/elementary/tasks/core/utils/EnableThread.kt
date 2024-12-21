package com.elementary.tasks.core.utils

import com.elementary.tasks.core.controller.EventControlFactory
import com.github.naz013.repository.ReminderRepository

class EnableThread(
  private val reminderRepository: ReminderRepository,
  private val eventControlFactory: EventControlFactory
) {

  fun run() {
    launchDefault {
      try {
        reminderRepository.getAll(active = true, removed = false)
      } catch (e: Exception) {
        listOf()
      }.forEach { eventControlFactory.getController(it).enable() }
    }
  }
}
