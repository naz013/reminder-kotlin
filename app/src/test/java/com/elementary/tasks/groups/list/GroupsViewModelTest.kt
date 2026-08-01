package com.elementary.tasks.groups.list

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.groups.usecase.DeleteGroupUseCase
import com.elementary.tasks.groups.usecase.MakeGroupDefaultUseCase
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.usecase.reminders.CountActiveRemindersV2ByGroupIdUseCase
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
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val uiGroupListAdapter = mockk<UiGroupListAdapter>()
  private val deleteGroupUseCase = mockk<DeleteGroupUseCase>(relaxed = true)
  private val makeGroupDefaultUseCase = mockk<MakeGroupDefaultUseCase>(relaxed = true)
  private val countActiveRemindersV2ByGroupIdUseCase = mockk<CountActiveRemindersV2ByGroupIdUseCase>()

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
    coEvery { groupV2Repository.getAll() } returns emptyList()
    coEvery { countActiveRemindersV2ByGroupIdUseCase(any()) } returns 0
    every { uiGroupListAdapter.convert(any<GroupV2>(), any()) } returns uiGroupList()

    viewModel =
      GroupsViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        groupV2Repository = groupV2Repository,
        uiGroupListAdapter = uiGroupListAdapter,
        deleteGroupUseCase = deleteGroupUseCase,
        makeGroupDefaultUseCase = makeGroupDefaultUseCase,
        countActiveRemindersV2ByGroupIdUseCase = countActiveRemindersV2ByGroupIdUseCase,
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
      val defaultGroup = groupV2(id = "2", title = "Zeta", isDefault = true)
      val otherGroup = groupV2(id = "1", title = "Alpha", isDefault = false)
      coEvery { groupV2Repository.getAll() } returns listOf(otherGroup, defaultGroup)
      every { uiGroupListAdapter.convert(defaultGroup, any()) } returns uiGroupList(id = "2", title = "Zeta", isDefault = true)
      every { uiGroupListAdapter.convert(otherGroup, any()) } returns uiGroupList(id = "1", title = "Alpha", isDefault = false)

      val state = viewModel.state.first()

      val ready = state.listState as ListState.Ready
      assertEquals(listOf("2", "1"), ready.groups.map { it.id })
    }

  @Test
  fun `loads each group with its active reminder count`() =
    runTest {
      val group = groupV2(id = "1", title = "Work")
      coEvery { groupV2Repository.getAll() } returns listOf(group)
      coEvery { countActiveRemindersV2ByGroupIdUseCase("1") } returns 4
      every { uiGroupListAdapter.convert(group, 4) } returns uiGroupList(id = "1", title = "Work")

      viewModel.state.first()

      coVerify(exactly = 1) { uiGroupListAdapter.convert(group, 4) }
    }

  @Test
  fun `reloads groups on each fresh state collection`() =
    runTest {
      viewModel.state.first()
      viewModel.state.first()

      coVerify(exactly = 2) { groupV2Repository.getAll() }
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
  fun `onGroupMenuAction MAKE_DEFAULT delegates to use case and reloads`() =
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
    }

  @Test
  fun `deleteGroup deletes the group when it exists`() =
    runTest {
      coEvery { groupV2Repository.getById("1") } returns groupV2(id = "1")

      viewModel.deleteGroup("1")

      coVerify(exactly = 1) { deleteGroupUseCase("1") }
    }
}
