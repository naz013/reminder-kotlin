package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.GoogleTask
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.scheduler.JobSchedulerApi

/**
 * Completes related Google Task when a reminder is completed.
 */
class CompleteRelatedGoogleTaskUseCase(
  private val jobScheduler: JobSchedulerApi,
  private val googleTaskRepository: GoogleTaskRepository,
) {
  suspend operator fun invoke(reminderId: String) {
    val googleTask = googleTaskRepository.getByReminderId(reminderId)
    if (googleTask != null && googleTask.status == GoogleTask.TASKS_NEED_ACTION) {
      jobScheduler.scheduleTaskDone(googleTask, reminderId)
      Logger.i(TAG, "Scheduled Google Task done for reminderId=$reminderId, taskId=${googleTask.uuId}")
    }
  }

  companion object {
    private const val TAG = "CompleteRelatedGoogleTaskUseCase"
  }
}
