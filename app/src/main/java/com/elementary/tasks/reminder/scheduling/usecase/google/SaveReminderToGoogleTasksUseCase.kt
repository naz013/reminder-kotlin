package com.elementary.tasks.reminder.scheduling.usecase.google

import com.elementary.tasks.R
import com.elementary.tasks.core.services.JobScheduler
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger

/**
 * Saves reminder as Google Task if export is enabled.
 */
class SaveReminderToGoogleTasksUseCase(
  private val jobScheduler: JobScheduler,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
) {

  suspend operator fun invoke(reminder: Reminder) {
    if (reminder.exportToTasks) {
      val due = dateTimeManager.toMillis(reminder.eventTime)
      val googleTask = GoogleTask()
      googleTask.listId = reminder.taskListId ?: ""
      googleTask.status = GoogleTask.TASKS_NEED_ACTION
      googleTask.title = reminder.summary
      googleTask.dueDate = due
      googleTask.notes = reminder.description ?: textProvider.getText(R.string.from_reminder)
      googleTask.uuId = reminder.uuId
      jobScheduler.scheduleSaveNewTask(googleTask, reminder.uuId)
      Logger.i(TAG, "Scheduled saving reminder id=${reminder.uuId} to Google Tasks")
    }
  }

  companion object {
    private const val TAG = "SaveReminderToGoogleTasksUseCase"
  }
}
