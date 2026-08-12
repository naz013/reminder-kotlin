package com.github.naz013.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

/**
 * The single [CoroutineWorker] registered with WorkManager. It resolves the actual
 * [BackgroundTask] to run from the `task_key` carried in its input data, so feature
 * modules never need to declare their own Worker subclass or Koin `worker { }` binding.
 */
class GenericTaskWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider,
) : CoroutineWorker(context, workerParams),
  KoinComponent {
  override suspend fun doWork(): Result {
    val taskKey = inputData.getString(KEY_TASK_KEY)
    if (taskKey == null) {
      Logger.e(TAG, "No task key provided in input data.")
      return Result.failure()
    }

    val task = get<BackgroundTask>(qualifier = named(taskKey))
    val input = inputData.toTaskData()
    val progressReporter = TaskProgressReporter { data -> setProgress(data.toWorkData(taskKey)) }

    Logger.i(TAG, "Running task: $taskKey, input=$input, task=$task")

    return withContext(dispatcherProvider.io()) {
      when (task.run(input, progressReporter)) {
        TaskResult.Success -> Result.success()
        TaskResult.Retry -> Result.retry()
        TaskResult.Failure -> Result.failure()
      }
    }
  }

  companion object {
    private const val TAG = "GenericTaskWorker"
    const val KEY_TASK_KEY = "task_key"
  }
}
