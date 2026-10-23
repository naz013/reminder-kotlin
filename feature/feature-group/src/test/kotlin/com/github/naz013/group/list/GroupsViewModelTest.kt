package com.github.naz013.group.list

import com.github.naz013.common.TextProvider
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.viewmodel.UndoEvent
import com.github.naz013.logic.group.DeleteGroupUseCase
import com.github.naz013.logic.group.MakeGroupDefaultUseCase
import com.github.naz013.logic.group.SaveGroupUseCase
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.R
import com.github.naz013.ui.group.UiGroupList
import com.github.naz013.ui.group.UiGroupListAdapter
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

class GroupsViewModelTest : BaseTest() {
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val uiGroupListAdapter = mockk<UiGroupListAdapter>()
  private val deleteGroupUseCase = mockk<DeleteGroupUseCase>(relaxed = true)
  private val makeGroupDefaultUseCase = mockk<MakeGroupDefaultUseCase>(relaxed = true)
  private val saveGroupUseCase = mockk<SaveGroupUseCase>(relaxed = true)
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val textProvider = mockk<TextProvider>(relaxed = true)

  private lateinit var viewModel: GroupsViewModel

  private fun groupV2(
    id: String = "1",
    title: String = "Work",
    isDefault: Boolean = false,
  ) = GroupV2(
    uuId = id,
    title = title,
    color = 0,
    isDefault = isDefault,
    syncState = SyncState.Synced,
  )

  private fun uiGroupList(
    id: String = "1",
    title: String = "Work",
    isDefault: Boolean = false,
  ) = UiGroupList(
    id = id,
    title = title,
    color = 0,
    colorPosition = 0,
    contrastColor = 0,
    isDefaultGroup = isDefault,
    canDelete = !isDefault,
    canSetAsDefault = !isDefault,
  )

  @Before
  override fun setUp() {
    super.setUp()
    every { groupV2Repository.observeAll() } returns flowOf(emptyList())
    coEvery { reminderV2Repository.countActiveByGroupId(any()) } returns 0
    every { uiGroupListAdapter.convert(any<GroupV2>(), any()) } returns uiGroupList()

    viewModel = createViewModel()
  }

  private fun createViewModel(): GroupsViewModel =
    GroupsViewModel(
      dispatcherProvider = mockDispatcherProvider(),
      textProvider = textProvider,
      groupV2Repository = groupV2Repository,
      uiGroupListAdapter = uiGroupListAdapter,
      deleteGroupUseCase = deleteGroupUseCase,
      makeGroupDefaultUseCase = makeGroupDefaultUseCase,
      saveGroupUseCase = saveGroupUseCase,
      reminderV2Repository = reminderV2Repository,
    )

  /** Loads a ready list of groups (ids in order, optionally marking one as the default group) and
   * subscribes once so selection mutations made between reads aren't discarded by a fresh
   * collection's refresh - see the "Testing pitfall: refresh-on-collection" note in
   * docs/multiselect.md. */
  private fun TestScope.readyGroups(
    ids: List<String>,
    defaultId: String? = null,
  ): Pair<GroupsViewModel, () -> GroupsScreenState> {
    val groups = ids.map { groupV2(id = it, title = it, isDefault = it == defaultId) }
    every { groupV2Repository.observeAll() } returns flowOf(groups)
    groups.forEach { g ->
      every { uiGroupListAdapter.convert(g, any()) } returns
        uiGroupList(id = g.uuId, title = g.title, isDefault = g.isDefault)
    }
    val vm = createViewModel()
    var latest = GroupsScreenState()
    backgroundScope.launch(Dispatchers.Unconfined) { vm.state.collect { latest = it } }
    return vm to { latest }
  }

  @Test
  fun `loads empty state when there are no groups`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(ListState.Empty, state.listState)
    }

  @Test
  fun `loads groups sorted with default group first then alphabetically`() =
    runTest {
      val defaultGroup = groupV2(id = "2", title = "Zeta", isDefault = true)
      val otherGroup = groupV2(id = "1", title = "Alpha", isDefault = false)
      every { groupV2Repository.observeAll() } returns flowOf(listOf(otherGroup, defaultGroup))
      every { uiGroupListAdapter.convert(defaultGroup, any()) } returns
        uiGroupList(id = "2", title = "Zeta", isDefault = true)
      every { uiGroupListAdapter.convert(otherGroup, any()) } returns
        uiGroupList(id = "1", title = "Alpha", isDefault = false)
      val vm = createViewModel()

      val state = vm.state.first()

      val ready = state.listState as ListState.Ready
      assertEquals(listOf("2", "1"), ready.groups.map { it.id })
    }

  @Test
  fun `loads each group with its active reminder count`() =
    runTest {
      val group = groupV2(id = "1", title = "Work")
      every { groupV2Repository.observeAll() } returns flowOf(listOf(group))
      coEvery { reminderV2Repository.countActiveByGroupId("1") } returns 4
      every { uiGroupListAdapter.convert(group, 4) } returns uiGroupList(id = "1", title = "Work")
      val vm = createViewModel()

      vm.state.first()

      coVerify(exactly = 1) { uiGroupListAdapter.convert(group, 4) }
    }

  @Test
  fun `state updates reactively when the groups Flow emits a new list`() =
    runTest {
      val group = groupV2(id = "1", title = "Work")
      val groupsFlow = MutableStateFlow<List<GroupV2>>(emptyList())
      every { groupV2Repository.observeAll() } returns groupsFlow
      every { uiGroupListAdapter.convert(group, any()) } returns uiGroupList(id = "1", title = "Work")
      val vm = createViewModel()
      assertEquals(ListState.Empty, vm.state.first().listState)

      groupsFlow.value = listOf(group)

      val ready = vm.state.first().listState as ListState.Ready
      assertEquals(listOf("1"), ready.groups.map { it.id })
    }

  @Test
  fun `onAddClick posts AddGroup navigation event`() {
    viewModel.onAddClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(GroupsViewModel.NavigationEvent.AddGroup, event)
  }

  @Test
  fun `onGroupClick posts OpenDetails navigation event with the group id`() {
    viewModel.onGroupClick("42")

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(GroupsViewModel.NavigationEvent.OpenDetails("42"), event)
  }

  @Test
  fun `onGroupMenuAction EDIT posts OpenEdit navigation event`() {
    viewModel.onGroupMenuAction(uiGroupList(id = "5"), GroupMenuAction.EDIT)

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(GroupsViewModel.NavigationEvent.OpenEdit("5"), event)
  }

  @Test
  fun `onGroupMenuAction DELETE posts ConfirmDelete navigation event`() {
    viewModel.onGroupMenuAction(uiGroupList(id = "5"), GroupMenuAction.DELETE)

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(GroupsViewModel.NavigationEvent.ConfirmDelete("5"), event)
  }

  @Test
  fun `onGroupMenuAction DELETE does not mark the group pending before confirmation`() =
    runTest {
      viewModel.onGroupMenuAction(uiGroupList(id = "1"), GroupMenuAction.DELETE)

      assertTrue(viewModel.undoDelete.pendingIds.value.isEmpty())
      coVerify(exactly = 0) { deleteGroupUseCase(any()) }
    }

  @Test
  fun `onGroupMenuAction MAKE_DEFAULT delegates to use case`() =
    runTest {
      viewModel.onGroupMenuAction(uiGroupList(id = "5"), GroupMenuAction.MAKE_DEFAULT)

      coVerify(exactly = 1) { makeGroupDefaultUseCase("5") }
    }

  @Test
  fun `deleteGroup does nothing when group is not found`() =
    runTest {
      coEvery { groupV2Repository.getById("missing") } returns null

      viewModel.deleteGroup("missing")

      coVerify(exactly = 0) { deleteGroupUseCase(any()) }
      assertTrue(viewModel.undoDelete.pendingIds.value.isEmpty())
    }

  @Test
  fun `deleteGroup does nothing for the default group`() =
    runTest {
      coEvery { groupV2Repository.getById("1") } returns groupV2(id = "1", isDefault = true)

      viewModel.deleteGroup("1")
      viewModel.undoDelete.commit("1")

      coVerify(exactly = 0) { deleteGroupUseCase(any()) }
      assertTrue(viewModel.undoDelete.pendingIds.value.isEmpty())
    }

  @Test
  fun `deleteGroup marks the group pending without cascading yet`() =
    runTest {
      coEvery { groupV2Repository.getById("1") } returns groupV2(id = "1")
      every { textProvider.getText(R.string.group_deleted) } returns "Group deleted"

      viewModel.deleteGroup("1")

      coVerify(exactly = 0) { deleteGroupUseCase(any()) }
      assertEquals(
        UndoEvent(batchKey = "1", message = "Group deleted", token = 1L),
        viewModel.undoDelete.undoEvents.first(),
      )
    }

  @Test
  fun `pending group is hidden from the list and restored on undo without deleting`() =
    runTest {
      val (vm, state) = readyGroups(listOf("1", "2"))
      coEvery { groupV2Repository.getById("1") } returns groupV2(id = "1")

      vm.deleteGroup("1")
      val hiddenIds = (state().listState as ListState.Ready).groups.map { it.id }
      vm.undoDelete.undo("1")
      val restoredIds = (state().listState as ListState.Ready).groups.map { it.id }

      assertEquals(listOf("2"), hiddenIds)
      assertEquals(listOf("1", "2"), restoredIds)
      coVerify(exactly = 0) { deleteGroupUseCase(any()) }
    }

  @Test
  fun `commit runs the delete cascade exactly once`() =
    runTest {
      coEvery { groupV2Repository.getById("1") } returns groupV2(id = "1")
      viewModel.deleteGroup("1")

      viewModel.undoDelete.commit("1")
      viewModel.undoDelete.commit("1")

      coVerify(exactly = 1) { deleteGroupUseCase("1") }
    }

  @Test
  fun `clearing the view model commits a pending group delete`() =
    runTest {
      coEvery { groupV2Repository.getById("1") } returns groupV2(id = "1")
      viewModel.deleteGroup("1")

      GroupsViewModel::class.java.getDeclaredMethod("onCleared").apply { isAccessible = true }.invoke(viewModel)

      coVerify(exactly = 1) { deleteGroupUseCase("1") }
    }

  @Test
  fun `onGroupLongClick selects the group and enters selection mode`() =
    runTest {
      val (vm, state) = readyGroups(listOf("1", "2"))

      vm.onGroupLongClick("1")

      val ready = state().listState as ListState.Ready
      assertEquals(1, state().selectedCount)
      assertTrue(ready.groups.first { it.id == "1" }.isSelected)
      assertFalse(ready.groups.first { it.id == "2" }.isSelected)
    }

  @Test
  fun `onGroupClick toggles selection while in selection mode instead of opening details`() =
    runTest {
      val (vm, state) = readyGroups(listOf("1", "2"))
      vm.onGroupLongClick("1")

      vm.onGroupClick("1")

      assertEquals(0, state().selectedCount)
      vm.onGroupClick("2")
      assertEquals(
        GroupsViewModel.NavigationEvent.OpenDetails("2"),
        vm.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `onSelectionCancel clears all selected groups`() =
    runTest {
      val (vm, state) = readyGroups(listOf("1", "2"))
      vm.onGroupLongClick("1")
      vm.onGroupClick("2")

      vm.onSelectionCancel()

      assertEquals(0, state().selectedCount)
      val ready = state().listState as ListState.Ready
      assertFalse(ready.groups.any { it.isSelected })
    }

  @Test
  fun `onDeleteSelectedClick posts ConfirmDeleteSelected with the selected ids and a formatted title`() =
    runTest {
      every { textProvider.getText(R.string.groups_delete_selected_permanently, 2) } returns "Delete 2 groups permanently?"
      val (vm, _) = readyGroups(listOf("1", "2"))
      vm.onGroupLongClick("1")
      vm.onGroupClick("2")

      vm.onDeleteSelectedClick()

      assertEquals(
        GroupsViewModel.NavigationEvent.ConfirmDeleteSelected(setOf("1", "2"), "Delete 2 groups permanently?"),
        vm.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `onDeleteSelectedClick does not mark the selected groups pending before confirmation`() =
    runTest {
      val (vm, _) = readyGroups(listOf("1", "2"))
      vm.onGroupLongClick("1")
      vm.onGroupClick("2")

      vm.onDeleteSelectedClick()

      assertTrue(vm.undoDelete.pendingIds.value.isEmpty())
      coVerify(exactly = 0) { deleteGroupUseCase(any()) }
    }

  @Test
  fun `onDeleteSelectedClick does nothing when nothing is selected`() =
    runTest {
      val (vm, _) = readyGroups(listOf("1"))

      vm.onDeleteSelectedClick()

      assertEquals(null, vm.navigationEvent.value)
    }

  @Test
  fun `onDeleteSelectedClick does nothing when the default group is selected`() =
    runTest {
      val (vm, _) = readyGroups(listOf("1", "2"), defaultId = "1")
      vm.onGroupLongClick("1")
      vm.onGroupClick("2")

      vm.onDeleteSelectedClick()

      assertEquals(null, vm.navigationEvent.value)
    }

  @Test
  fun `deleteSelectedGroups clears selection, hides the groups and does not cascade yet`() =
    runTest {
      every { textProvider.getText(R.string.groups_deleted_count, 2) } returns "2 groups deleted"
      val (vm, state) = readyGroups(listOf("1", "2", "3"))
      vm.onGroupLongClick("1")
      vm.onGroupClick("2")

      vm.deleteSelectedGroups(setOf("1", "2"))

      assertEquals(0, state().selectedCount)
      assertEquals(listOf("3"), (state().listState as ListState.Ready).groups.map { it.id })
      assertEquals("2 groups deleted", vm.undoDelete.undoEvents.first().message)
      coVerify(exactly = 0) { deleteGroupUseCase(any()) }
    }

  @Test
  fun `commit after a bulk delete cascades each group`() =
    runTest {
      every { textProvider.getText(R.string.groups_deleted_count, 2) } returns "2 groups deleted"
      val (vm, _) = readyGroups(listOf("1", "2"))
      vm.deleteSelectedGroups(setOf("1", "2"))

      vm.undoDelete.commit(vm.undoDelete.undoEvents.first().batchKey)

      coVerify(exactly = 1) { deleteGroupUseCase("1") }
      coVerify(exactly = 1) { deleteGroupUseCase("2") }
    }

  @Test
  fun `undo after a bulk delete restores the groups and never deletes them`() =
    runTest {
      every { textProvider.getText(R.string.groups_deleted_count, 2) } returns "2 groups deleted"
      val (vm, state) = readyGroups(listOf("1", "2"))
      vm.deleteSelectedGroups(setOf("1", "2"))

      vm.undoDelete.undo(vm.undoDelete.undoEvents.first().batchKey)

      assertEquals(listOf("1", "2"), (state().listState as ListState.Ready).groups.map { it.id })
      coVerify(exactly = 0) { deleteGroupUseCase(any()) }
    }

  @Test
  fun `deleteSelectedGroups does nothing when the default group is included`() =
    runTest {
      val (vm, _) = readyGroups(listOf("1", "2"), defaultId = "1")

      vm.deleteSelectedGroups(setOf("1", "2"))
      vm.undoDelete.commit("x")

      assertTrue(vm.undoDelete.pendingIds.value.isEmpty())
      coVerify(exactly = 0) { deleteGroupUseCase(any()) }
    }

  @Test
  fun `applySelectedColor saves each selected group with the new color and clears selection`() =
    runTest {
      val (vm, state) = readyGroups(listOf("1", "2"))
      coEvery { groupV2Repository.getById("1") } returns groupV2(id = "1")
      coEvery { groupV2Repository.getById("2") } returns groupV2(id = "2")
      val savedGroups = mutableListOf<GroupV2>()
      coEvery { saveGroupUseCase(capture(savedGroups)) } returns Unit
      vm.onGroupLongClick("1")
      vm.onGroupClick("2")

      vm.applySelectedColor(3)

      assertEquals(setOf("1" to 3, "2" to 3), savedGroups.map { it.uuId to it.color }.toSet())
      assertEquals(0, state().selectedCount)
    }

  @Test
  fun `applySelectedColor does nothing when nothing is selected`() =
    runTest {
      viewModel.applySelectedColor(3)

      coVerify(exactly = 0) { saveGroupUseCase(any()) }
    }
}
