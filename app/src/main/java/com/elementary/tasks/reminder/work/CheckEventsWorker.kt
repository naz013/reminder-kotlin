package com.elementary.tasks.reminder.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.EventImportProcessor
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext

class CheckEventsWorker(
  private val prefs: Prefs,
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider,
  private val eventImportProcessor: EventImportProcessor
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    if (
      Permissions.checkPermission(
        applicationContext,
        Permissions.READ_CALENDAR,
        Permissions.WRITE_CALENDAR
      )
    ) {
      launchCheckEvents()
    }
    return Result.success()
  }

  private suspend fun launchCheckEvents() {
    withContext(dispatcherProvider.default()) {
      eventImportProcessor.importEventsFor(prefs.trackCalendarIds.toList())
    }
  }

  companion object {
    private const val TAG = "CheckEventsWorker"

    fun schedule(context: Context) {
      val work = OneTimeWorkRequest.Builder(CheckEventsWorker::class.java)
        .addTag(TAG)
        .build()
      WorkManager.getInstance(context).enqueue(work)
    }
  }
}
