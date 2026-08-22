package com.github.naz013.logic.notificationaction.reminder

import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.scheduler.JobSchedulerApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ReminderRepeatProcessor(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderV2Repository: ReminderV2Repository,
  private val jobScheduler: JobSchedulerApi,
  private val reminderActionProcessor: ReminderActionProcessor,
) {
  private val scope = CoroutineScope(dispatcherProvider.default())

  fun process(id: String) {
    Logger.d(TAG, "process: $id")
    scope.launch {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      reminderActionProcessor.process(reminder.uuId)
      jobScheduler.scheduleReminderRepeat(reminder)
    }
  }

  companion object {
    private const val TAG = "ReminderRepeatProcessor"
  }
}
