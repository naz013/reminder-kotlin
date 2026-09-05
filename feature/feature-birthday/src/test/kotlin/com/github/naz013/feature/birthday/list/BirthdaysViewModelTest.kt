package com.github.naz013.feature.birthday.list

import com.github.naz013.common.TextProvider
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logic.birthday.BirthdaySmartListPredicate
import com.github.naz013.logic.birthday.DeleteBirthdayUseCase
import com.github.naz013.logic.reminder.smartlist.SmartListFilter
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.agenda.AgendaMenuAction
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaItemAdapter
import com.github.naz013.ui.common.R
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
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class BirthdaysViewModelTest : BaseTest() {
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val tagRepository = mockk<TagRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val tagChipStateAdapter = mockk<TagChipStateAdapter>()
  private val uiAgendaItemAdapter = mockk<UiAgendaItemAdapter>()
  private val birthdaySmartListPredicate = mockk<BirthdaySmartListPredicate>()
  private val deleteBirthdayUseCase = mockk<DeleteBirthdayUseCase>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)

  private lateinit var viewModel: BirthdaysViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { birthdayRepository.observeAll() } returns flowOf(emptyList())
    every { tagRepository.observeAll() } returns flowOf(emptyList())
    viewModel = createViewModel()
  }

  private fun createViewModel(): BirthdaysViewModel =
    BirthdaysViewModel(
      dispatcherProvider = mockDispatcherProvider(),
      textProvider = textProvider,
      birthdayRepository = birthdayRepository,
      tagRepository = tagRepository,
      tagAssignmentRepository = tagAssignmentRepository,
      tagChipStateAdapter = tagChipStateAdapter,
      uiAgendaItemAdapter = uiAgendaItemAdapter,
      birthdaySmartListPredicate = birthdaySmartListPredicate,
      deleteBirthdayUseCase = deleteBirthdayUseCase,
    )

  private fun birthday(
    id: String = "1",
    name: String = "Alice",
  ) = Birthday(uuId = id, name = name, syncState = SyncState.Synced)

  private fun uiBirthday(id: String = "1") =
    UiAgendaBirthday(
      id = id,
      dateTime = LocalDateTime.of(2026, 1, 1, 0, 0),
      name = "Alice",
      ageFormatted = "25",
      remainingTimeFormatted = null,
      color = 0,
      contrastColor = 0,
      dateFormatted = "1 Jan",
    )

  @Test
  fun `loads birthdays into ready state on first collection`() =
    runTest {
      val b = birthday()
      every { birthdayRepository.observeAll() } returns flowOf(listOf(b))
      every { uiAgendaItemAdapter.convertBirthday(b) } returns uiBirthday()
      val vm = createViewModel()

      val state = vm.state.first()

      val ready = state.listState as ListState.Ready
      assertEquals(1, ready.items.size)
    }

  @Test
  fun `loads empty state when there are no birthdays`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(ListState.Empty, state.listState)
    }

  @Test
  fun `onSearchQueryChange updates the search query in state immediately`() =
    runTest {
      viewModel.onSearchQueryChange("Alice")

      assertEquals("Alice", viewModel.state.first().searchQuery)
    }

  @Test
  fun `onSmartListSelected filters birthdays using the predicate`() =
    runTest {
      val match = birthday(id = "1")
      val noMatch = birthday(id = "2")
      every { birthdayRepository.observeAll() } returns flowOf(listOf(match, noMatch))
      // Both need a stub: before onSmartListSelected fires, the combine's first emission has no
      // smart-list filter applied yet, so both birthdays get converted for that unfiltered pass.
      every { uiAgendaItemAdapter.convertBirthday(match) } returns uiBirthday(id = "1")
      every { uiAgendaItemAdapter.convertBirthday(noMatch) } returns uiBirthday(id = "2")
      every { birthdaySmartListPredicate.matches(SmartListFilter.TODAY, match, any<LocalDate>()) } returns true
      every { birthdaySmartListPredicate.matches(SmartListFilter.TODAY, noMatch, any<LocalDate>()) } returns false
      val vm = createViewModel()

      vm.onSmartListSelected(SmartListFilter.TODAY)

      val ready = vm.state.first().listState as ListState.Ready
      assertEquals(listOf("1"), ready.items.map { it.id })
    }

  @Test
  fun `onSmartListSelected twice with the same filter clears it`() =
    runTest {
      viewModel.onSmartListSelected(SmartListFilter.TODAY)

      viewModel.onSmartListSelected(SmartListFilter.TODAY)

      assertEquals(null, viewModel.state.first().selectedSmartList)
    }

  @Test
  fun `onTagFilterSelected filters birthdays down to items carrying that tag`() =
    runTest {
      val b1 = birthday(id = "1")
      val b2 = birthday(id = "2")
      every { birthdayRepository.observeAll() } returns flowOf(listOf(b1, b2))
      every { uiAgendaItemAdapter.convertBirthday(b1) } returns uiBirthday(id = "1")
      every { uiAgendaItemAdapter.convertBirthday(b2) } returns uiBirthday(id = "2")
      coEvery { tagAssignmentRepository.getItemIdsForTag("tag1", TaggedItemType.BIRTHDAY) } returns listOf("1")
      val vm = createViewModel()

      vm.onTagFilterSelected("tag1")

      val ready = vm.state.first().listState as ListState.Ready
      assertEquals(listOf("1"), ready.items.map { it.id })
    }

  @Test
  fun `onItemClick posts OpenPreview navigation event`() {
    viewModel.onItemClick(uiBirthday(id = "7"))

    assertEquals(
      BirthdaysViewModel.NavigationEvent.OpenPreview("7"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onMenuAction OPEN posts OpenPreview navigation event`() {
    viewModel.onMenuAction(uiBirthday(id = "7"), AgendaMenuAction.OPEN)

    assertEquals(
      BirthdaysViewModel.NavigationEvent.OpenPreview("7"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onMenuAction EDIT posts OpenEdit navigation event`() {
    viewModel.onMenuAction(uiBirthday(id = "7"), AgendaMenuAction.EDIT)

    assertEquals(
      BirthdaysViewModel.NavigationEvent.OpenEdit("7"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onMenuAction DELETE shows the delete confirmation instead of posting a navigation event`() =
    runTest {
      viewModel.onMenuAction(uiBirthday(id = "7"), AgendaMenuAction.DELETE)

      assertEquals("7", viewModel.state.first().confirmDeleteId)
      assertEquals(null, viewModel.navigationEvent.value)
    }

  @Test
  fun `onDeleteConfirmed hides the confirmation without deleting yet and posts ShowUndoDelete`() =
    runTest {
      every { textProvider.getText(R.string.birthday_deleted) } returns "Birthday deleted"
      viewModel.onMenuAction(uiBirthday(id = "7"), AgendaMenuAction.DELETE)

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 0) { deleteBirthdayUseCase(any()) }
      assertEquals(null, viewModel.state.first().confirmDeleteId)
      assertEquals(
        BirthdaysViewModel.NavigationEvent.ShowUndoDelete(batchKey = "7", message = "Birthday deleted"),
        viewModel.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `undoDelete cancels the pending delete`() =
    runTest {
      viewModel.onMenuAction(uiBirthday(id = "7"), AgendaMenuAction.DELETE)
      viewModel.onDeleteConfirmed()

      viewModel.undoDelete("7")

      coVerify(exactly = 0) { deleteBirthdayUseCase(any()) }
    }

  @Test
  fun `commitDelete deletes the birthday after the undo window`() =
    runTest {
      viewModel.onMenuAction(uiBirthday(id = "7"), AgendaMenuAction.DELETE)
      viewModel.onDeleteConfirmed()

      viewModel.commitDelete("7")

      coVerify(exactly = 1) { deleteBirthdayUseCase("7") }
    }

  @Test
  fun `onDeleteDismiss hides the confirmation without deleting`() =
    runTest {
      viewModel.onMenuAction(uiBirthday(id = "7"), AgendaMenuAction.DELETE)

      viewModel.onDeleteDismiss()

      coVerify(exactly = 0) { deleteBirthdayUseCase(any()) }
      assertEquals(null, viewModel.state.first().confirmDeleteId)
    }

  @Test
  fun `onAddClick posts OpenNewBirthday navigation event`() {
    viewModel.onAddClick()

    assertEquals(
      BirthdaysViewModel.NavigationEvent.OpenNewBirthday,
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  private fun readyViewModelWithTwoBirthdays(): BirthdaysViewModel {
    val b1 = birthday(id = "1")
    val b2 = birthday(id = "2")
    every { birthdayRepository.observeAll() } returns flowOf(listOf(b1, b2))
    every { uiAgendaItemAdapter.convertBirthday(b1) } returns uiBirthday(id = "1")
    every { uiAgendaItemAdapter.convertBirthday(b2) } returns uiBirthday(id = "2")
    return createViewModel()
  }

  @Test
  fun `onItemLongClick selects the birthday and enters selection mode`() =
    runTest {
      val vm = readyViewModelWithTwoBirthdays()

      vm.onItemLongClick("1")

      val state = vm.state.first()
      assertEquals(1, state.selectedCount)
      val ready = state.listState as ListState.Ready
      assertEquals(true, ready.items.first { it.id == "1" }.isSelected)
    }

  @Test
  fun `onItemClick toggles selection while in selection mode instead of opening the birthday`() =
    runTest {
      val vm = readyViewModelWithTwoBirthdays()
      vm.onItemLongClick("1")

      vm.onItemClick(uiBirthday(id = "2"))

      assertEquals(2, vm.state.first().selectedCount)
      assertEquals(null, vm.navigationEvent.value)
    }

  @Test
  fun `onItemClick exits selection mode automatically after the last birthday is deselected`() =
    runTest {
      val vm = readyViewModelWithTwoBirthdays()
      vm.onItemLongClick("1")

      vm.onItemClick(uiBirthday(id = "1"))

      assertEquals(0, vm.state.first().selectedCount)
    }

  @Test
  fun `onSelectionCancel clears all selected birthdays`() =
    runTest {
      val vm = readyViewModelWithTwoBirthdays()
      vm.onItemLongClick("1")
      vm.onItemClick(uiBirthday(id = "2"))

      vm.onSelectionCancel()

      assertEquals(0, vm.state.first().selectedCount)
    }

  @Test
  fun `onDeleteSelectedClick posts ConfirmDeleteSelected with the selected ids and a formatted title`() =
    runTest {
      every {
        textProvider.getText(R.string.birthdays_delete_selected_permanently, 2)
      } returns "Delete 2 birthdays permanently?"
      val vm = readyViewModelWithTwoBirthdays()
      vm.onItemLongClick("1")
      vm.onItemClick(uiBirthday(id = "2"))

      vm.onDeleteSelectedClick()

      assertEquals(
        BirthdaysViewModel.NavigationEvent.ConfirmDeleteSelected(setOf("1", "2"), "Delete 2 birthdays permanently?"),
        vm.navigationEvent.value?.peekContent(),
      )
    }

  @Test
  fun `onDeleteSelectedClick does nothing when nothing is selected`() =
    runTest {
      val vm = readyViewModelWithTwoBirthdays()

      vm.onDeleteSelectedClick()

      assertEquals(null, vm.navigationEvent.value)
    }

  @Test
  fun `deleteSelectedBirthdays clears selection and posts ShowUndoDelete without deleting yet`() =
    runTest {
      every { textProvider.getText(R.string.birthdays_deleted_count, 2) } returns "2 birthdays deleted"
      val vm = readyViewModelWithTwoBirthdays()
      vm.onItemLongClick("1")
      vm.onItemClick(uiBirthday(id = "2"))

      vm.deleteSelectedBirthdays(setOf("1", "2"))

      coVerify(exactly = 0) { deleteBirthdayUseCase(any()) }
      assertEquals(0, vm.state.first().selectedCount)
      val event = vm.navigationEvent.value?.peekContent() as BirthdaysViewModel.NavigationEvent.ShowUndoDelete
      assertEquals("2 birthdays deleted", event.message)
    }

  @Test
  fun `commitDelete after a bulk delete deletes each birthday`() =
    runTest {
      val vm = readyViewModelWithTwoBirthdays()
      vm.onItemLongClick("1")
      vm.onItemClick(uiBirthday(id = "2"))
      vm.deleteSelectedBirthdays(setOf("1", "2"))
      val batchKey =
        (vm.navigationEvent.value?.peekContent() as BirthdaysViewModel.NavigationEvent.ShowUndoDelete).batchKey

      vm.commitDelete(batchKey)

      coVerify(exactly = 1) { deleteBirthdayUseCase("1") }
      coVerify(exactly = 1) { deleteBirthdayUseCase("2") }
    }
}
