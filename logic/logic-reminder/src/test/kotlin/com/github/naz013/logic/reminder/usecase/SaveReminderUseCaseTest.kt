package com.github.naz013.logic.reminder.usecase

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.ReminderWorkflowTrigger
import com.github.naz013.logic.reminder.ScheduleReminderUploadUseCase
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class SaveReminderUseCaseTest {
  private val reminderV2Repository = mockk<ReminderV2Repository>(relaxed = true)
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val scheduleReminderUploadUseCase = mockk<ScheduleReminderUploadUseCase>(relaxed = true)
  private val reminderWorkflowTrigger = mockk<ReminderWorkflowTrigger>(relaxed = true)

  private lateinit var useCase: SaveReminderUseCase

  @Before
  fun setUp() {
    useCase = SaveReminderUseCase(
      reminderV2Repository,
      appWidgetUpdater,
      scheduleReminderUploadUseCase,
      reminderWorkflowTrigger,
    )
  }

  private fun reminder(id: String) = ReminderV2(
    uuId = id,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 6, 1, 9, 0))
  )

  @Test
  fun `fires onReminderCreated when the reminder does not already exist`() = runTest {
    val reminder = reminder("new-reminder")
    coEvery { reminderV2Repository.getById("new-reminder") } returns null

    useCase(reminder)

    coVerify(exactly = 1) { reminderV2Repository.save(reminder) }
    coVerify(exactly = 1) { reminderWorkflowTrigger.onReminderCreated("new-reminder") }
  }

  @Test
  fun `does not fire onReminderCreated when editing an existing reminder`() = runTest {
    val reminder = reminder("existing-reminder")
    coEvery { reminderV2Repository.getById("existing-reminder") } returns reminder

    useCase(reminder)

    coVerify(exactly = 1) { reminderV2Repository.save(reminder) }
    coVerify(exactly = 0) { reminderWorkflowTrigger.onReminderCreated(any()) }
  }

  @Test
  fun `always schedules a cloud upload regardless of new or existing`() = runTest {
    val reminder = reminder("r1")
    coEvery { reminderV2Repository.getById("r1") } returns null

    useCase(reminder)

    coVerify(exactly = 1) { scheduleReminderUploadUseCase("r1") }
  }
}
