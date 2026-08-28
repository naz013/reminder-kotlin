package com.github.naz013.feature.workflow

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class WeeklySummaryTaskTest {

  private val context = mockk<Context>(relaxed = true)
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val notificationGateway = mockk<NotificationGateway>(relaxed = true)
  private val builder = mockk<NotificationCompat.Builder>(relaxed = true)

  private val task = WeeklySummaryTask(context, reminderV2Repository, notificationGateway)

  private fun completedReminder(id: String, updatedAt: LocalDateTime) = ReminderV2(
    uuId = id,
    schedule = ReminderSchedule(startDateTime = updatedAt, updatedAt = updatedAt),
    isActive = false
  )

  @Before
  fun setUp() {
    every { notificationGateway.builder(NotificationGateway.CHANNEL_SYSTEM) } returns builder
    every { builder.build() } returns mockk<Notification>()
  }

  @Test
  fun `posts a summary notification counting reminders completed in the last 7 days`() = runTest {
    val now = LocalDateTime.now()
    val recentlyCompleted = completedReminder("r1", now.minusDays(2))
    val archivedRecently = completedReminder("r2", now.minusDays(5)).copy(isRemoved = true)
    val tooOld = completedReminder("r3", now.minusDays(20))
    coEvery { reminderV2Repository.getAll(active = false, removed = false) } returns listOf(recentlyCompleted, tooOld)
    coEvery { reminderV2Repository.getAll(active = false, removed = true) } returns listOf(archivedRecently)

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 1) { notificationGateway.notify(any(), any()) }
  }

  @Test
  fun `skips posting when nothing was completed in the last 7 days`() = runTest {
    coEvery { reminderV2Repository.getAll(active = false, removed = false) } returns emptyList()
    coEvery { reminderV2Repository.getAll(active = false, removed = true) } returns emptyList()

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 0) { notificationGateway.notify(any(), any()) }
  }
}
