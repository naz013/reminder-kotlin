package com.github.naz013.tags.compose

import androidx.compose.ui.graphics.Color
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.Tag
import com.github.naz013.feature.common.viewmodel.PendingDeleteCoordinator
import com.github.naz013.feature.common.viewmodel.PendingEntity
import com.github.naz013.feature.common.viewmodel.UndoEvent
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.logic.tag.DeleteTagWithAssignmentsUseCase
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.theme.ThemeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TagsViewModelTest : BaseTest() {
  private val tagRepository = mockk<TagRepository>()
  private val deleteTagWithAssignmentsUseCase = mockk<DeleteTagWithAssignmentsUseCase>(relaxed = true)
  private val themeProvider = mockk<ThemeProvider>()
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val coordinatorPendingIds = MutableStateFlow<Set<String>>(emptySet())
  private val pendingDeleteCoordinator = mockk<PendingDeleteCoordinator>(relaxed = true)

  private lateinit var viewModel: TagsViewModel

  private fun tag(
    id: String = "1",
    name: String = "Work",
    color: Int = 0,
  ) = Tag(id = id, name = name, color = color)

  @Before
  override fun setUp() {
    super.setUp()
    every { pendingDeleteCoordinator.pendingIds(PendingEntity.TAG) } returns coordinatorPendingIds
    every { tagRepository.observeAll() } returns flowOf(emptyList())
    every { themeProvider.themedColor(any<Int>()) } returns Color.Red

    viewModel = createViewModel()
  }

  private fun createViewModel(): TagsViewModel =
    TagsViewModel(
      dispatcherProvider = mockDispatcherProvider(),
      textProvider = textProvider,
      tagRepository = tagRepository,
      deleteTagWithAssignmentsUseCase = deleteTagWithAssignmentsUseCase,
      themeProvider = themeProvider,
      pendingDeleteCoordinator = pendingDeleteCoordinator,
    )

  /** Loads a ready list of tags (ids in order) and subscribes once so selection mutations made
   * between reads aren't discarded by a fresh collection's refresh - see the "Testing pitfall:
   * refresh-on-collection" note in docs/multiselect.md. */
  private fun TestScope.readyTags(ids: List<String>): Pair<TagsViewModel, () -> TagsScreenState> {
    val tags = ids.map { tag(id = it, name = it) }
    every { tagRepository.observeAll() } returns flowOf(tags)
    val vm = createViewModel()
    var latest = TagsScreenState()
    backgroundScope.launch(Dispatchers.Unconfined) { vm.state.collect { latest = it } }
    return vm to { latest }
  }

  @Test
  fun `loads empty state when there are no tags`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(TagsListState.Empty, state.listState)
    }

  @Test
  fun `loads tags from the repository`() =
    runTest {
      every { tagRepository.observeAll() } returns flowOf(listOf(tag(id = "1"), tag(id = "2")))
      val vm = createViewModel()

      val ready = vm.state.first().listState as TagsListState.Ready

      assertEquals(listOf("1", "2"), ready.tags.map { it.id })
    }

  @Test
  fun `onAddClick posts OpenEdit navigation event`() {
    viewModel.onAddClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(TagsViewModel.NavigationEvent.OpenEdit(null), event)
  }

  @Test
  fun `onTagMenuAction EDIT posts OpenEdit navigation event`() {
    viewModel.onTagMenuAction(TagState(id = "5", name = "Work", color = Color.Red), TagMenuAction.EDIT)

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(TagsViewModel.NavigationEvent.OpenEdit("5"), event)
  }

  @Test
  fun `onTagMenuAction DELETE posts ConfirmDelete navigation event`() {
    viewModel.onTagMenuAction(TagState(id = "5", name = "Work", color = Color.Red), TagMenuAction.DELETE)

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(TagsViewModel.NavigationEvent.ConfirmDelete("5"), event)
  }

  @Test
  fun `onTagMenuAction DELETE does not mark the tag pending before confirmation`() =
    runTest {
      viewModel.onTagMenuAction(TagState(id = "5", name = "Work", color = Color.Red), TagMenuAction.DELETE)

      assertTrue(viewModel.undoDelete.pendingIds.value.isEmpty())
      coVerify(exactly = 0) { deleteTagWithAssignmentsUseCase(any()) }
    }

  @Test
  fun `deleteTag marks the tag pending without detaching or deleting yet`() =
    runTest {
      every { textProvider.getText(R.string.tag_deleted) } returns "Tag deleted"

      viewModel.deleteTag("1")

      coVerify(exactly = 0) { deleteTagWithAssignmentsUseCase(any()) }
      assertEquals(
        UndoEvent(batchKey = "1", message = "Tag deleted", token = 1L),
        viewModel.undoDelete.undoEvents.first(),
      )
    }

  @Test
  fun `pending tag is hidden from the list and restored on undo`() =
    runTest {
      val (vm, state) = readyTags(listOf("1", "2"))
      every { textProvider.getText(R.string.tag_deleted) } returns "Tag deleted"

      vm.deleteTag("1")
      val hiddenIds = (state().listState as TagsListState.Ready).tags.map { it.id }
      vm.undoDelete.undo("1")
      val restoredIds = (state().listState as TagsListState.Ready).tags.map { it.id }

      assertEquals(listOf("2"), hiddenIds)
      assertEquals(listOf("1", "2"), restoredIds)
      coVerify(exactly = 0) { deleteTagWithAssignmentsUseCase(any()) }
    }

  @Test
  fun `commit detaches assignments and deletes the tag exactly once`() =
    runTest {
      viewModel.deleteTag("1")

      viewModel.undoDelete.commit("1")
      viewModel.undoDelete.commit("1")

      coVerify(exactly = 1) { deleteTagWithAssignmentsUseCase("1") }
    }

  @Test
  fun `clearing the view model commits a pending tag delete`() =
    runTest {
      viewModel.deleteTag("1")

      TagsViewModel::class.java.getDeclaredMethod("onCleared").apply { isAccessible = true }.invoke(viewModel)

      coVerify(exactly = 1) { deleteTagWithAssignmentsUseCase("1") }
    }

  @Test
  fun `onTagLongClick selects the tag and enters selection mode`() =
    runTest {
      val (vm, state) = readyTags(listOf("1", "2"))

      vm.onTagLongClick("1")

      val ready = state().listState as TagsListState.Ready
      assertEquals(1, state().selectedCount)
      assertTrue(ready.tags.first { it.id == "1" }.isSelected)
      assertFalse(ready.tags.first { it.id == "2" }.isSelected)
    }

  @Test
  fun `onTagClick toggles selection while in selection mode instead of opening details`() =
    runTest {
      val (vm, state) = readyTags(listOf("1", "2"))
      vm.onTagLongClick("1")

      vm.onTagClick("1")

      assertEquals(0, state().selectedCount)
      vm.onTagClick("2")
      assertEquals(
        TagsViewModel.NavigationEvent.OpenDetails("2"),
        vm.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `onSelectionCancel clears all selected tags`() =
    runTest {
      val (vm, state) = readyTags(listOf("1", "2"))
      vm.onTagLongClick("1")
      vm.onTagClick("2")

      vm.onSelectionCancel()

      assertEquals(0, state().selectedCount)
      val ready = state().listState as TagsListState.Ready
      assertFalse(ready.tags.any { it.isSelected })
    }

  @Test
  fun `onDeleteSelectedClick posts ConfirmDeleteSelected with the selected ids and a formatted title`() =
    runTest {
      every { textProvider.getText(R.string.tags_delete_selected_permanently, 2) } returns "Delete 2 tags permanently?"
      val (vm, _) = readyTags(listOf("1", "2"))
      vm.onTagLongClick("1")
      vm.onTagClick("2")

      vm.onDeleteSelectedClick()

      assertEquals(
        TagsViewModel.NavigationEvent.ConfirmDeleteSelected(setOf("1", "2"), "Delete 2 tags permanently?"),
        vm.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `onDeleteSelectedClick does not mark the selected tags pending before confirmation`() =
    runTest {
      val (vm, _) = readyTags(listOf("1", "2"))
      vm.onTagLongClick("1")
      vm.onTagClick("2")

      vm.onDeleteSelectedClick()

      assertTrue(vm.undoDelete.pendingIds.value.isEmpty())
      coVerify(exactly = 0) { deleteTagWithAssignmentsUseCase(any()) }
    }

  @Test
  fun `onDeleteSelectedClick does nothing when nothing is selected`() =
    runTest {
      val (vm, _) = readyTags(listOf("1"))

      vm.onDeleteSelectedClick()

      assertEquals(null, vm.navigationEvent.value)
    }

  @Test
  fun `deleteSelectedTags clears selection, hides the tags and does not detach or delete yet`() =
    runTest {
      every { textProvider.getText(R.string.tags_deleted_count, 2) } returns "2 tags deleted"
      val (vm, state) = readyTags(listOf("1", "2", "3"))
      vm.onTagLongClick("1")
      vm.onTagClick("2")

      vm.deleteSelectedTags(setOf("1", "2"))

      assertEquals(0, state().selectedCount)
      assertEquals(listOf("3"), (state().listState as TagsListState.Ready).tags.map { it.id })
      assertEquals("2 tags deleted", vm.undoDelete.undoEvents.first().message)
      coVerify(exactly = 0) { deleteTagWithAssignmentsUseCase(any()) }
    }

  @Test
  fun `commit after a bulk delete detaches assignments and deletes each tag`() =
    runTest {
      every { textProvider.getText(R.string.tags_deleted_count, 2) } returns "2 tags deleted"
      val (vm, _) = readyTags(listOf("1", "2"))
      vm.deleteSelectedTags(setOf("1", "2"))

      vm.undoDelete.commit(vm.undoDelete.undoEvents.first().batchKey)

      coVerify(exactly = 1) { deleteTagWithAssignmentsUseCase("1") }
      coVerify(exactly = 1) { deleteTagWithAssignmentsUseCase("2") }
    }

  @Test
  fun `undo after a bulk delete restores the tags and never deletes them`() =
    runTest {
      every { textProvider.getText(R.string.tags_deleted_count, 2) } returns "2 tags deleted"
      val (vm, state) = readyTags(listOf("1", "2"))
      vm.deleteSelectedTags(setOf("1", "2"))

      vm.undoDelete.undo(vm.undoDelete.undoEvents.first().batchKey)

      assertEquals(listOf("1", "2"), (state().listState as TagsListState.Ready).tags.map { it.id })
      coVerify(exactly = 0) { deleteTagWithAssignmentsUseCase(any()) }
    }

  @Test
  fun `applySelectedColor saves each selected tag with the new color and clears selection`() =
    runTest {
      val (vm, state) = readyTags(listOf("1", "2"))
      coEvery { tagRepository.getById("1") } returns tag(id = "1")
      coEvery { tagRepository.getById("2") } returns tag(id = "2")
      val savedTags = mutableListOf<Tag>()
      coEvery { tagRepository.save(capture(savedTags)) } returns Unit
      vm.onTagLongClick("1")
      vm.onTagClick("2")

      vm.applySelectedColor(3)

      assertEquals(setOf("1" to 3, "2" to 3), savedTags.map { it.id to it.color }.toSet())
      assertEquals(0, state().selectedCount)
    }

  @Test
  fun `applySelectedColor does nothing when nothing is selected`() =
    runTest {
      viewModel.applySelectedColor(3)

      coVerify(exactly = 0) { tagRepository.save(any()) }
    }
}
