package com.elementary.tasks.core.services.event

import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class BirthdayPermanentEventTask(
  private val prefs: Prefs,
  private val notifier: Notifier,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult {
    if (prefs.isBirthdayPermanentEnabled) {
      notifier.showBirthdayPermanent()
    }
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "event_birthday_permanent"
  }
}
