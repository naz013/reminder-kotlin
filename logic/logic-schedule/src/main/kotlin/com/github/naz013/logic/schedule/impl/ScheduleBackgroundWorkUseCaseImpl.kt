package com.github.naz013.logic.schedule.impl

import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.SchedulePreferences
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.logic.schedule.WorkerData
import com.github.naz013.logic.schedule.toNetworkRequirement
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler

internal class ScheduleBackgroundWorkUseCaseImpl(
  private val workScheduler: WorkScheduler,
  private val getWorkerTagUseCase: GetWorkerTagUseCase,
  private val schedulePreferences: SchedulePreferences,
  private val cloudApiProvider: CloudApiProvider,
) : ScheduleBackgroundWorkUseCase {

  override operator fun invoke(
    workType: WorkType,
    dataType: DataType?,
    id: String?,
    ids: List<String>?,
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
    val taskKey = when (workType) {
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
        networkRequirement = schedulePreferences.workerNetworkType.toNetworkRequirement(),
      ),
    )
    Logger.i(
      TAG,
      "Scheduled work: type=$workType, dataType=$dataType, id=$id, " +
        "tag=$tag, network=${schedulePreferences.workerNetworkType.name}",
    )
    return tag
  }

  companion object {
    private const val TAG = "ScheduleUploadUseCase"
  }
}
