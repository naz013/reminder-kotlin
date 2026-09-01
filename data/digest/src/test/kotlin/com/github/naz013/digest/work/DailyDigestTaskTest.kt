package com.github.naz013.digest.work

import android.app.Notification
import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import com.github.naz013.digest.DigestContentBuilder
import com.github.naz013.digest.DigestInput
import com.github.naz013.digest.DigestReminderItem
import com.github.naz013.digest.DigestSummarizerChain
import com.github.naz013.digestapi.DigestSettingsGate
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class DailyDigestTaskTest {
  private val context = mockk<Context>(relaxed = true)
  private val digestSettingsGate = mockk<DigestSettingsGate>()
  private val digestContentBuilder = mockk<DigestContentBuilder>()
  private val digestSummarizerChain = mockk<DigestSummarizerChain>()
  private val notificationGateway = mockk<NotificationGateway>(relaxed = true)
  private val builder = mockk<NotificationCompat.Builder>(relaxed = true)
  private val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
  private val editor = mockk<SharedPreferences.Editor>(relaxed = true)

  private lateinit var task: DailyDigestTask

  private val nonEmptyInput = DigestInput(
    reminders = listOf(DigestReminderItem("Pay rent", LocalDateTime.now())),
    birthdays = emptyList(),
  )

  @Before
  fun setUp() {
    task = DailyDigestTask(
      context, digestSettingsGate, digestContentBuilder, digestSummarizerChain, notificationGateway,
    )
    every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
    every { sharedPreferences.edit() } returns editor
    every { editor.putString(any(), any()) } returns editor
    every { sharedPreferences.getString(any(), null) } returns null
    every { digestSettingsGate.isDailyEnabled() } returns true
    every { digestSettingsGate.preferredHour() } returns 0
    coEvery { digestContentBuilder.buildDaily(any()) } returns nonEmptyInput
    coEvery { digestSummarizerChain.summarize(any()) } returns "A digest sentence"
    every { notificationGateway.builder(NotificationGateway.CHANNEL_SYSTEM) } returns builder
    every { builder.build() } returns mockk<Notification>()
  }

  @Test
  fun `run short-circuits without building content when the gate is disabled`() = runTest {
    every { digestSettingsGate.isDailyEnabled() } returns false

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 0) { digestContentBuilder.buildDaily(any()) }
  }

  @Test
  fun `run skips before the preferred hour`() = runTest {
    every { digestSettingsGate.preferredHour() } returns LocalTime.now().hour + 1

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 0) { digestContentBuilder.buildDaily(any()) }
  }

  @Test
  fun `run skips when already posted today`() = runTest {
    every { sharedPreferences.getString("daily_last_posted_date", null) } returns LocalDate.now().toString()

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 0) { digestContentBuilder.buildDaily(any()) }
  }

  @Test
  fun `run skips posting when there is nothing to report`() = runTest {
    coEvery { digestContentBuilder.buildDaily(any()) } returns
      DigestInput(reminders = emptyList(), birthdays = emptyList())

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 0) { digestSummarizerChain.summarize(any()) }
    coVerify(exactly = 0) { notificationGateway.notify(any(), any()) }
  }

  @Test
  fun `run posts a notification and stamps today's date when there is something to report`() = runTest {
    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 1) { notificationGateway.notify(985_612, any()) }
    verify { editor.putString("daily_last_posted_date", LocalDate.now().toString()) }
  }
}
