package com.github.naz013.holidays

import com.github.naz013.holidays.work.HolidaySyncTask
import com.github.naz013.holidaysapi.HolidaySyncScheduler
import com.github.naz013.workapi.ExistingWorkPolicy
import com.github.naz013.workapi.NetworkRequirement
import com.github.naz013.workapi.PeriodicWorkRequest
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import java.util.concurrent.TimeUnit

internal class HolidaySyncSchedulerImpl(
  private val workScheduler: WorkScheduler,
) : HolidaySyncScheduler {

  override fun enable() {
    syncNow()
    workScheduler.enqueuePeriodic(
      PeriodicWorkRequest(
        taskKey = HolidaySyncTask.TASK_KEY,
        tag = TAG,
        repeatIntervalMillis = TimeUnit.DAYS.toMillis(7),
        flexIntervalMillis = TimeUnit.HOURS.toMillis(6),
        networkRequirement = NetworkRequirement.CONNECTED,
      )
    )
  }

  override fun disable() {
    workScheduler.cancelByTag(TAG)
  }

  override fun syncNow() {
    workScheduler.enqueueUnique(
      UNIQUE_INITIAL,
      ExistingWorkPolicy.REPLACE,
      WorkRequest(
        taskKey = HolidaySyncTask.TASK_KEY,
        tag = TAG,
        networkRequirement = NetworkRequirement.CONNECTED,
      )
    )
  }

  companion object {
    private const val TAG = "holiday_sync"
    private const val UNIQUE_INITIAL = "holiday_sync_initial"
  }
}
