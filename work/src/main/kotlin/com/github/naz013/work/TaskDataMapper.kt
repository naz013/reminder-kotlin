package com.github.naz013.work

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.WorkInfo
import com.github.naz013.workapi.ExistingWorkPolicy
import com.github.naz013.workapi.NetworkRequirement
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkState

internal fun TaskData.toWorkData(taskKey: String): Data {
  val builder = Data.Builder().putString(GenericTaskWorker.KEY_TASK_KEY, taskKey)
  asMap().forEach { (key, value) ->
    when (value) {
      is String -> builder.putString(key, value)
      is Boolean -> builder.putBoolean(key, value)
      is Array<*> -> builder.putStringArray(key, value.filterIsInstance<String>().toTypedArray())
    }
  }
  return builder.build()
}

internal fun taskKeyOnlyWorkData(taskKey: String): Data =
  Data.Builder().putString(GenericTaskWorker.KEY_TASK_KEY, taskKey).build()

internal fun Data.toTaskData(): TaskData = TaskData.of(keyValueMap)

internal fun NetworkRequirement.toNetworkType(): NetworkType =
  when (this) {
    NetworkRequirement.NONE -> NetworkType.NOT_REQUIRED
    NetworkRequirement.CONNECTED -> NetworkType.CONNECTED
    NetworkRequirement.UNMETERED -> NetworkType.UNMETERED
    NetworkRequirement.METERED -> NetworkType.METERED
  }

internal fun WorkRequest.toConstraints(): Constraints =
  Constraints
    .Builder()
    .setRequiredNetworkType(networkRequirement.toNetworkType())
    .setRequiresBatteryNotLow(requiresBatteryNotLow)
    .build()

internal fun ExistingWorkPolicy.toAndroidPolicy(): androidx.work.ExistingWorkPolicy =
  when (this) {
    ExistingWorkPolicy.KEEP -> androidx.work.ExistingWorkPolicy.KEEP
    ExistingWorkPolicy.REPLACE -> androidx.work.ExistingWorkPolicy.REPLACE
  }

internal fun WorkInfo.toWorkState(): WorkState =
  when (state) {
    WorkInfo.State.ENQUEUED -> WorkState.Enqueued
    WorkInfo.State.RUNNING -> WorkState.Running(progress.toTaskData())
    WorkInfo.State.SUCCEEDED -> WorkState.Succeeded
    WorkInfo.State.FAILED -> WorkState.Failed
    WorkInfo.State.CANCELLED -> WorkState.Cancelled
    WorkInfo.State.BLOCKED -> WorkState.Blocked
  }
