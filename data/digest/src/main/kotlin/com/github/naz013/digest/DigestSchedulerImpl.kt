package com.github.naz013.digest

import com.github.naz013.digest.work.DailyDigestTask
import com.github.naz013.digestapi.DigestScheduler
import com.github.naz013.workapi.PeriodicWorkRequest
import com.github.naz013.workapi.WorkScheduler
import java.util.concurrent.TimeUnit

internal class DigestSchedulerImpl(
  private val workScheduler: WorkScheduler,
) : DigestScheduler {

  override fun enable() {
    workScheduler.enqueuePeriodic(
      PeriodicWorkRequest(
        taskKey = DailyDigestTask.TASK_KEY,
        tag = TAG,
        repeatIntervalMillis = TimeUnit.HOURS.toMillis(24),
        flexIntervalMillis = TimeUnit.HOURS.toMillis(1),
      )
    )
  }

  override fun disable() {
    workScheduler.cancelByTag(TAG)
  }

  companion object {
    private const val TAG = "digest_daily"
  }
}
