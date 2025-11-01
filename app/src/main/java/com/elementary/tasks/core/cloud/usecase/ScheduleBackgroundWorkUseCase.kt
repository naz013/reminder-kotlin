package com.elementary.tasks.core.cloud.usecase

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.elementary.tasks.core.cloud.worker.DeleteWorker
import com.elementary.tasks.core.cloud.worker.ForceUploadWorker
import com.elementary.tasks.core.cloud.worker.SyncWorker
import com.elementary.tasks.core.cloud.worker.UploadWorker
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.cloud.worker.WorkerData
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.work.WorkManagerProvider
import com.github.naz013.logging.Logger
import com.github.naz013.sync.DataType

class ScheduleBackgroundWorkUseCase(
  private val workManagerProvider: WorkManagerProvider,
  private val getWorkerTagUseCase: GetWorkerTagUseCase,
  private val prefs: Prefs
) {

  operator fun invoke(
    workType: WorkType,
    dataType: DataType? = null,
    id: String? = null,
    ids: List<String>? = null,
  ) :String {
    val dataBuilder = Data.Builder()
    dataType?.also { dataBuilder.putString(WorkerData.DATA_TYPE, it.name) }
    id?.also { dataBuilder.putString(WorkerData.ITEM_ID, it) }
    ids?.also { dataBuilder.putStringArray(WorkerData.ITEM_IDS, it.toTypedArray()) }
    val tag = getWorkerTagUseCase(workType, dataType, id)
    val builder = when (workType) {
      WorkType.Upload -> OneTimeWorkRequest.Builder(UploadWorker::class.java)
      WorkType.Sync -> OneTimeWorkRequest.Builder(SyncWorker::class.java)
      WorkType.Delete -> OneTimeWorkRequest.Builder(DeleteWorker::class.java)
      WorkType.ForceUpload -> OneTimeWorkRequest.Builder(ForceUploadWorker::class.java)
      WorkType.ForceSync -> {
        dataBuilder.putBoolean(WorkerData.FORCE, true)
        OneTimeWorkRequest.Builder(SyncWorker::class.java)
      }
    }
    val work = builder
      .setInputData(dataBuilder.build())
      .addTag(tag)
      .setConstraints(
        Constraints.Builder()
          .setRequiredNetworkType(prefs.workerNetworkType.type)
          .build()
      )
      .build()
    workManagerProvider.getWorkManager().enqueue(work)
    Logger.i(TAG, "Scheduled work: type=$workType, dataType=$dataType, id=$id, tag=$tag, network=${prefs.workerNetworkType.name}")
    return tag
  }

  companion object {
    private const val TAG = "ScheduleUploadUseCase"
  }
}
