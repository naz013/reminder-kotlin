package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.launchIo
import timber.log.Timber
import java.io.File

class DeleteFileWorker(
  private val syncManagers: SyncManagers,
  context: Context,
  val workerParams: WorkerParameters
) : Worker(context, workerParams) {

  override fun doWork(): Result {
    val fileName = workerParams.inputData.getString(ARG_FILE_NAME) ?: ""
    if (fileName.isNotEmpty()) {
      val uuId = uuIdFromFileName(fileName) ?: return Result.success()
      Timber.d("doWork: $uuId")
      val storage = CompositeStorage(syncManagers.storageManager)
      launchIo {
        when {
          fileName.endsWith(FileConfig.FILE_NAME_REMINDER) -> {
            DataFlow(
              syncManagers.repositoryManager.reminderRepository,
              syncManagers.converterManager.reminderConverter,
              storage,
              syncManagers.completableManager.reminderDeleteCompletable
            ).delete(uuId, IndexTypes.TYPE_REMINDER)
          }
          fileName.endsWith(FileConfig.FILE_NAME_BIRTHDAY) -> {
            DataFlow(
              syncManagers.repositoryManager.birthdayRepository,
              syncManagers.converterManager.birthdayConverter,
              storage,
              completable = null
            ).delete(uuId, IndexTypes.TYPE_BIRTHDAY)
          }
          fileName.endsWith(FileConfig.FILE_NAME_GROUP) -> {
            DataFlow(
              syncManagers.repositoryManager.groupRepository,
              syncManagers.converterManager.groupConverter,
              storage,
              completable = null
            ).delete(uuId, IndexTypes.TYPE_GROUP)
          }
          fileName.endsWith(FileConfig.FILE_NAME_NOTE) -> {
            DataFlow(
              syncManagers.repositoryManager.noteRepository,
              syncManagers.converterManager.noteConverter,
              storage,
              completable = null
            ).delete(uuId, IndexTypes.TYPE_NOTE)
          }
          fileName.endsWith(FileConfig.FILE_NAME_PLACE) -> {
            DataFlow(
              syncManagers.repositoryManager.placeRepository,
              syncManagers.converterManager.placeConverter,
              storage,
              completable = null
            ).delete(uuId, IndexTypes.TYPE_PLACE)
          }
          fileName.endsWith(FileConfig.FILE_NAME_TEMPLATE) -> {
            DataFlow(
              syncManagers.repositoryManager.templateRepository,
              syncManagers.converterManager.templateConverter,
              storage,
              completable = null
            ).delete(uuId, IndexTypes.TYPE_TEMPLATE)
          }
        }
      }
    }
    return Result.success()
  }

  private fun uuIdFromFileName(fileName: String): String? {
    return try {
      File(fileName).nameWithoutExtension
    } catch (e: Exception) {
      null
    }
  }

  companion object {
    private const val TAG = "DeleteFileWorker"
    private const val ARG_FILE_NAME = "arg_file_name"

    fun schedule(context: Context, fileName: String) {
      val work = OneTimeWorkRequest.Builder(DeleteFileWorker::class.java)
        .addTag(TAG + fileName)
        .setInputData(Data.Builder()
          .putString(ARG_FILE_NAME, fileName)
          .build())
        .setConstraints(Constraints.Builder()
          .setRequiredNetworkType(NetworkType.UNMETERED)
          .setRequiresBatteryNotLow(true)
          .build())
        .build()
      WorkManager.getInstance(context).enqueue(work)
    }
  }
}