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
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.viewmodel.PendingDeleteCoordinator
import com.github.naz013.feature.common.viewmodel.PendingEntity
import com.github.naz013.ui.common.R
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
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

  private val pendingIds = MutableStateFlow<Set<String>>(emptySet())
  private val pendingDeleteCoordinator = mockk<PendingDeleteCoordinator>(relaxed = true)
  private val textProvider = mockk<TextProvider>()

  private lateinit var viewModel: PreviewBirthdayViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { pendingDeleteCoordinator.pendingIds(PendingEntity.BIRTHDAY) } returns pendingIds
    every { textProvider.getText(R.string.birthday_deleted) } returns "Birthday deleted"
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
      pendingDeleteCoordinator = pendingDeleteCoordinator,
      textProvider = textProvider,
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
  fun `onDeleteClick marks the birthday pending and posts Deleted without deleting`() =
    runTest {
      viewModel.onDeleteClick()

      verify(exactly = 1) {
        pendingDeleteCoordinator.markPending(PendingEntity.BIRTHDAY, "42", "Birthday deleted", any())
      }
      coVerify(exactly = 0) { deleteBirthdayUseCase(any()) }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(PreviewBirthdayViewModel.ViewModelEvent.Deleted("42"), event?.getContentIfNotHandled())
    }

  @Test
  fun `committing the pending delete deletes the birthday exactly once`() =
    runTest {
      val onCommit = slot<suspend () -> Unit>()
      every { pendingDeleteCoordinator.markPending(any(), any(), any(), capture(onCommit)) } just runs
      viewModel.onDeleteClick()

      onCommit.captured.invoke()

      coVerify(exactly = 1) { deleteBirthdayUseCase("42") }
    }

  @Test
  fun `onDeleteClick does nothing when the birthday is already pending`() =
    runTest {
      pendingIds.value = setOf("42")

      viewModel.onDeleteClick()

      verify(exactly = 0) { pendingDeleteCoordinator.markPending(any(), any(), any(), any()) }
    }

  @Test
  fun `treats a pending birthday as not found`() =
    runTest {
      pendingIds.value = setOf("42")

      assertEquals(null, viewModel.state.first().birthday)
    }
}
