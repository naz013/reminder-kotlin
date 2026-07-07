package com.github.naz013.workapi

/**
 * Describes a recurring execution of the [BackgroundTask] registered under [taskKey].
 *
 * @param repeatIntervalMillis How often the task should repeat.
 * @param flexIntervalMillis Optional flex window within which the task may run near the
 * end of each interval, allowing the scheduler to batch work with other tasks.
 */
data class PeriodicWorkRequest(
  val taskKey: String,
  val tag: String = taskKey,
  val repeatIntervalMillis: Long,
  val flexIntervalMillis: Long? = null,
  val networkRequirement: NetworkRequirement = NetworkRequirement.NONE,
)
