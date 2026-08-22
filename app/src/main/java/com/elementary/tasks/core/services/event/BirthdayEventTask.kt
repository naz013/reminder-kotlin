package com.elementary.tasks.core.services.event

import com.github.naz013.logic.notificationaction.birthday.BirthdayActionProcessor
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class BirthdayEventTask(
  private val birthdayActionProcessor: BirthdayActionProcessor,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult {
    birthdayActionProcessor.process()
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "event_birthday"
  }
}
