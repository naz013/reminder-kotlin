package com.elementary.tasks.birthdays.preview

import com.elementary.tasks.BaseTest
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayPreviewAdapter
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayPreview
import com.elementary.tasks.getOrAwaitValue
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PreviewBirthdayViewModelTest : BaseTest() {
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val uiBirthdayPreviewAdapter = mockk<UiBirthdayPreviewAdapter>()
  private val deleteBirthdayUseCase = mockk<DeleteBirthdayUseCase>(relaxed = true)

  private lateinit var viewModel: PreviewBirthdayViewModel

  @Before
  override fun setUp() {
    super.setUp()
    viewModel =
      PreviewBirthdayViewModel(
        id = "42",
        birthdayRepository = birthdayRepository,
        dispatcherProvider = mockDispatcherProvider(),
        analyticsEventSender = analyticsEventSender,
        uiBirthdayPreviewAdapter = uiBirthdayPreviewAdapter,
        deleteBirthdayUseCase = deleteBirthdayUseCase,
      )
  }

  private fun uiBirthday(hasBirthdayToday: Boolean = false) =
    UiBirthdayPreview(
      uuId = "42",
      name = "Alice",
      number = null,
      photo = null,
      contactName = null,
      ageFormatted = "25",
      dateOfBirth = "1 Jan",
      nextBirthdayDate = "1 Jan 2027",
      hasBirthdayToday = hasBirthdayToday,
    )

  @Test
  fun `onResume loads the birthday into state`() =
    runTest {
      val birthday = Birthday(uuId = "42", name = "Alice", syncState = SyncState.Synced)
      coEvery { birthdayRepository.getById("42") } returns birthday
      every { uiBirthdayPreviewAdapter.convert(birthday) } returns uiBirthday()

      viewModel.onResume(mockk(relaxed = true))
      val state = viewModel.state.value

      assertEquals("Alice", state.birthday?.name)
    }

  @Test
  fun `plays confetti once when it is the contact's birthday today`() =
    runTest {
      val birthday = Birthday(uuId = "42", name = "Alice", syncState = SyncState.Synced)
      coEvery { birthdayRepository.getById("42") } returns birthday
      every { uiBirthdayPreviewAdapter.convert(birthday) } returns uiBirthday(hasBirthdayToday = true)

      viewModel.onResume(mockk(relaxed = true))
      assertEquals(true, viewModel.state.value.playConfetti)

      viewModel.onResume(mockk(relaxed = true))
      assertEquals(false, viewModel.state.value.playConfetti)
    }

  @Test
  fun `onDeleteClick shows the delete confirmation dialog`() {
    viewModel.onDeleteClick()

    assertEquals(true, viewModel.state.value.showDeleteConfirm)
  }

  @Test
  fun `onDeleteDismiss hides the delete confirmation dialog`() {
    viewModel.onDeleteClick()

    viewModel.onDeleteDismiss()

    assertEquals(false, viewModel.state.value.showDeleteConfirm)
  }

  @Test
  fun `onDeleteConfirmed deletes the birthday and posts DELETED`() =
    runTest {
      viewModel.onDeleteClick()

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 1) { deleteBirthdayUseCase("42") }
      assertEquals(false, viewModel.state.value.showDeleteConfirm)
      val event = viewModel.resultEvent.getOrAwaitValue()
      assertEquals(Commands.DELETED, event?.getContentIfNotHandled())
    }
}
