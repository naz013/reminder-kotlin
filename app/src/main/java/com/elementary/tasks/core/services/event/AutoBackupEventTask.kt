package com.elementary.tasks.core.services.event

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.services.JobScheduler
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class AutoBackupEventTask(
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val jobScheduler: JobScheduler,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult {
    scheduleBackgroundWorkUseCase(workType = WorkType.Upload, dataType = null, id = null)
    jobScheduler.scheduleAutoBackup()
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "event_auto_backup"
  }
}
