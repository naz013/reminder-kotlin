package com.github.naz013.logic.notificationaction.reminder

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderRepeatProcessorTest {
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val reminderActionProcessor = mockk<ReminderActionProcessor>(relaxed = true)

  private lateinit var processor: ReminderRepeatProcessor

  private val reminder = ReminderV2(uuId = "1", schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

  @Before
  fun setUp() {
    coEvery { reminderV2Repository.getById("1") } returns reminder

    processor = ReminderRepeatProcessor(
      dispatcherProvider = mockDispatcherProvider(),
      reminderV2Repository = reminderV2Repository,
      reminderActionProcessor = reminderActionProcessor,
    )
  }

  @Test
  fun `process passes the repeat count through to the action processor`() =
    runTest {
      processor.process("1", repeatCount = 4)

      coVerify(exactly = 1) { reminderActionProcessor.process("1", 4) }
    }

  @Test
  fun `process does nothing when the reminder no longer exists`() =
    runTest {
      coEvery { reminderV2Repository.getById("missing") } returns null

      processor.process("missing", repeatCount = 1)

      coVerify(exactly = 0) { reminderActionProcessor.process(any(), any()) }
    }
}
