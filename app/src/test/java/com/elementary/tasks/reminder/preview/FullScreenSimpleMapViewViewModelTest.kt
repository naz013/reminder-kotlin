package com.elementary.tasks.reminder.preview

import com.elementary.tasks.BaseTest
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.usecase.reminders.GetReminderByIdUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FullScreenSimpleMapViewViewModelTest : BaseTest() {
  private val getReminderByIdUseCase = mockk<GetReminderByIdUseCase>()

  private fun createViewModel(id: String = "42"): FullScreenMapViewModel =
    FullScreenMapViewModel(
      id = id,
      dispatcherProvider = mockDispatcherProvider(),
      getReminderByIdUseCase = getReminderByIdUseCase,
    )

  @Test
  fun `onCreate loads the reminder into state`() =
    runTest {
      val reminder = Reminder(uuId = "42", summary = "Trip", syncState = SyncState.Synced)
      coEvery { getReminderByIdUseCase("42") } returns reminder
      val viewModel = createViewModel()

      viewModel.onCreate(mockk(relaxed = true))

      assertEquals("Trip", viewModel.reminder.value?.summary)
    }

  @Test
  fun `state stays null when the reminder is not found`() =
    runTest {
      coEvery { getReminderByIdUseCase("42") } returns null
      val viewModel = createViewModel()

      viewModel.onCreate(mockk(relaxed = true))

      assertNull(viewModel.reminder.value)
    }
}
