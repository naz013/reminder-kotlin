package com.elementary.tasks.settings.calendar.work

import android.content.Context
import com.elementary.tasks.settings.calendar.usecase.ScanGoogleCalendarForNewEventsUseCase
import com.github.naz013.common.Permissions
import com.github.naz013.logging.Logger
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class ScanGoogleCalendarEventsTask(
  private val context: Context,
  private val scanGoogleCalendarForNewEventsUseCase: ScanGoogleCalendarForNewEventsUseCase,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult {
    if (Permissions.checkPermission(context, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
      Logger.i(TASK_KEY, "Starting Google Calendar events scan worker.")
      scanGoogleCalendarForNewEventsUseCase()
    } else {
      Logger.w(TASK_KEY, "Calendar permissions are not granted. Cannot scan Google Calendar events.")
    }
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "scan_google_calendar_events"
  }
}
