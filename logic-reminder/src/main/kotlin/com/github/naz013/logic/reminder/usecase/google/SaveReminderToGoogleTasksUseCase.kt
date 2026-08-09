package com.github.naz013.logic.reminder.usecase.google

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.scheduler.JobSchedulerApi

/**
 * Saves reminder as Google Task if export is enabled.
 */
class SaveReminderToGoogleTasksUseCase(
  private val jobScheduler: JobSchedulerApi,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    val taskExport = reminder.taskExport ?: return
    val eventDateTime = reminder.schedule.eventDateTime ?: return
    val due = dateTimeManager.toMillis(dateTimeManager.utcToLocal(eventDateTime))
    val googleTask = GoogleTask()
    googleTask.listId = taskExport.taskListId
    googleTask.status = GoogleTask.TASKS_NEED_ACTION
    googleTask.title = reminder.summary
    googleTask.dueDate = due
    googleTask.notes = reminder.description ?: ""
    googleTask.uuId = reminder.uuId
    jobScheduler.scheduleSaveNewTask(googleTask, reminder.uuId)
    Logger.i(TAG, "Scheduled saving reminder id=${reminder.uuId} to Google Tasks")
  }

  companion object {
    private const val TAG = "SaveReminderToGoogleTasksUseCase"
  }
}
