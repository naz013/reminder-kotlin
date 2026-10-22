package com.github.naz013.logic.reminder.usecase

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.ReminderWorkflowTrigger
import com.github.naz013.logic.reminder.ScheduleReminderUploadUseCase
import com.github.naz013.logic.reminder.chain.ReminderChainValidator
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
      lazy { reminderWorkflowTrigger },
      ResolveChainedStartUseCase(reminderV2Repository, ReminderChainValidator()),
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

  @Test
  fun `saves a reminder inactive while it waits for an active predecessor`() = runTest {
    val waiting = reminder("second").copy(startAfterReminderId = "first")
    coEvery { reminderV2Repository.getById("second") } returns null
    coEvery { reminderV2Repository.getById("first") } returns reminder("first").copy(isActive = true)

    useCase(waiting)

    coVerify(exactly = 1) {
      reminderV2Repository.save(match { it.uuId == "second" && !it.isActive && it.startAfterReminderId == "first" })
    }
  }

  @Test
  fun `drops the link and keeps the reminder active when the predecessor no longer exists`() = runTest {
    val waiting = reminder("second").copy(startAfterReminderId = "gone")
    coEvery { reminderV2Repository.getById("gone") } returns null

    useCase(waiting)

    coVerify(exactly = 1) {
      reminderV2Repository.save(match { it.uuId == "second" && it.isActive && it.startAfterReminderId == null })
    }
  }

  @Test
  fun `drops the link when the predecessor is already finished`() = runTest {
    val waiting = reminder("second").copy(startAfterReminderId = "done")
    coEvery { reminderV2Repository.getById("done") } returns reminder("done").copy(isActive = false)

    useCase(waiting)

    coVerify(exactly = 1) { reminderV2Repository.save(match { it.startAfterReminderId == null && it.isActive }) }
  }

  @Test
  fun `drops the link when the predecessor is archived`() = runTest {
    val waiting = reminder("second").copy(startAfterReminderId = "old")
    coEvery { reminderV2Repository.getById("old") } returns reminder("old").copy(isActive = true, isRemoved = true)

    useCase(waiting)

    coVerify(exactly = 1) { reminderV2Repository.save(match { it.startAfterReminderId == null }) }
  }

  @Test
  fun `rejects a link that points to the reminder itself`() = runTest {
    val selfLinked = reminder("same").copy(startAfterReminderId = "same")

    useCase(selfLinked)

    coVerify(exactly = 1) { reminderV2Repository.save(match { it.startAfterReminderId == null }) }
  }

  @Test
  fun `rejects a link that closes a cycle through a waiting predecessor`() = runTest {
    val second = reminder("second").copy(startAfterReminderId = "first")
    val first = reminder("first").copy(isActive = false, startAfterReminderId = "second")
    coEvery { reminderV2Repository.getById("first") } returns first
    coEvery { reminderV2Repository.getById("second") } returns second

    useCase(second)

    coVerify(exactly = 1) {
      reminderV2Repository.save(match { it.uuId == "second" && it.startAfterReminderId == null })
    }
  }
}
