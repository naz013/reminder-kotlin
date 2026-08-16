package com.github.naz013.feature.reminder.preview

import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDateTime

class FullScreenSimpleMapViewViewModelTest : BaseTest() {
  private val reminderV2Repository = mockk<ReminderV2Repository>()

  private fun createViewModel(id: String = "42"): FullScreenMapViewModel =
    FullScreenMapViewModel(
      id = id,
      dispatcherProvider = mockDispatcherProvider(),
      reminderV2Repository = reminderV2Repository,
    )

  @Test
  fun `loads the reminder into state on creation`() =
    runTest {
      val reminder = ReminderV2(
        uuId = "42",
        summary = "Trip",
        schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
      )
      coEvery { reminderV2Repository.getById("42") } returns reminder

      val viewModel = createViewModel()

      assertEquals("Trip", viewModel.reminder.value?.summary)
    }

  @Test
  fun `state stays null when the reminder is not found`() =
    runTest {
      coEvery { reminderV2Repository.getById("42") } returns null

      val viewModel = createViewModel()

      assertNull(viewModel.reminder.value)
    }
}
