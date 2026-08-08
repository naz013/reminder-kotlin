package com.elementary.tasks.core.services.event

import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class AutoBackupEventTask(
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val jobScheduler: JobSchedulerApi,
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
