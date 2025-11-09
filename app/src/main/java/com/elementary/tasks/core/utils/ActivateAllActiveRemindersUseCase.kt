package com.elementary.tasks.core.utils

import com.elementary.tasks.reminder.scheduling.usecase.ActivateReminderUseCase
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivateAllActiveRemindersUseCase(
  private val reminderRepository: ReminderRepository,
  private val activateReminderUseCase: ActivateReminderUseCase
) {

  private val coroutineScope = CoroutineScope(Dispatchers.IO)

  fun run() {
    coroutineScope.launch {
      reminderRepository.getAll(active = true, removed = false).forEach {
        activateReminderUseCase(it)
      }
    }
  }
}
