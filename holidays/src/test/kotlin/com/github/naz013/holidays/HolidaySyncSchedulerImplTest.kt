package com.github.naz013.holidays

import com.github.naz013.holidays.work.HolidaySyncTask
import com.github.naz013.workapi.ExistingWorkPolicy
import com.github.naz013.workapi.NetworkRequirement
import com.github.naz013.workapi.PeriodicWorkRequest
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class HolidaySyncSchedulerImplTest {
  private val workScheduler = mockk<WorkScheduler>(relaxed = true)
  private lateinit var scheduler: HolidaySyncSchedulerImpl

  @Before
  fun setUp() {
    scheduler = HolidaySyncSchedulerImpl(workScheduler)
  }

  @Test
  fun `enable enqueues an immediate sync and a weekly periodic check under the same tag`() {
    scheduler.enable()

    verify {
      workScheduler.enqueueUnique(
        "holiday_sync_initial",
        ExistingWorkPolicy.REPLACE,
        WorkRequest(
          taskKey = HolidaySyncTask.TASK_KEY,
          tag = "holiday_sync",
          networkRequirement = NetworkRequirement.CONNECTED,
        )
      )
    }
    verify {
      workScheduler.enqueuePeriodic(
        PeriodicWorkRequest(
          taskKey = HolidaySyncTask.TASK_KEY,
          tag = "holiday_sync",
          repeatIntervalMillis = TimeUnit.DAYS.toMillis(7),
          flexIntervalMillis = TimeUnit.HOURS.toMillis(6),
          networkRequirement = NetworkRequirement.CONNECTED,
        )
      )
    }
  }

  @Test
  fun `disable cancels everything under the shared tag`() {
    scheduler.disable()

    verify { workScheduler.cancelByTag("holiday_sync") }
  }

  @Test
  fun `syncNow re-enqueues the one-shot sync only`() {
    scheduler.syncNow()

    verify {
      workScheduler.enqueueUnique(
        "holiday_sync_initial",
        ExistingWorkPolicy.REPLACE,
        WorkRequest(
          taskKey = HolidaySyncTask.TASK_KEY,
          tag = "holiday_sync",
          networkRequirement = NetworkRequirement.CONNECTED,
        )
      )
    }
    verify(exactly = 0) { workScheduler.enqueuePeriodic(any()) }
  }
}
