package com.elementary.tasks.settings.export

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.sync.SyncApi
import kotlinx.coroutines.withContext

/**
 * WorkManager worker for performing observable sync operations.
 * Reports progress during sync to allow UI updates.
 *
 * @param context Application context
 * @param workerParams Worker parameters from WorkManager
 * @param dispatcherProvider Coroutine dispatcher provider for background work
 * @param syncApi API for performing sync operations
 */
class ObservableSyncWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider,
  private val syncApi: SyncApi
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    return try {
      Logger.i(TAG, "Starting observable sync")
      setProgress(createProgressData(true))

      withContext(dispatcherProvider.io()) {
        syncApi.sync(forceUpload = false)
      }

      Logger.i(TAG, "Observable sync completed successfully")
      setProgress(createProgressData(false))
      Result.success()
    } catch (e: Exception) {
      Logger.e(TAG, "Observable sync failed", e)
      setProgress(createProgressData(false))
      Result.failure()
    }
  }

  /**
   * Creates progress data for WorkManager progress updates.
   *
   * @param isInProgress Whether the backup is currently in progress
   * @return Data object containing progress state
   */
  private fun createProgressData(isInProgress: Boolean): Data {
    return Data.Builder()
      .putBoolean(KEY_IS_IN_PROGRESS, isInProgress)
      .build()
  }

  companion object {
    private const val TAG = "ObservableSyncWorker"
    const val KEY_IS_IN_PROGRESS = "is_in_progress"
    /**
     * Schedules the observable sync work with WorkManager.
     *
     * @param context Application context
     */
    fun schedule(context: Context) {
      val work = OneTimeWorkRequest.Builder(ObservableSyncWorker::class.java)
        .addTag(TAG)
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .build()
      WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, work)
    }

    /**
     * Gets the unique work name/tag for this worker.
     *
     * @return The worker tag
     */
    fun getWorkTag(): String = TAG
  }
}
