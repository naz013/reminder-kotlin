package com.github.naz013.work

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.github.naz013.logging.Logger
import com.github.naz013.workapi.ExistingWorkPolicy
import com.github.naz013.workapi.PeriodicWorkRequest
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import com.github.naz013.workapi.WorkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import java.util.concurrent.TimeUnit
import androidx.work.PeriodicWorkRequest as AndroidPeriodicWorkRequest

class WorkSchedulerImpl(
  private val context: Context,
) : WorkScheduler {
  // WorkManager.getInstance() throws IllegalStateException on devices where WorkManager
  // failed to initialize (e.g. OEM Android 14 builds missing JobScheduler.forNamespace).
  // Treated as absent rather than fatal so callers degrade instead of crashing.
  private fun workManagerOrNull(): WorkManager? =
    runCatching { WorkManager.getInstance(context) }
      .onFailure { Logger.w("WorkScheduler", "WorkManager is unavailable: ${it.message}") }
      .getOrNull()

  override fun enqueue(request: WorkRequest): String {
    workManagerOrNull()?.enqueue(request.toOneTimeWorkRequest())
    return request.tag
  }

  override fun enqueueUnique(
    uniqueName: String,
    policy: ExistingWorkPolicy,
    request: WorkRequest,
  ): String {
    workManagerOrNull()?.enqueueUniqueWork(uniqueName, policy.toAndroidPolicy(), request.toOneTimeWorkRequest())
    return uniqueName
  }

  override fun enqueuePeriodic(request: PeriodicWorkRequest): String {
    workManagerOrNull()?.enqueue(request.toAndroidPeriodicWorkRequest())
    return request.tag
  }

  override fun cancelByTag(tag: String) {
    workManagerOrNull()?.cancelAllWorkByTag(tag)
  }

  override fun cancelUniqueWork(uniqueName: String) {
    workManagerOrNull()?.cancelUniqueWork(uniqueName)
  }

  override fun observeUniqueWork(uniqueName: String): Flow<WorkState> =
    workManagerOrNull()
      ?.getWorkInfosForUniqueWorkFlow(uniqueName)
      ?.mapNotNull { it.firstOrNull() }
      ?.map { it.toWorkState() }
      ?: flowOf(WorkState.Failed)

  private fun WorkRequest.toOneTimeWorkRequest(): OneTimeWorkRequest =
    OneTimeWorkRequest
      .Builder(GenericTaskWorker::class.java)
      .setInputData(input.toWorkData(taskKey))
      .addTag(tag)
      .setConstraints(toConstraints())
      .apply { if (initialDelayMillis > 0) setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS) }
      .build()

  private fun PeriodicWorkRequest.toAndroidPeriodicWorkRequest(): AndroidPeriodicWorkRequest {
    val flexMillis = flexIntervalMillis
    val builder =
      if (flexMillis != null) {
        AndroidPeriodicWorkRequest.Builder(
          GenericTaskWorker::class.java,
          repeatIntervalMillis,
          TimeUnit.MILLISECONDS,
          flexMillis,
          TimeUnit.MILLISECONDS,
        )
      } else {
        AndroidPeriodicWorkRequest.Builder(GenericTaskWorker::class.java, repeatIntervalMillis, TimeUnit.MILLISECONDS)
      }
    return builder
      .setInputData(taskKeyOnlyWorkData(taskKey))
      .addTag(tag)
      .setConstraints(
        androidx.work.Constraints
          .Builder()
          .setRequiredNetworkType(networkRequirement.toNetworkType())
          .build(),
      ).build()
  }
}
