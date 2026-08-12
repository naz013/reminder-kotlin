package com.github.naz013.workapi

/**
 * Describes a single execution of the [BackgroundTask] registered under [taskKey].
 *
 * @param taskKey DI qualifier used to look up the [BackgroundTask] implementation to run.
 * @param tag Identifier used for [WorkScheduler.cancelByTag]; defaults to [taskKey].
 * @param input Data made available to the task via [BackgroundTask.run].
 * @param networkRequirement Network condition that must hold before the task runs.
 * @param requiresBatteryNotLow Whether the task should be deferred while battery is low.
 * @param initialDelayMillis Minimum delay before the task is allowed to run.
 */
data class WorkRequest(
  val taskKey: String,
  val tag: String = taskKey,
  val input: TaskData = TaskData.EMPTY,
  val networkRequirement: NetworkRequirement = NetworkRequirement.NONE,
  val requiresBatteryNotLow: Boolean = false,
  val initialDelayMillis: Long = 0L,
)
