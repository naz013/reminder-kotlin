package com.elementary.tasks.core.services.action.reminder

import com.elementary.tasks.core.services.JobScheduler
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ReminderRepeatProcessor(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderRepository: ReminderRepository,
  private val jobScheduler: JobScheduler,
  private val reminderActionProcessor: ReminderActionProcessor
) {

  private val scope = CoroutineScope(dispatcherProvider.default())

  fun process(id: String) {
    Logger.d("process: $id")
    scope.launch {
      val reminder = reminderRepository.getById(id) ?: return@launch
      reminderActionProcessor.process(reminder.uuId)
      jobScheduler.scheduleReminderRepeat(reminder)
    }
  }
}
