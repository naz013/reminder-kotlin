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
import com.github.naz013.cloudapi.dropbox.DropboxApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import kotlinx.coroutines.withContext

class ObservableEraseDataWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider,
  private val googleDriveApi: GoogleDriveApi,
  private val dropboxApi: DropboxApi
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    return try {
      Logger.i(TAG, "Starting observable erase data work")
      setProgress(createProgressData(true))

      withContext(dispatcherProvider.io()) {
        googleDriveApi.removeAllData()
        dropboxApi.removeAllData()
      }

      Logger.i(TAG, "Observable erase data work completed successfully")
      setProgress(createProgressData(false))
      Result.success()
    } catch (e: Exception) {
      Logger.e(TAG, "Observable erase data work failed", e)
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
    private const val TAG = "ObservableEraseDataWorker"
    const val KEY_IS_IN_PROGRESS = "is_in_progress"

    /**
     * Schedules the observable erase data work with WorkManager.
     *
     * @param context Application context
     */
    fun schedule(context: Context) {
      val work = OneTimeWorkRequest.Builder(ObservableEraseDataWorker::class.java)
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
