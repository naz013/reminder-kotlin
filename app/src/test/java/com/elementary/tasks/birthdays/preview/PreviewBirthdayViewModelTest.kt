package com.elementary.tasks.birthdays.preview

import com.elementary.tasks.BaseTest
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayPreviewAdapter
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayPreview
import com.elementary.tasks.getOrAwaitValue
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.ui.tag.TagChipStateAdapter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PreviewBirthdayViewModelTest : BaseTest() {
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val uiBirthdayPreviewAdapter = mockk<UiBirthdayPreviewAdapter>()
  private val deleteBirthdayUseCase = mockk<DeleteBirthdayUseCase>(relaxed = true)
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val tagChipStateAdapter = mockk<TagChipStateAdapter>()

  private lateinit var viewModel: PreviewBirthdayViewModel

  @Before
  override fun setUp() {
    super.setUp()
    // PreviewBirthdayViewModel.state runs load() in onStart on every collection - every test
    // collects state at least once (even ones only exercising delete-dialog toggles), so a
    // default stub avoids an unstubbed-call failure from that automatic load().
    val defaultBirthday = Birthday(uuId = "42", name = "Alice", syncState = SyncState.Synced)
    coEvery { birthdayRepository.getById("42") } returns defaultBirthday
    every { uiBirthdayPreviewAdapter.convert(any()) } returns uiBirthday()
    every { tagAssignmentRepository.observeTagsForItem(any(), any()) } returns flowOf(emptyList())
    viewModel =
      PreviewBirthdayViewModel(
        id = "42",
        birthdayRepository = birthdayRepository,
        dispatcherProvider = mockDispatcherProvider(),
        analyticsEventSender = analyticsEventSender,
        uiBirthdayPreviewAdapter = uiBirthdayPreviewAdapter,
        deleteBirthdayUseCase = deleteBirthdayUseCase,
        tagAssignmentRepository = tagAssignmentRepository,
        tagChipStateAdapter = tagChipStateAdapter,
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
  fun `loads the birthday into state on first collection`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals("Alice", state.birthday?.name)
    }

  @Test
  fun `plays confetti once when it is the contact's birthday today`() =
    runTest {
      val birthday = Birthday(uuId = "42", name = "Alice", syncState = SyncState.Synced)
      coEvery { birthdayRepository.getById("42") } returns birthday
      every { uiBirthdayPreviewAdapter.convert(birthday) } returns uiBirthday(hasBirthdayToday = true)

      // Each fresh collection of `state` re-runs load() in onStart, matching the screen re-entering
      // foreground - `canShowAnimation` flips to false after the first load, so a second collection
      // shouldn't replay the confetti.
      assertEquals(true, viewModel.state.first().playConfetti)
      assertEquals(false, viewModel.state.first().playConfetti)
    }

  @Test
  fun `onDeleteClick shows the delete confirmation dialog`() =
    runTest {
      viewModel.onDeleteClick()

      assertEquals(true, viewModel.state.first().showDeleteConfirm)
    }

  @Test
  fun `onDeleteDismiss hides the delete confirmation dialog`() =
    runTest {
      viewModel.onDeleteClick()

      viewModel.onDeleteDismiss()

      assertEquals(false, viewModel.state.first().showDeleteConfirm)
    }

  @Test
  fun `onDeleteConfirmed deletes the birthday and posts MoveBack`() =
    runTest {
      viewModel.onDeleteClick()

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 1) { deleteBirthdayUseCase("42") }
      assertEquals(false, viewModel.state.first().showDeleteConfirm)
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(PreviewBirthdayViewModel.ViewModelEvent.MoveBack, event?.getContentIfNotHandled())
    }
}
