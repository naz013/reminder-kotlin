package com.elementary.tasks.groups.list

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.groups.usecase.DeleteReminderGroupUseCase
import com.elementary.tasks.groups.usecase.MakeGroupDefaultUseCase
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.ReminderGroupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GroupsViewModelTest : BaseTest() {
  private val reminderGroupRepository = mockk<ReminderGroupRepository>()
  private val uiGroupListAdapter = mockk<UiGroupListAdapter>()
  private val deleteReminderGroupUseCase = mockk<DeleteReminderGroupUseCase>(relaxed = true)
  private val makeGroupDefaultUseCase = mockk<MakeGroupDefaultUseCase>(relaxed = true)

  private lateinit var viewModel: GroupsViewModel

  private fun reminderGroup(
    id: String = "1",
    title: String = "Work",
    isDefault: Boolean = false,
  ) = ReminderGroup(
    groupTitle = title,
    groupUuId = id,
    groupColor = 0,
    groupDateTime = "2026-07-23T10:00:00",
    isDefaultGroup = isDefault,
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
    coEvery { reminderGroupRepository.getAll() } returns emptyList()
    every { uiGroupListAdapter.convert(any<ReminderGroup>()) } returns uiGroupList()

    viewModel =
      GroupsViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        reminderGroupRepository = reminderGroupRepository,
        uiGroupListAdapter = uiGroupListAdapter,
        deleteReminderGroupUseCase = deleteReminderGroupUseCase,
        makeGroupDefaultUseCase = makeGroupDefaultUseCase,
      )
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
      val defaultGroup = reminderGroup(id = "2", title = "Zeta", isDefault = true)
      val otherGroup = reminderGroup(id = "1", title = "Alpha", isDefault = false)
      coEvery { reminderGroupRepository.getAll() } returns listOf(otherGroup, defaultGroup)
      every { uiGroupListAdapter.convert(defaultGroup) } returns uiGroupList(id = "2", title = "Zeta", isDefault = true)
      every { uiGroupListAdapter.convert(otherGroup) } returns uiGroupList(id = "1", title = "Alpha", isDefault = false)

      val state = viewModel.state.first()

      val ready = state.listState as ListState.Ready
      assertEquals(listOf("2", "1"), ready.groups.map { it.id })
    }

  @Test
  fun `reloads groups on each fresh state collection`() =
    runTest {
      viewModel.state.first()
      viewModel.state.first()

      coVerify(exactly = 2) { reminderGroupRepository.getAll() }
    }

  @Test
  fun `onAddClick posts AddGroup navigation event`() {
    viewModel.onAddClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(GroupsViewModel.NavigationEvent.AddGroup, event)
  }

  @Test
  fun `onGroupClick posts OpenEdit navigation event with the group id`() {
    viewModel.onGroupClick("42")

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(GroupsViewModel.NavigationEvent.OpenEdit("42"), event)
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
  fun `onGroupMenuAction MAKE_DEFAULT delegates to use case and reloads`() =
    runTest {
      viewModel.onGroupMenuAction(uiGroupList(id = "5"), GroupMenuAction.MAKE_DEFAULT)

      coVerify(exactly = 1) { makeGroupDefaultUseCase("5") }
    }

  @Test
  fun `deleteGroup does nothing when group is not found`() =
    runTest {
      coEvery { reminderGroupRepository.getById("missing") } returns null

      viewModel.deleteGroup("missing")

      coVerify(exactly = 0) { deleteReminderGroupUseCase(any()) }
    }

  @Test
  fun `deleteGroup deletes the group when it exists`() =
    runTest {
      coEvery { reminderGroupRepository.getById("1") } returns reminderGroup(id = "1")

      viewModel.deleteGroup("1")

      coVerify(exactly = 1) { deleteReminderGroupUseCase("1") }
    }
}
