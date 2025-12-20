package com.elementary.tasks.settings.export.work

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
 * WorkManager worker for performing observable backup operations.
 * Reports progress during backup to allow UI updates.
 *
 * @param context Application context
 * @param workerParams Worker parameters from WorkManager
 * @param dispatcherProvider Coroutine dispatcher provider for background work
 * @param syncApi API for performing sync operations
 */
class ObservableBackupWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider,
  private val syncApi: SyncApi
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    return try {
      Logger.i(TAG, "Starting observable backup")
      setProgress(createProgressData(true))

      withContext(dispatcherProvider.io()) {
        syncApi.upload()
      }

      Logger.i(TAG, "Observable backup completed successfully")
      setProgress(createProgressData(false))
      Result.success()
    } catch (e: Exception) {
      Logger.e(TAG, "Observable backup failed", e)
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
    private const val TAG = "ObservableBackupWorker"
    const val KEY_IS_IN_PROGRESS = "is_in_progress"

    /**
     * Schedules a new backup work request.
     * Uses REPLACE policy to ensure only one backup runs at a time.
     *
     * @param context Application context
     */
    fun schedule(context: Context) {
      val work = OneTimeWorkRequest.Builder(ObservableBackupWorker::class.java)
        .addTag(TAG)
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .build()
      WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, work)
    }

    /**
     * Gets the unique work name/tag for this worker.
     *
     * @return The worker tag
     */
    fun getWorkTag(): String = TAG
  }
}
