package com.elementary.tasks.reminder.scheduling.usecase.google

import com.elementary.tasks.core.services.JobScheduler
import com.github.naz013.domain.GoogleTask
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskRepository

/**
 * Completes related Google Task when a reminder is completed.
 */
class CompleteRelatedGoogleTaskUseCase(
  private val jobScheduler: JobScheduler,
  private val googleTaskRepository: GoogleTaskRepository
) {

  suspend operator fun invoke(reminderId: String) {
    val googleTask = googleTaskRepository.getByReminderId(reminderId)
    if (googleTask != null && googleTask.status == GoogleTask.Companion.TASKS_NEED_ACTION) {
      jobScheduler.scheduleTaskDone(googleTask, reminderId)
      Logger.i(TAG, "Scheduled Google Task done for reminderId=$reminderId, taskId=${googleTask.uuId}")
    }
  }

  companion object {
    private const val TAG = "CompleteRelatedGoogleTaskUseCase"
  }
}
