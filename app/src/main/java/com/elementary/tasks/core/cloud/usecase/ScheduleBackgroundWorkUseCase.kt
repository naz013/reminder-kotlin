package com.elementary.tasks.core.cloud.usecase

import com.elementary.tasks.core.cloud.worker.DeleteTask
import com.elementary.tasks.core.cloud.worker.ForceUploadTask
import com.elementary.tasks.core.cloud.worker.SyncTask
import com.elementary.tasks.core.cloud.worker.UploadTask
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.cloud.worker.WorkerData
import com.elementary.tasks.core.cloud.worker.toNetworkRequirement
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logging.Logger
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.files.DataType
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler

class ScheduleBackgroundWorkUseCase(
  private val workScheduler: WorkScheduler,
  private val getWorkerTagUseCase: GetWorkerTagUseCase,
  private val prefs: Prefs,
  private val cloudApiProvider: CloudApiProvider,
) {
  operator fun invoke(
    workType: WorkType,
    dataType: DataType? = null,
    id: String? = null,
    ids: List<String>? = null,
  ): String? {
    if (cloudApiProvider.getAllowedCloudApis().isEmpty()) {
      Logger.i(TAG, "No authorized cloud APIs. Work not scheduled.")
      return null
    }

    val dataBuilder = TaskData.builder()
    dataType?.also { dataBuilder.putString(WorkerData.DATA_TYPE, it.name) }
    id?.also { dataBuilder.putString(WorkerData.ITEM_ID, it) }
    ids?.also { dataBuilder.putStringArray(WorkerData.ITEM_IDS, it.toTypedArray()) }
    val tag = getWorkerTagUseCase(workType, dataType, id)
    val taskKey =
      when (workType) {
        WorkType.Upload -> UploadTask.TASK_KEY
        WorkType.Sync -> SyncTask.TASK_KEY
        WorkType.Delete -> DeleteTask.TASK_KEY
        WorkType.ForceUpload -> ForceUploadTask.TASK_KEY
        WorkType.ForceSync -> {
          dataBuilder.putBoolean(WorkerData.FORCE, true)
          SyncTask.TASK_KEY
        }
      }

    workScheduler.enqueue(
      WorkRequest(
        taskKey = taskKey,
        tag = tag,
        input = dataBuilder.build(),
        networkRequirement = prefs.workerNetworkType.toNetworkRequirement(),
      ),
    )
    Logger.i(
      TAG,
      "Scheduled work: type=$workType, dataType=$dataType, id=$id, tag=$tag, network=${prefs.workerNetworkType.name}",
    )
    return tag
  }

  companion object {
    private const val TAG = "ScheduleUploadUseCase"
  }
}
