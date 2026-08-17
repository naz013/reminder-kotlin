package com.github.naz013.feature.agenda

import com.github.naz013.logic.birthday.BirthdaySmartListPredicate
import com.github.naz013.logic.birthday.DeleteBirthdayUseCase
import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.common.text.UiTextFormat
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListState
import com.github.naz013.logic.reminder.usecase.SkipReminderUseCase
import com.github.naz013.logic.reminder.usecase.ToggleReminderStateUseCase
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
import com.github.naz013.logic.reminder.smartlist.SmartListFilter
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.agenda.AgendaCategory
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.ui.agenda.UiAgendaItemAdapter
import com.github.naz013.ui.agenda.UiAgendaReminder
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
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
class AgendaViewModelTest {
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val tagRepository = mockk<TagRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val uiAgendaItemAdapter = mockk<UiAgendaItemAdapter>()
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val moveReminderToArchiveUseCase = mockk<MoveReminderToArchiveUseCase>()
  private val skipReminderUseCase = mockk<SkipReminderUseCase>()
  private val toggleReminderStateUseCase = mockk<ToggleReminderStateUseCase>()
  private val deleteReminderUseCase = mockk<DeleteReminderUseCase>()
  private val deleteBirthdayUseCase = mockk<DeleteBirthdayUseCase>()
  private val birthdaySmartListPredicate = BirthdaySmartListPredicate(provideBirthdayDateCalculator())

  private lateinit var viewModel: AgendaViewModel

  @Before
  fun setUp() {
    coEvery { groupV2Repository.getAll() } returns emptyList()
    // AgendaViewModel's init{} eagerly runs the load pipeline once on construction, and
    // mockDispatcherProvider() uses Dispatchers.Unconfined, so that eager call executes
    // synchronously here in setUp() before any test body runs. Stub safe defaults so it
    // doesn't crash on unmocked calls; individual tests override these as needed.
    coEvery { reminderV2Repository.getByRemovedStatus(any()) } returns emptyList()
    coEvery { birthdayRepository.getAll() } returns emptyList()
    coEvery { tagRepository.getAll() } returns emptyList()
    coEvery { tagAssignmentRepository.getItemIdsForTag(any(), any()) } returns emptyList()
    // Echoes the filtered reminders/birthdays back as bare UiAgendaItems keyed by id, so tests can
    // assert on which domain objects survived filtering without depending on real UI formatting.
    every { uiAgendaItemAdapter.convertV2(any(), any(), any()) } answers {
      val reminders = firstArg<List<ReminderV2>>()
      val birthdays = thirdArg<List<Birthday>>()
      reminders.map { fakeReminderItem(it.uuId) } + birthdays.map { fakeBirthdayItem(it.uuId) }
    }

    viewModel =
      AgendaViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        reminderV2Repository = reminderV2Repository,
        groupV2Repository = groupV2Repository,
        birthdayRepository = birthdayRepository,
        tagRepository = tagRepository,
        tagAssignmentRepository = tagAssignmentRepository,
        uiAgendaItemAdapter = uiAgendaItemAdapter,
        dateTimeManager = dateTimeManager,
        birthdaySmartListPredicate = birthdaySmartListPredicate,
        moveReminderToArchiveUseCase = moveReminderToArchiveUseCase,
        skipReminderUseCase = skipReminderUseCase,
        toggleReminderStateUseCase = toggleReminderStateUseCase,
        deleteReminderUseCase = deleteReminderUseCase,
        deleteBirthdayUseCase = deleteBirthdayUseCase,
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

  private fun reminderBirthday(
    id: String,
    name: String = "",
    day: Int = 1,
    month: Int = 1,
  ) = Birthday(uuId = id, name = name, day = day, month = month, syncState = SyncState.Synced)

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
