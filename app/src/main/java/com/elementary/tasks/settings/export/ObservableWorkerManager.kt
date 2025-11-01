package com.elementary.tasks.settings.export

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.github.naz013.logging.Logger

/**
 * Generic manager class for observing WorkManager-based workers progress.
 * Provides listener-based callbacks for work progress and completion.
 * Can be used with any observable worker that follows the standard pattern.
 *
 * @param context Application context
 */
class ObservableWorkerManager(
  private val context: Context
) {

  var onEnd: (() -> Unit)? = null
  var listener: ((Boolean) -> Unit)? = null

  /**
   * Starts observing a worker's progress.
   * Triggers listener callbacks when progress changes.
   *
   * @param lifecycleOwner Lifecycle owner for observing WorkManager state
   * @param workTag Unique tag identifier for the worker
   * @param progressKey Key used to retrieve progress data from WorkInfo
   */
  fun observeWork(
    lifecycleOwner: LifecycleOwner,
    workTag: String,
    progressKey: String = KEY_IS_IN_PROGRESS
  ) {
    val workManager = WorkManager.getInstance(context)

    workManager.getWorkInfosForUniqueWorkLiveData(workTag)
      .observe(lifecycleOwner) { workInfoList ->
        handleWorkInfoUpdate(workInfoList, progressKey, workTag)
      }
  }

  /**
   * Handles updates from WorkManager work info.
   *
   * @param workInfoList List of work info objects
   * @param progressKey Key used to retrieve progress data
   * @param workTag Tag identifier for logging purposes
   */
  private fun handleWorkInfoUpdate(
    workInfoList: List<WorkInfo>?,
    progressKey: String,
    workTag: String
  ) {
    if (workInfoList.isNullOrEmpty()) {
      return
    }

    val workInfo = workInfoList.firstOrNull() ?: return

    when (workInfo.state) {
      WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> {
        val isInProgress = workInfo.progress.getBoolean(progressKey, true)
        Logger.d(TAG, "[$workTag] Work in progress: $isInProgress")
        listener?.invoke(isInProgress)
      }
      WorkInfo.State.SUCCEEDED -> {
        Logger.i(TAG, "[$workTag] Work succeeded")
        listener?.invoke(false)
        onEnd?.invoke()
      }
      WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
        Logger.w(TAG, "[$workTag] Work ended with state: ${workInfo.state}")
        listener?.invoke(false)
        onEnd?.invoke()
      }
      WorkInfo.State.BLOCKED -> {
        Logger.d(TAG, "[$workTag] Work blocked")
      }
    }
  }

  /**
   * Unsubscribes all listeners.
   * Should be called when the observer is no longer needed.
   */
  fun unsubscribe() {
    onEnd = null
    listener = null
  }

  companion object {
    private const val TAG = "ObservableWorkerManager"
    const val KEY_IS_IN_PROGRESS = "is_in_progress"
  }
}

