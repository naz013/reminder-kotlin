package com.elementary.tasks.calendar.occurrence.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.elementary.tasks.calendar.occurrence.CalculateBirthdayOccurrencesUseCase
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import kotlinx.coroutines.withContext

class CalculateBirthdayOccurrencesWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider,
  private val calculateBirthdayOccurrencesUseCase: CalculateBirthdayOccurrencesUseCase
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    val itemId = inputData.getString(ARG_ID)
      ?.takeIf { it.isNotEmpty() }
      ?: run {
        Logger.w(TAG, "No birthday id provided")
        return Result.success()
      }

    withContext(dispatcherProvider.io()) {
      calculateBirthdayOccurrencesUseCase(itemId)
    }

    return Result.success()
  }

  companion object {
    private const val TAG = "CalculateBirthdayOccurrences"
    private const val ARG_ID = "arg_id"

    fun prepareWork(id: String): OneTimeWorkRequest {
      val dataBuilder = Data.Builder()
        .putString(ARG_ID, id)

      val tag = "$TAG-$id"
      val work = OneTimeWorkRequest.Builder(CalculateBirthdayOccurrencesWorker::class.java)
        .setInputData(dataBuilder.build())
        .addTag(tag)
        .build()
      Logger.i(TAG, "Prepared work: tag=$tag, id=$id")
      return work
    }
  }
}
