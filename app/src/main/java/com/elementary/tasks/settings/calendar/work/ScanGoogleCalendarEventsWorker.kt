package com.elementary.tasks.settings.calendar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elementary.tasks.settings.calendar.usecase.ScanGoogleCalendarForNewEventsUseCase
import com.github.naz013.common.Permissions
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import kotlinx.coroutines.withContext

class ScanGoogleCalendarEventsWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider,
  private val scanGoogleCalendarForNewEventsUseCase: ScanGoogleCalendarForNewEventsUseCase
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    if (
      Permissions.checkPermission(
        applicationContext,
        Permissions.READ_CALENDAR,
        Permissions.WRITE_CALENDAR
      )
    ) {
      Logger.i(TAG, "Starting Google Calendar events scan worker.")
      launchCheckEvents()
    } else {
      Logger.w(TAG, "Calendar permissions are not granted. Cannot scan Google Calendar events.")
    }
    return Result.success()
  }

  private suspend fun launchCheckEvents() {
    withContext(dispatcherProvider.io()) {
      scanGoogleCalendarForNewEventsUseCase()
    }
  }

  companion object {
    private const val TAG = "ScanGoogleCalendarEventsWorker"

    fun schedule(context: Context) {
      val work = OneTimeWorkRequest.Builder(ScanGoogleCalendarEventsWorker::class.java)
        .addTag(TAG)
        .build()
      WorkManager.getInstance(context).enqueue(work)
    }
  }
}
