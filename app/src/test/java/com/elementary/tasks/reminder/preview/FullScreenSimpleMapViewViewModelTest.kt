package com.elementary.tasks.reminder.preview

import com.elementary.tasks.BaseTest
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.usecase.reminders.GetReminderV2ByIdUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDateTime

class FullScreenSimpleMapViewViewModelTest : BaseTest() {
  private val getReminderV2ByIdUseCase = mockk<GetReminderV2ByIdUseCase>()

  private fun createViewModel(id: String = "42"): FullScreenMapViewModel =
    FullScreenMapViewModel(
      id = id,
      dispatcherProvider = mockDispatcherProvider(),
      getReminderV2ByIdUseCase = getReminderV2ByIdUseCase,
    )

  @Test
  fun `loads the reminder into state on creation`() =
    runTest {
      val reminder = ReminderV2(
        uuId = "42",
        summary = "Trip",
        schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
      )
      coEvery { getReminderV2ByIdUseCase("42") } returns reminder

      val viewModel = createViewModel()

      assertEquals("Trip", viewModel.reminder.value?.summary)
    }

  @Test
  fun `state stays null when the reminder is not found`() =
    runTest {
      coEvery { getReminderV2ByIdUseCase("42") } returns null

      val viewModel = createViewModel()

      assertNull(viewModel.reminder.value)
    }
}
