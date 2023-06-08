package com.elementary.tasks.core.services.action.reminder

import com.elementary.tasks.core.data.repository.ReminderRepository
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class ReminderRepeatProcessor(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderRepository: ReminderRepository,
  private val jobScheduler: JobScheduler,
  private val reminderActionProcessor: ReminderActionProcessor
) {

  private val scope = CoroutineScope(dispatcherProvider.default())

  fun process(id: String) {
    Timber.d("process: $id")
    scope.launch {
      val reminder = reminderRepository.getById(id) ?: return@launch
      reminderActionProcessor.process(reminder.uuId)
      jobScheduler.scheduleReminderRepeat(reminder)
    }
  }
}
