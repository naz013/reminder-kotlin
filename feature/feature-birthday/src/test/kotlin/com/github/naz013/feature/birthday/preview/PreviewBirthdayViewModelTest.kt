package com.github.naz013.feature.birthday.preview

import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.getOrAwaitValue
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.logic.birthday.DeleteBirthdayUseCase
import com.github.naz013.ui.birthday.UiBirthdayPreview
import com.github.naz013.ui.birthday.UiBirthdayPreviewAdapter
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.ui.tag.TagChipStateAdapter
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
    // PreviewBirthdayViewModel.state re-subscribes to observeById() on every fresh collection
    // (stateInWhileSubscribed) - every test collects state at least once (even ones only
    // exercising delete-dialog toggles), so a default stub avoids an unstubbed-call failure.
    val defaultBirthday = Birthday(uuId = "42", name = "Alice", syncState = SyncState.Synced)
    every { birthdayRepository.observeById("42") } returns flowOf(defaultBirthday)
    every { uiBirthdayPreviewAdapter.convert(any()) } returns uiBirthday()
    every { tagAssignmentRepository.observeTagsForItem(any(), any()) } returns flowOf(emptyList())
    viewModel = createViewModel()
  }

  private fun createViewModel() =
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
  fun `plays confetti once per view model even if the birthday re-emits`() =
    runTest {
      val birthday = Birthday(uuId = "42", name = "Alice", syncState = SyncState.Synced)
      val updatedBirthday = birthday.copy(name = "Alice Updated")
      val birthdayFlow = MutableStateFlow(birthday)
      every { birthdayRepository.observeById("42") } returns birthdayFlow
      every { uiBirthdayPreviewAdapter.convert(birthday) } returns uiBirthday(hasBirthdayToday = true)
      every { uiBirthdayPreviewAdapter.convert(updatedBirthday) } returns
        uiBirthday(hasBirthdayToday = true).copy(name = "Alice Updated")
      val freshViewModel = createViewModel()

      var latest = PreviewBirthdayState()
      backgroundScope.launch(Dispatchers.Unconfined) {
        freshViewModel.state.collect { latest = it }
      }
      assertEquals(true, latest.playConfetti)

      // Simulates the birthday being re-saved elsewhere (e.g. the Edit screen) while this pane
      // stays open - no explicit refresh call from the view model is needed for this to show up,
      // but `canShowAnimation` already flipped false on the first emission, so the confetti
      // shouldn't replay.
      birthdayFlow.value = updatedBirthday

      assertEquals("Alice Updated", latest.birthday?.name)
      assertEquals(false, latest.playConfetti)
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
