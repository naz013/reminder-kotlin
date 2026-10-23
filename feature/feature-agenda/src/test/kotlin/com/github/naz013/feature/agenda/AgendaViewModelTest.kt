package com.github.naz013.feature.agenda

import com.github.naz013.common.TextProvider
import com.github.naz013.logic.birthday.BirthdaySmartListPredicate
import com.github.naz013.logic.birthday.DeleteBirthdayUseCase
import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.common.text.UiTextFormat
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListState
import com.github.naz013.logic.reminder.usecase.ResolveWaitingTitlesUseCase
import com.github.naz013.logic.reminder.usecase.SkipReminderUseCase
import com.github.naz013.logic.reminder.usecase.ToggleReminderStateUseCase
import com.github.naz013.logic.reminder.usecase.TogglePinnedReminderUseCase
import com.github.naz013.logic.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.provideBirthdayDateCalculator
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logic.reminder.AgendaViewMode
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.smartlist.SmartListFilter
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.agenda.AgendaCategory
import com.github.naz013.ui.agenda.AgendaMenuAction
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.ui.agenda.UiAgendaItemAdapter
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.ui.common.R
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import com.github.naz013.feature.common.viewmodel.PendingDeleteCoordinator
import com.github.naz013.feature.common.viewmodel.PendingEntity
import com.github.naz013.feature.common.viewmodel.UndoEvent
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for [AgendaViewModel]'s merge/sort/filter pipeline ([AgendaViewModel.loadMerged]).
 * [UiAgendaItemAdapter] is mocked so these tests can focus purely on which reminders/birthdays make
 * it through category and search-query filtering, rather than on presentation formatting (covered
 * separately by ui-agenda's own UiAgendaItemAdapterTest).
 */
class AgendaViewModelTest : BaseTest() {
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val tagRepository = mockk<TagRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val uiAgendaItemAdapter = mockk<UiAgendaItemAdapter>()
  private val resolveWaitingTitlesUseCase = mockk<ResolveWaitingTitlesUseCase>()
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val moveReminderToArchiveUseCase = mockk<MoveReminderToArchiveUseCase>()
  private val skipReminderUseCase = mockk<SkipReminderUseCase>()
  private val toggleReminderStateUseCase = mockk<ToggleReminderStateUseCase>()
  private val togglePinnedReminderUseCase = mockk<TogglePinnedReminderUseCase>(relaxed = true)
  private val deleteReminderUseCase = mockk<DeleteReminderUseCase>()
  private val deleteBirthdayUseCase = mockk<DeleteBirthdayUseCase>()
  private val birthdaySmartListPredicate = BirthdaySmartListPredicate(provideBirthdayDateCalculator())
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val reminderPreferences = mockk<ReminderPreferences>(relaxed = true)
  private val coordinatorReminderPendingIds = MutableStateFlow<Set<String>>(emptySet())
  private val coordinatorBirthdayPendingIds = MutableStateFlow<Set<String>>(emptySet())
  private val pendingDeleteCoordinator = mockk<PendingDeleteCoordinator>(relaxed = true)

  private lateinit var viewModel: AgendaViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { pendingDeleteCoordinator.pendingIds(PendingEntity.REMINDER) } returns coordinatorReminderPendingIds
    every { pendingDeleteCoordinator.pendingIds(PendingEntity.BIRTHDAY) } returns coordinatorBirthdayPendingIds
    every { reminderPreferences.agendaViewMode } returns AgendaViewMode.LIST
    coEvery { groupV2Repository.getAll() } returns emptyList()
    // AgendaViewModel's init{} eagerly runs the load pipeline once on construction, and
    // mockDispatcherProvider() uses Dispatchers.Unconfined, so that eager call executes
    // synchronously here in setUp() before any test body runs. Stub safe defaults so it
    // doesn't crash on unmocked calls; individual tests override these as needed.
    coEvery { reminderV2Repository.getByRemovedStatus(any()) } returns emptyList()
    coEvery { birthdayRepository.getAll() } returns emptyList()
    coEvery { tagRepository.getAll() } returns emptyList()
    coEvery { tagAssignmentRepository.getItemIdsForTag(any(), any()) } returns emptyList()
    // AgendaViewModel now also observes these reactively (dataChangedSignal) purely to know when
    // to re-run loadMerged() - it never reads their emitted values - so a single-emission stub is
    // enough for construction to succeed; tests still drive behavior through loadMerged() directly.
    every { reminderV2Repository.observeByRemovedStatus(any()) } returns flowOf(emptyList())
    every { birthdayRepository.observeAll() } returns flowOf(emptyList())
    every { groupV2Repository.observeAll() } returns flowOf(emptyList())
    every { tagRepository.observeAll() } returns flowOf(emptyList())
    // Echoes the filtered reminders/birthdays back as bare UiAgendaItems keyed by id, so tests can
    // assert on which domain objects survived filtering without depending on real UI formatting.
    every { uiAgendaItemAdapter.convertV2(any(), any(), any(), any()) } answers {
      val reminders = firstArg<List<ReminderV2>>()
      val birthdays = thirdArg<List<Birthday>>()
      reminders.map { fakeReminderItem(it.uuId) } + birthdays.map { fakeBirthdayItem(it.uuId) }
    }
    coEvery { resolveWaitingTitlesUseCase(any()) } returns emptyMap()
    every { uiAgendaItemAdapter.findTodayScrollTargetId(any()) } returns null

    viewModel =
      AgendaViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        textProvider = textProvider,
        reminderV2Repository = reminderV2Repository,
        groupV2Repository = groupV2Repository,
        birthdayRepository = birthdayRepository,
        tagRepository = tagRepository,
        tagAssignmentRepository = tagAssignmentRepository,
        uiAgendaItemAdapter = uiAgendaItemAdapter,
        resolveWaitingTitlesUseCase = resolveWaitingTitlesUseCase,
        dateTimeManager = dateTimeManager,
        birthdaySmartListPredicate = birthdaySmartListPredicate,
        moveReminderToArchiveUseCase = moveReminderToArchiveUseCase,
        skipReminderUseCase = skipReminderUseCase,
        toggleReminderStateUseCase = toggleReminderStateUseCase,
        togglePinnedReminderUseCase = togglePinnedReminderUseCase,
        deleteReminderUseCase = deleteReminderUseCase,
        deleteBirthdayUseCase = deleteBirthdayUseCase,
        reminderPreferences = reminderPreferences,
      pendingDeleteCoordinator = pendingDeleteCoordinator,
      )

    // Construction above already triggered one eager load via init{}. Clear recorded
    // invocations (keeping the stubbed answers) so each test's coVerify(exactly = ...)
    // reflects only the calls it makes explicitly through loadMerged().
    clearMocks(reminderV2Repository, birthdayRepository, answers = false, recordedCalls = true)
  }

  @Test
  fun `loads only birthdays and skips reminder repository when only Birthdays category selected`() =
    runTest {
      coEvery { birthdayRepository.getAll() } returns listOf(reminderBirthday("b1"))

      val result = viewModel.loadMerged("", setOf(AgendaCategory.BIRTHDAYS))

      coVerify(exactly = 0) { reminderV2Repository.getByRemovedStatus(any()) }
      assertEquals(listOf("b1"), result.items.map { it.id })
    }

  @Test
  fun `loads only reminders and skips birthday repository when only Reminders category selected`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(reminderV2(id = "r1"))

      val result = viewModel.loadMerged("", setOf(AgendaCategory.REMINDERS))

      coVerify(exactly = 0) { birthdayRepository.getAll() }
      assertEquals(listOf("r1"), result.items.map { it.id })
    }

  @Test
  fun `filters out shopping-type reminders when only Reminders category is selected`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(
          reminderV2(id = "normal"),
          reminderV2(id = "shopping", isShopping = true),
        )

      val result = viewModel.loadMerged("", setOf(AgendaCategory.REMINDERS))

      assertEquals(listOf("normal"), result.items.map { it.id })
    }

  @Test
  fun `filters out normal reminders when only Shopping category is selected`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(
          reminderV2(id = "normal"),
          reminderV2(id = "shopping", isShopping = true),
        )

      val result = viewModel.loadMerged("", setOf(AgendaCategory.SHOPPING))

      assertEquals(listOf("shopping"), result.items.map { it.id })
    }

  @Test
  fun `filters out location-type reminders when only Reminders category is selected`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(
          reminderV2(id = "normal"),
          reminderV2(id = "location", isLocation = true),
        )

      val result = viewModel.loadMerged("", setOf(AgendaCategory.REMINDERS))

      assertEquals(listOf("normal"), result.items.map { it.id })
    }

  @Test
  fun `filters out normal reminders when only Location category is selected`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(
          reminderV2(id = "normal"),
          reminderV2(id = "location", isLocation = true),
        )

      val result = viewModel.loadMerged("", setOf(AgendaCategory.LOCATION))

      assertEquals(listOf("location"), result.items.map { it.id })
    }

  @Test
  fun `loads reminders when only Location category is selected`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(reminderV2(id = "location", isLocation = true))

      val result = viewModel.loadMerged("", setOf(AgendaCategory.LOCATION))

      coVerify(exactly = 0) { birthdayRepository.getAll() }
      assertEquals(listOf("location"), result.items.map { it.id })
    }

  @Test
  fun `search query filters reminders by summary and birthdays by name independently`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(
          reminderV2(id = "match", summary = "Buy milk"),
          reminderV2(id = "no-match", summary = "Call mom"),
        )
      coEvery { birthdayRepository.getAll() } returns
        listOf(
          reminderBirthday(id = "match-bday", name = "Milkman"),
          reminderBirthday(id = "no-match-bday", name = "John"),
        )

      val result = viewModel.loadMerged("milk", AgendaCategory.entries.toSet())

      assertEquals(setOf("match", "match-bday"), result.items.map { it.id }.toSet())
    }

  @Test
  fun `returns empty items when all categories are deselected`() =
    runTest {
      val result = viewModel.loadMerged("", emptySet())

      coVerify(exactly = 0) { reminderV2Repository.getByRemovedStatus(any()) }
      coVerify(exactly = 0) { birthdayRepository.getAll() }
      assertEquals(emptyList<UiAgendaItem>(), result.items)
    }

  @Test
  fun `returns empty items when no reminders or birthdays match the search query`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(reminderV2(id = "r1", summary = "Call mom"))
      coEvery { birthdayRepository.getAll() } returns listOf(reminderBirthday("b1", name = "John"))

      val result = viewModel.loadMerged("nonexistent-query", AgendaCategory.entries.toSet())

      assertEquals(emptyList<UiAgendaItem>(), result.items)
    }

  @Test
  fun `no group smart list keeps only reminders without a group`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(
          reminderV2(id = "grouped", groupId = "group-1"),
          reminderV2(id = "ungrouped", groupId = null),
        )

      val result = viewModel.loadMerged("", setOf(AgendaCategory.REMINDERS), SmartListFilter.NO_GROUP)

      assertEquals(listOf("ungrouped"), result.items.map { it.id })
    }

  @Test
  fun `group filter keeps only reminders in that group and drops birthdays`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(
          reminderV2(id = "in-group", groupId = "group-1"),
          reminderV2(id = "other-group", groupId = "group-2"),
        )
      coEvery { birthdayRepository.getAll() } returns listOf(reminderBirthday("b1"))

      val result =
        viewModel.loadMerged("", AgendaCategory.entries.toSet(), smartList = null, tagId = null, groupId = "group-1")

      assertEquals(listOf("in-group"), result.items.map { it.id })
    }

  @Test
  fun `tag filter keeps only reminders returned by getItemIdsForTag and drops untagged birthdays`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(
          reminderV2(id = "tagged"),
          reminderV2(id = "not-tagged"),
        )
      coEvery { birthdayRepository.getAll() } returns listOf(reminderBirthday("b1"))
      coEvery { tagAssignmentRepository.getItemIdsForTag("tag-1", TaggedItemType.REMINDER) } returns
        listOf("tagged")
      coEvery { tagAssignmentRepository.getItemIdsForTag("tag-1", TaggedItemType.BIRTHDAY) } returns
        emptyList()

      val result =
        viewModel.loadMerged("", AgendaCategory.entries.toSet(), smartList = null, tagId = "tag-1", groupId = null)

      assertEquals(listOf("tagged"), result.items.map { it.id })
    }

  @Test
  fun `tag filter also keeps birthdays returned by getItemIdsForTag for the BIRTHDAY item type`() =
    runTest {
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(reminderV2(id = "untagged-reminder"))
      coEvery { birthdayRepository.getAll() } returns
        listOf(
          reminderBirthday(id = "tagged-birthday"),
          reminderBirthday(id = "untagged-birthday"),
        )
      coEvery { tagAssignmentRepository.getItemIdsForTag("tag-1", TaggedItemType.REMINDER) } returns
        emptyList()
      coEvery { tagAssignmentRepository.getItemIdsForTag("tag-1", TaggedItemType.BIRTHDAY) } returns
        listOf("tagged-birthday")

      val result =
        viewModel.loadMerged("", AgendaCategory.entries.toSet(), smartList = null, tagId = "tag-1", groupId = null)

      assertEquals(listOf("tagged-birthday"), result.items.map { it.id })
    }

  @Test
  fun `loadMerged returns available tags and groups for the filter sheet`() =
    runTest {
      val tag = Tag(id = "tag-1", name = "Work", color = 0)
      coEvery { tagRepository.getAll() } returns listOf(tag)
      coEvery { groupV2Repository.getAll() } returns emptyList()

      val result = viewModel.loadMerged("", AgendaCategory.entries.toSet())

      assertEquals(listOf(tag), result.availableTags)
    }

  @Test
  fun `today smart list keeps only reminders due today`() =
    runTest {
      val now = LocalDateTime.of(2026, 8, 2, 12, 0)
      every { dateTimeManager.getCurrentDateTime() } returns now
      // Reminders' eventDateTime is stored in UTC, so filterReminders() converts "now" to UTC
      // before comparing - an identity conversion here keeps this test focused on the smart-list
      // predicate rather than the timezone math (covered separately).
      every { dateTimeManager.localToUtc(any()) } answers { firstArg() }
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(
          reminderV2(id = "today", eventDateTime = now.plusHours(1)),
          reminderV2(id = "tomorrow", eventDateTime = now.plusDays(1)),
        )

      val result = viewModel.loadMerged("", setOf(AgendaCategory.REMINDERS), SmartListFilter.TODAY)

      assertEquals(listOf("today"), result.items.map { it.id })
    }

  @Test
  fun `overdue smart list accounts for the local-to-UTC offset instead of comparing local time directly`() =
    runTest {
      // Regression test for a bug where filterReminders() compared eventDateTime (always stored
      // in UTC - see UiReminderListAdapter's utcToLocal conversion for the same field) directly
      // against local "now", silently misclassifying reminders in any non-UTC timezone. Here the
      // device is 2 hours ahead of UTC (e.g. Europe/Warsaw in summer): a reminder due in 1 local
      // hour is genuinely in the future and must not show up as overdue.
      val localNow = LocalDateTime.of(2026, 8, 2, 22, 0)
      val utcNow = localNow.minusHours(2)
      every { dateTimeManager.getCurrentDateTime() } returns localNow
      every { dateTimeManager.localToUtc(localNow) } returns utcNow
      coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns
        listOf(
          reminderV2(id = "due-in-one-local-hour", eventDateTime = utcNow.plusHours(1)),
          reminderV2(id = "genuinely-overdue", eventDateTime = utcNow.minusMinutes(1)),
        )

      val result = viewModel.loadMerged("", setOf(AgendaCategory.REMINDERS), SmartListFilter.OVERDUE)

      assertEquals(listOf("genuinely-overdue"), result.items.map { it.id })
    }

  @Test
  fun `today smart list keeps only birthdays occurring today`() =
    runTest {
      val now = LocalDateTime.of(2026, 8, 2, 12, 0)
      every { dateTimeManager.getCurrentDateTime() } returns now
      coEvery { birthdayRepository.getAll() } returns
        listOf(
          reminderBirthday(id = "today", day = 2, month = 8),
          reminderBirthday(id = "later-this-month", day = 10, month = 8),
        )

      val result = viewModel.loadMerged("", setOf(AgendaCategory.BIRTHDAYS), SmartListFilter.TODAY)

      assertEquals(listOf("today"), result.items.map { it.id })
    }

  @Test
  fun `this week smart list keeps only birthdays occurring in the next seven days`() =
    runTest {
      val now = LocalDateTime.of(2026, 8, 2, 12, 0)
      every { dateTimeManager.getCurrentDateTime() } returns now
      coEvery { birthdayRepository.getAll() } returns
        listOf(
          reminderBirthday(id = "this-week", day = 6, month = 8),
          reminderBirthday(id = "next-month", day = 2, month = 9),
        )

      val result = viewModel.loadMerged("", setOf(AgendaCategory.BIRTHDAYS), SmartListFilter.THIS_WEEK)

      assertEquals(listOf("this-week"), result.items.map { it.id })
    }

  @Test
  fun `overdue smart list excludes all birthdays since they always recur into the future`() =
    runTest {
      val now = LocalDateTime.of(2026, 8, 2, 12, 0)
      every { dateTimeManager.getCurrentDateTime() } returns now
      coEvery { birthdayRepository.getAll() } returns listOf(reminderBirthday(id = "b1", day = 1, month = 1))

      val result = viewModel.loadMerged("", setOf(AgendaCategory.BIRTHDAYS), SmartListFilter.OVERDUE)

      assertEquals(emptyList<String>(), result.items.map { it.id })
    }

  @Test
  fun `togglePinned toggles the pinned state of the fetched reminder and refreshes`() =
    runTest {
      val reminder = reminderV2(id = "r1")
      coEvery { reminderV2Repository.getById("r1") } returns reminder

      viewModel.togglePinned("r1")

      coVerify(exactly = 1) { togglePinnedReminderUseCase(reminder) }
    }

  @Test
  fun `togglePinned does nothing when the reminder no longer exists`() =
    runTest {
      coEvery { reminderV2Repository.getById("missing") } returns null

      viewModel.togglePinned("missing")

      coVerify(exactly = 0) { togglePinnedReminderUseCase(any()) }
    }

  @Test
  fun `hasScrolledToToday starts false and flips to true after onScrolledToToday`() {
    assertEquals(false, viewModel.hasScrolledToToday)

    viewModel.onScrolledToToday()

    assertEquals(true, viewModel.hasScrolledToToday)
  }

  private fun readyViewModelWithReminderAndBirthday(): AgendaViewModel {
    coEvery { reminderV2Repository.getByRemovedStatus(removed = false) } returns listOf(reminderV2(id = "r1"))
    coEvery { birthdayRepository.getAll() } returns listOf(reminderBirthday(id = "b1"))
    return AgendaViewModel(
      dispatcherProvider = mockDispatcherProvider(),
      textProvider = textProvider,
      reminderV2Repository = reminderV2Repository,
      groupV2Repository = groupV2Repository,
      birthdayRepository = birthdayRepository,
      tagRepository = tagRepository,
      tagAssignmentRepository = tagAssignmentRepository,
      uiAgendaItemAdapter = uiAgendaItemAdapter,
      resolveWaitingTitlesUseCase = resolveWaitingTitlesUseCase,
      dateTimeManager = dateTimeManager,
      birthdaySmartListPredicate = birthdaySmartListPredicate,
      moveReminderToArchiveUseCase = moveReminderToArchiveUseCase,
      skipReminderUseCase = skipReminderUseCase,
      toggleReminderStateUseCase = toggleReminderStateUseCase,
      togglePinnedReminderUseCase = togglePinnedReminderUseCase,
      deleteReminderUseCase = deleteReminderUseCase,
      deleteBirthdayUseCase = deleteBirthdayUseCase,
      reminderPreferences = reminderPreferences,
      pendingDeleteCoordinator = pendingDeleteCoordinator,
    )
  }

  @Test
  fun `onItemLongClick selects the item and enters selection mode`() =
    runTest {
      val vm = readyViewModelWithReminderAndBirthday()

      vm.onItemLongClick("r1")

      val state = vm.agendaScreenState.first()
      assertEquals(1, state.selectedCount)
    }

  @Test
  fun `onItemClick toggles selection while in selection mode instead of opening a preview`() =
    runTest {
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onItemLongClick("r1")
      val ready = vm.agendaScreenState.first().listState as ListState.Ready
      val birthdayItem = ready.items.first { it.id == "b1" } as UiAgendaBirthday

      vm.onItemClick(birthdayItem)

      assertEquals(2, vm.agendaScreenState.first().selectedCount)
      assertEquals(null, vm.navigationEvent.value)
    }

  @Test
  fun `onItemClick exits selection mode automatically after the last item is deselected`() =
    runTest {
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onItemLongClick("r1")
      val reminderItem = (vm.agendaScreenState.first().listState as ListState.Ready)
        .items.first { it.id == "r1" } as UiAgendaReminder

      vm.onItemClick(reminderItem)

      assertEquals(0, vm.agendaScreenState.first().selectedCount)
    }

  @Test
  fun `onSelectionCancel clears the selection`() =
    runTest {
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onItemLongClick("r1")

      vm.onSelectionCancel()

      assertEquals(0, vm.agendaScreenState.first().selectedCount)
    }

  @Test
  fun `onDeleteSelectedClick does nothing when nothing is selected`() =
    runTest {
      val vm = readyViewModelWithReminderAndBirthday()

      vm.onDeleteSelectedClick()

      assertEquals(null, vm.navigationEvent.value)
    }

  @Test
  fun `hides a birthday pending delete in the coordinator`() =
    runTest {
      val vm = readyViewModelWithReminderAndBirthday()
      coordinatorBirthdayPendingIds.value = setOf("b1")

      val state = vm.agendaScreenState.first { (it.listState as? ListState.Ready)?.items?.map { item -> item.id } == listOf("r1") }

      assertEquals(listOf("r1"), (state.listState as ListState.Ready).items.map { it.id })
    }

  @Test
  fun `onDeleteSelectedClick hides selected items and clears selection without deleting yet`() =
    runTest {
      every { textProvider.getText(R.string.agenda_items_deleted_count, 2) } returns "2 items deleted"
      val vm = readyViewModelWithReminderAndBirthday()
      selectReminderAndBirthday(vm)

      vm.onDeleteSelectedClick()

      val state = vm.agendaScreenState.first()
      assertEquals(ListState.Empty, state.listState)
      assertEquals(0, state.selectedCount)
      val event = vm.undoDelete.undoEvents.first()
      assertEquals("2 items deleted", event.message)
      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
      coVerify(exactly = 0) { deleteBirthdayUseCase(any()) }
    }

  @Test
  fun `undoDelete undo after bulk delete restores the items and never runs the use cases`() =
    runTest {
      val vm = readyViewModelWithReminderAndBirthday()
      selectReminderAndBirthday(vm)
      vm.onDeleteSelectedClick()

      vm.undoDelete.undo(lastBatchKey(vm))

      val items = (vm.agendaScreenState.first().listState as ListState.Ready).items
      assertEquals(setOf("r1", "b1"), items.map { it.id }.toSet())
      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
      coVerify(exactly = 0) { deleteBirthdayUseCase(any()) }
    }

  @Test
  fun `undoDelete commit after bulk delete deletes reminders and birthdays once`() =
    runTest {
      val reminder = reminderV2(id = "r1")
      coEvery { reminderV2Repository.getById("r1") } returns reminder
      coEvery { deleteReminderUseCase(reminder) } just Runs
      coEvery { deleteBirthdayUseCase("b1") } just Runs
      val vm = readyViewModelWithReminderAndBirthday()
      selectReminderAndBirthday(vm)
      vm.onDeleteSelectedClick()
      val batchKey = lastBatchKey(vm)

      vm.undoDelete.commit(batchKey)
      vm.undoDelete.commit(batchKey)

      coVerify(exactly = 1) { deleteReminderUseCase(reminder) }
      coVerify(exactly = 1) { deleteBirthdayUseCase("b1") }
    }

  @Test
  fun `onArchiveSelectedClick does nothing when only birthdays are selected`() =
    runTest {
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onItemLongClick("b1")

      vm.onArchiveSelectedClick()

      assertEquals(null, vm.navigationEvent.value)
    }

  @Test
  fun `onArchiveSelectedClick hides selected reminders and clears selection without archiving yet`() =
    runTest {
      every { textProvider.getText(R.string.reminders_archived_count, 1) } returns "1 reminders archived"
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onItemLongClick("r1")

      vm.onArchiveSelectedClick()

      val state = vm.agendaScreenState.first()
      assertEquals(listOf("b1"), (state.listState as ListState.Ready).items.map { it.id })
      assertEquals(0, state.selectedCount)
      val event = vm.undoDelete.undoEvents.first()
      assertEquals("1 reminders archived", event.message)
      coVerify(exactly = 0) { moveReminderToArchiveUseCase(any()) }
    }

  @Test
  fun `undoDelete commit after bulk archive archives each reminder once`() =
    runTest {
      coEvery { moveReminderToArchiveUseCase("r1") } just Runs
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onItemLongClick("r1")
      vm.onArchiveSelectedClick()
      val batchKey = lastBatchKey(vm)

      vm.undoDelete.commit(batchKey)
      vm.undoDelete.commit(batchKey)

      coVerify(exactly = 1) { moveReminderToArchiveUseCase("r1") }
    }

  @Test
  fun `delete menu action on a reminder hides it immediately and posts undo without deleting yet`() =
    runTest {
      every { textProvider.getText(R.string.reminder_deleted) } returns "Reminder deleted"
      val vm = readyViewModelWithReminderAndBirthday()

      vm.onAgendaMenuAction(fakeReminderItem("r1"), AgendaMenuAction.DELETE)

      val items = (vm.agendaScreenState.first().listState as ListState.Ready).items
      assertEquals(listOf("b1"), items.map { it.id })
      assertEquals(UndoEvent(batchKey = "r1", message = "Reminder deleted", token = 1L), vm.undoDelete.undoEvents.first())
      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
    }

  @Test
  fun `undoDelete undo after reminder delete restores it and double undo is a no-op`() =
    runTest {
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onAgendaMenuAction(fakeReminderItem("r1"), AgendaMenuAction.DELETE)

      vm.undoDelete.undo("r1")
      vm.undoDelete.undo("r1")

      val items = (vm.agendaScreenState.first().listState as ListState.Ready).items
      assertEquals(setOf("r1", "b1"), items.map { it.id }.toSet())
      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
    }

  @Test
  fun `undoDelete commit after reminder delete runs the use case once and ignores undo afterwards`() =
    runTest {
      val reminder = reminderV2(id = "r1")
      coEvery { reminderV2Repository.getById("r1") } returns reminder
      coEvery { deleteReminderUseCase(reminder) } just Runs
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onAgendaMenuAction(fakeReminderItem("r1"), AgendaMenuAction.DELETE)

      vm.undoDelete.commit("r1")
      vm.undoDelete.commit("r1")
      vm.undoDelete.undo("r1")

      coVerify(exactly = 1) { deleteReminderUseCase(reminder) }
    }

  @Test
  fun `archive menu action on a reminder hides it and archives only after commit`() =
    runTest {
      every { textProvider.getText(R.string.reminder_archived) } returns "Reminder archived"
      coEvery { moveReminderToArchiveUseCase("r1") } just Runs
      val vm = readyViewModelWithReminderAndBirthday()

      vm.onAgendaMenuAction(fakeReminderItem("r1"), AgendaMenuAction.ARCHIVE)

      assertEquals(listOf("b1"), (vm.agendaScreenState.first().listState as ListState.Ready).items.map { it.id })
      assertEquals(UndoEvent(batchKey = "r1", message = "Reminder archived", token = 1L), vm.undoDelete.undoEvents.first())
      coVerify(exactly = 0) { moveReminderToArchiveUseCase(any()) }

      vm.undoDelete.commit("r1")

      coVerify(exactly = 1) { moveReminderToArchiveUseCase("r1") }
    }

  @Test
  fun `delete menu action on a birthday defers the use case and undo skips it`() =
    runTest {
      every { textProvider.getText(R.string.birthday_deleted) } returns "Birthday deleted"
      coEvery { deleteBirthdayUseCase("b1") } just Runs
      val vm = readyViewModelWithReminderAndBirthday()

      vm.onAgendaMenuAction(fakeBirthdayItem("b1"), AgendaMenuAction.DELETE)

      assertEquals(listOf("r1"), (vm.agendaScreenState.first().listState as ListState.Ready).items.map { it.id })
      coVerify(exactly = 0) { deleteBirthdayUseCase(any()) }

      vm.undoDelete.undo("b1")
      vm.undoDelete.commit("b1")

      assertEquals(
        setOf("r1", "b1"),
        (vm.agendaScreenState.first().listState as ListState.Ready).items.map { it.id }.toSet(),
      )
      coVerify(exactly = 0) { deleteBirthdayUseCase(any()) }
    }

  @Test
  fun `undoDelete commit after birthday delete runs the use case once`() =
    runTest {
      coEvery { deleteBirthdayUseCase("b1") } just Runs
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onAgendaMenuAction(fakeBirthdayItem("b1"), AgendaMenuAction.DELETE)

      vm.undoDelete.commit("b1")
      vm.undoDelete.commit("b1")

      coVerify(exactly = 1) { deleteBirthdayUseCase("b1") }
    }

  @Test
  fun `bulk delete of reminders only uses the reminders deleted message`() =
    runTest {
      every { textProvider.getText(R.string.reminders_deleted_count, 1) } returns "1 reminders deleted"
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onItemLongClick("r1")

      vm.onDeleteSelectedClick()

      val event = vm.undoDelete.undoEvents.first()
      assertEquals("1 reminders deleted", event.message)
    }

  @Test
  fun `bulk delete of birthdays only uses the birthdays deleted message`() =
    runTest {
      every { textProvider.getText(R.string.birthdays_deleted_count, 1) } returns "1 birthdays deleted"
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onItemLongClick("b1")

      vm.onDeleteSelectedClick()

      val event = vm.undoDelete.undoEvents.first()
      assertEquals("1 birthdays deleted", event.message)
    }

  @Test
  fun `undoDelete undo after bulk archive restores the reminder and never archives it`() =
    runTest {
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onItemLongClick("r1")
      vm.onArchiveSelectedClick()

      vm.undoDelete.undo(lastBatchKey(vm))

      val items = (vm.agendaScreenState.first().listState as ListState.Ready).items
      assertEquals(setOf("r1", "b1"), items.map { it.id }.toSet())
      coVerify(exactly = 0) { moveReminderToArchiveUseCase(any()) }
    }

  @Test
  fun `undoDelete undo after reminder archive restores it and never archives it`() =
    runTest {
      every { textProvider.getText(R.string.reminder_archived) } returns "Reminder archived"
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onAgendaMenuAction(fakeReminderItem("r1"), AgendaMenuAction.ARCHIVE)

      vm.undoDelete.undo("r1")
      vm.undoDelete.commit("r1")

      val items = (vm.agendaScreenState.first().listState as ListState.Ready).items
      assertEquals(setOf("r1", "b1"), items.map { it.id }.toSet())
      coVerify(exactly = 0) { moveReminderToArchiveUseCase(any()) }
    }

  @Test
  fun `onCleared commits pending deletes once so leaving the screen does not lose them`() =
    runTest {
      val reminder = reminderV2(id = "r1")
      coEvery { reminderV2Repository.getById("r1") } returns reminder
      coEvery { deleteReminderUseCase(reminder) } just Runs
      coEvery { deleteBirthdayUseCase("b1") } just Runs
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onAgendaMenuAction(fakeReminderItem("r1"), AgendaMenuAction.DELETE)
      vm.onAgendaMenuAction(fakeBirthdayItem("b1"), AgendaMenuAction.DELETE)

      vm.onCleared()
      vm.undoDelete.commit("r1")
      vm.onCleared()

      coVerify(exactly = 1) { deleteReminderUseCase(reminder) }
      coVerify(exactly = 1) { deleteBirthdayUseCase("b1") }
    }

  @Test
  fun `onCleared does not run pending deletes that were undone`() =
    runTest {
      val vm = readyViewModelWithReminderAndBirthday()
      vm.onAgendaMenuAction(fakeReminderItem("r1"), AgendaMenuAction.DELETE)
      vm.undoDelete.undo("r1")

      vm.onCleared()

      coVerify(exactly = 0) { deleteReminderUseCase(any()) }
    }

  private suspend fun selectReminderAndBirthday(vm: AgendaViewModel) {
    vm.onItemLongClick("r1")
    val birthdayItem = (vm.agendaScreenState.first().listState as ListState.Ready)
      .items.first { it.id == "b1" } as UiAgendaBirthday
    vm.onItemClick(birthdayItem)
  }

  private suspend fun lastBatchKey(vm: AgendaViewModel): String =
    vm.undoDelete.undoEvents.first().batchKey

  private fun reminderV2(
    id: String,
    summary: String = "",
    isShopping: Boolean = false,
    isLocation: Boolean = false,
    groupId: String? = null,
    eventDateTime: LocalDateTime? = null,
  ) = ReminderV2(
    uuId = id,
    summary = summary,
    groupId = groupId,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now(), eventDateTime = eventDateTime),
    action = if (isShopping) ReminderAction.Shopping else ReminderAction.None,
    location = if (isLocation) LocationSettings() else null,
  )

  // Birthday.month is stored 0-based (Calendar.MONTH style, e.g. August == 7) everywhere it's
  // written - see EditBirthdayViewModel/CheckBirthdaysTask - so this 1-based param is converted.
  private fun reminderBirthday(
    id: String,
    name: String = "",
    day: Int = 1,
    month: Int = 1,
  ) = Birthday(uuId = id, name = name, day = day, month = month - 1, syncState = SyncState.Synced)

  private fun fakeReminderItem(id: String): UiAgendaReminder =
    UiAgendaReminder(
      id = id,
      dateTime = LocalDateTime.now(),
      category = AgendaCategory.REMINDERS,
      mainText = UiTextElement(id, UiTextFormat(fontSize = 14f)),
      secondaryText = null,
      tertiaryText = null,
      tags = emptyList(),
      actions = UiReminderListActions(),
      state = UiReminderListState(),
    )

  private fun fakeBirthdayItem(id: String): UiAgendaBirthday =
    UiAgendaBirthday(
      id = id,
      dateTime = LocalDateTime.now(),
      name = id,
      ageFormatted = "",
      remainingTimeFormatted = null,
      color = 0,
      contrastColor = 0,
      dateFormatted = "",
    )
}
