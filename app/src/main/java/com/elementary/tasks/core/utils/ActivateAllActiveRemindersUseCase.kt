package com.elementary.tasks.core.utils

import com.elementary.tasks.reminder.scheduling.usecase.ActivateReminderUseCase
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivateAllActiveRemindersUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val activateReminderUseCase: ActivateReminderUseCase,
) {
  private val coroutineScope = CoroutineScope(Dispatchers.IO)

  fun run() {
    coroutineScope.launch {
      reminderV2Repository.getAll(active = true, removed = false).forEach {
        activateReminderUseCase(it)
      }
    }
  }
}
