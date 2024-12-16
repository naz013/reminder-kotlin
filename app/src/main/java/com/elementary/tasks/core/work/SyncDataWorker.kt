package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.work.operation.SyncOperationType
import com.elementary.tasks.core.work.operation.SyncOperationsFactory
import com.github.naz013.logging.Logger
import kotlinx.coroutines.withContext

class SyncDataWorker(
  private val syncManagers: SyncManagers,
  private val prefs: Prefs,
  context: Context,
  workerParams: WorkerParameters,
  private val updatesHelper: UpdatesHelper,
  private val dispatcherProvider: DispatcherProvider,
  private val syncOperationsFactory: SyncOperationsFactory
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    if (prefs.autoSyncState == 0) {
      return Result.success()
    }
    val storage = CompositeStorage(syncManagers.storageManager)

    withContext(dispatcherProvider.default()) {
      val result = OperationProcessor(
        syncOperationsFactory(storage, SyncOperationType.FULL)
      ).process()

      Logger.i("Sync finished with result = $result")

      withUIContext {
        updatesHelper.updateWidgets()
        updatesHelper.updateNotesWidget()
      }
    }
    return Result.success()
  }

  companion object {
    private const val TAG = "SyncDataWorker"
    const val FLAG_REMINDER = "flag.reminder"
    const val FLAG_NOTE = "flag.note"
    const val FLAG_BIRTHDAY = "flag.birthday"
    const val FLAG_PLACE = "flag.place"
    const val FLAG_SETTINGS = "flag.settings"

    fun schedule(context: Context) {
      val work = OneTimeWorkRequest.Builder(SyncDataWorker::class.java)
        .addTag(TAG)
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()
        )
        .build()
      WorkManager.getInstance(context).enqueue(work)
    }
  }
}
