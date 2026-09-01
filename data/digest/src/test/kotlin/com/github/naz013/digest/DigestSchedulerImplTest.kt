package com.github.naz013.digest

import com.github.naz013.digest.work.DailyDigestTask
import com.github.naz013.workapi.PeriodicWorkRequest
import com.github.naz013.workapi.WorkScheduler
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class DigestSchedulerImplTest {
  private val workScheduler = mockk<WorkScheduler>(relaxed = true)
  private lateinit var scheduler: DigestSchedulerImpl

  @Before
  fun setUp() {
    scheduler = DigestSchedulerImpl(workScheduler)
  }

  @Test
  fun `enable enqueues a 24h periodic check with a 1h flex window`() {
    scheduler.enable()

    verify {
      workScheduler.enqueuePeriodic(
        PeriodicWorkRequest(
          taskKey = DailyDigestTask.TASK_KEY,
          tag = "digest_daily",
          repeatIntervalMillis = TimeUnit.HOURS.toMillis(24),
          flexIntervalMillis = TimeUnit.HOURS.toMillis(1),
        )
      )
    }
  }

  @Test
  fun `disable cancels everything under the shared tag`() {
    scheduler.disable()

    verify { workScheduler.cancelByTag("digest_daily") }
  }
}
