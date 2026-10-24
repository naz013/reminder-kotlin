package com.github.naz013.tags.compose

import androidx.compose.ui.graphics.Color
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.viewmodel.PendingDeleteCoordinator
import com.github.naz013.feature.common.viewmodel.PendingEntity
import com.github.naz013.logic.tag.DeleteTagWithAssignmentsUseCase
import com.github.naz013.logic.tag.IsTagInUseUseCase
import com.github.naz013.repository.TagRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.preferences.AppPreferences
import com.github.naz013.ui.common.theme.ThemeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TagEditViewModelTest : BaseTest() {
  private val tagRepository = mockk<TagRepository>(relaxed = true)
  private val deleteTagWithAssignmentsUseCase = mockk<DeleteTagWithAssignmentsUseCase>(relaxed = true)
  private val isTagInUseUseCase = mockk<IsTagInUseUseCase>().also { coEvery { it(any()) } returns true }
  private val appPreferences = mockk<AppPreferences>(relaxed = true)
  private val themeProvider = mockk<ThemeProvider>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val pendingIds = MutableStateFlow<Set<String>>(emptySet())
  private val pendingDeleteCoordinator = mockk<PendingDeleteCoordinator>(relaxed = true)

  @Before
  override fun setUp() {
    super.setUp()
    every { pendingDeleteCoordinator.pendingIds(PendingEntity.TAG) } returns pendingIds
    every { textProvider.getText(R.string.tag_deleted) } returns "Tag deleted"
    every { themeProvider.colorsForSliderThemed() } returns listOf(Color.Red)
  }

  private fun createViewModel(id: String? = "1") =
    TagEditViewModel(
      id = id,
      dispatcherProvider = mockDispatcherProvider(),
      tagRepository = tagRepository,
      deleteTagWithAssignmentsUseCase = deleteTagWithAssignmentsUseCase,
      appPreferences = appPreferences,
      themeProvider = themeProvider,
      textProvider = textProvider,
      pendingDeleteCoordinator = pendingDeleteCoordinator,
      isTagInUseUseCase = isTagInUseUseCase,
    )

  @Test
  fun `onDeleteClick does nothing for a tag that was never saved`() =
    runTest {
      val viewModel = createViewModel(id = null)

      viewModel.onDeleteClick()

      verify(exactly = 0) { pendingDeleteCoordinator.markPending(any(), any(), any(), any()) }
      assertNull(viewModel.navigationEvent.value)
    }

  @Test
  fun `onDeleteClick asks for confirmation when the tag is in use`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onDeleteClick()

      assertEquals(TagEditViewModel.NavigationEvent.ConfirmDelete, viewModel.navigationEvent.value?.peekContent())
      verify(exactly = 0) { pendingDeleteCoordinator.markPending(any(), any(), any(), any()) }
    }

  @Test
  fun `onDeleteClick marks the tag pending right away when it is not in use`() =
    runTest {
      coEvery { isTagInUseUseCase("1") } returns false
      val viewModel = createViewModel()

      viewModel.onDeleteClick()

      verify(exactly = 1) {
        pendingDeleteCoordinator.markPending(PendingEntity.TAG, "1", "Tag deleted", any())
      }
      assertEquals(
        TagEditViewModel.NavigationEvent.Deleted("1"),
        viewModel.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `onDeleteConfirmed marks the tag pending without deleting and emits Deleted`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onDeleteConfirmed()

      verify(exactly = 1) {
        pendingDeleteCoordinator.markPending(PendingEntity.TAG, "1", "Tag deleted", any())
      }
      coVerify(exactly = 0) { deleteTagWithAssignmentsUseCase(any()) }
      assertEquals(
        TagEditViewModel.NavigationEvent.Deleted("1"),
        viewModel.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `committing the pending tag delete detaches assignments and deletes the tag once`() =
    runTest {
      val onCommit = slot<suspend () -> Unit>()
      every { pendingDeleteCoordinator.markPending(any(), any(), any(), capture(onCommit)) } just runs
      val viewModel = createViewModel()
      viewModel.onDeleteConfirmed()

      onCommit.captured.invoke()

      coVerify(exactly = 1) { deleteTagWithAssignmentsUseCase("1") }
    }

  @Test
  fun `onDeleteConfirmed does nothing when the tag is already pending`() =
    runTest {
      val viewModel = createViewModel()
      pendingIds.value = setOf("1")

      viewModel.onDeleteConfirmed()

      verify(exactly = 0) { pendingDeleteCoordinator.markPending(any(), any(), any(), any()) }
    }
}
