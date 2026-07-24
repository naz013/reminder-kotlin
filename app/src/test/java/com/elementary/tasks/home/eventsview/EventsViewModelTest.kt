package com.elementary.tasks.home.eventsview

import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.reminder.lists.data.UiReminderListActions
import com.elementary.tasks.reminder.lists.data.UiReminderListState
import com.elementary.tasks.reminder.scheduling.usecase.SkipReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ToggleReminderStateUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.usecase.reminders.GetRemindersV2ByRemovedStatusUseCase
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
 * Unit tests for [EventsViewModel]'s merge/sort/filter pipeline ([EventsViewModel.loadMerged]).
 * [UiEventItemAdapter] is mocked so these tests can focus purely on which reminders/birthdays make
 * it through category and search-query filtering, rather than on presentation formatting (covered
 * separately by [UiEventItemAdapterTest]).
 */
class EventsViewModelTest {
  private val reminderRepository = mockk<ReminderRepository>()
  private val getRemindersV2ByRemovedStatusUseCase = mockk<GetRemindersV2ByRemovedStatusUseCase>()
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val uiEventItemAdapter = mockk<UiEventItemAdapter>()
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val moveReminderToArchiveUseCase = mockk<MoveReminderToArchiveUseCase>()
  private val skipReminderUseCase = mockk<SkipReminderUseCase>()
  private val toggleReminderStateUseCase = mockk<ToggleReminderStateUseCase>()
  private val deleteReminderUseCase = mockk<DeleteReminderUseCase>()
  private val deleteBirthdayUseCase = mockk<DeleteBirthdayUseCase>()

  private lateinit var viewModel: EventsViewModel

  @Before
  fun setUp() {
    coEvery { groupV2Repository.getAll() } returns emptyList()
    // EventsViewModel's init{} eagerly runs the load pipeline once on construction, and
    // mockDispatcherProvider() uses Dispatchers.Unconfined, so that eager call executes
    // synchronously here in setUp() before any test body runs. Stub safe defaults so it
    // doesn't crash on unmocked calls; individual tests override these as needed.
    coEvery { getRemindersV2ByRemovedStatusUseCase(any()) } returns emptyList()
    coEvery { birthdayRepository.getAll() } returns emptyList()
    // Echoes the filtered reminders/birthdays back as bare UiEventItems keyed by id, so tests can
    // assert on which domain objects survived filtering without depending on real UI formatting.
    every { uiEventItemAdapter.convertV2(any(), any(), any()) } answers {
      val reminders = firstArg<List<ReminderV2>>()
      val birthdays = thirdArg<List<Birthday>>()
      reminders.map { fakeReminderItem(it.uuId) } + birthdays.map { fakeBirthdayItem(it.uuId) }
    }

    viewModel =
      EventsViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        reminderRepository = reminderRepository,
        getRemindersV2ByRemovedStatusUseCase = getRemindersV2ByRemovedStatusUseCase,
        groupV2Repository = groupV2Repository,
        birthdayRepository = birthdayRepository,
        uiEventItemAdapter = uiEventItemAdapter,
        textProvider = textProvider,
        moveReminderToArchiveUseCase = moveReminderToArchiveUseCase,
        skipReminderUseCase = skipReminderUseCase,
        toggleReminderStateUseCase = toggleReminderStateUseCase,
        deleteReminderUseCase = deleteReminderUseCase,
        deleteBirthdayUseCase = deleteBirthdayUseCase,
      )

    // Construction above already triggered one eager load via init{}. Clear recorded
    // invocations (keeping the stubbed answers) so each test's coVerify(exactly = ...)
    // reflects only the calls it makes explicitly through loadMerged().
    clearMocks(getRemindersV2ByRemovedStatusUseCase, birthdayRepository, answers = false, recordedCalls = true)
  }

  @Test
  fun `loads only birthdays and skips reminder repository when only Birthdays category selected`() =
    runTest {
      coEvery { birthdayRepository.getAll() } returns listOf(reminderBirthday("b1"))

      val result = viewModel.loadMerged("", setOf(EventCategory.BIRTHDAYS))

      coVerify(exactly = 0) { getRemindersV2ByRemovedStatusUseCase(any()) }
      assertEquals(listOf("b1"), result.items.map { it.id })
    }

  @Test
  fun `loads only reminders and skips birthday repository when only Reminders category selected`() =
    runTest {
      coEvery { getRemindersV2ByRemovedStatusUseCase(removed = false) } returns
        listOf(reminderV2(id = "r1"))

      val result = viewModel.loadMerged("", setOf(EventCategory.REMINDERS))

      coVerify(exactly = 0) { birthdayRepository.getAll() }
      assertEquals(listOf("r1"), result.items.map { it.id })
    }

  @Test
  fun `filters out shopping-type reminders when only Reminders category is selected`() =
    runTest {
      coEvery { getRemindersV2ByRemovedStatusUseCase(removed = false) } returns
        listOf(
          reminderV2(id = "normal"),
          reminderV2(id = "shopping", isShopping = true),
        )

      val result = viewModel.loadMerged("", setOf(EventCategory.REMINDERS))

      assertEquals(listOf("normal"), result.items.map { it.id })
    }

  @Test
  fun `filters out normal reminders when only Shopping category is selected`() =
    runTest {
      coEvery { getRemindersV2ByRemovedStatusUseCase(removed = false) } returns
        listOf(
          reminderV2(id = "normal"),
          reminderV2(id = "shopping", isShopping = true),
        )

      val result = viewModel.loadMerged("", setOf(EventCategory.SHOPPING))

      assertEquals(listOf("shopping"), result.items.map { it.id })
    }

  @Test
  fun `search query filters reminders by summary and birthdays by name independently`() =
    runTest {
      coEvery { getRemindersV2ByRemovedStatusUseCase(removed = false) } returns
        listOf(
          reminderV2(id = "match", summary = "Buy milk"),
          reminderV2(id = "no-match", summary = "Call mom"),
        )
      coEvery { birthdayRepository.getAll() } returns
        listOf(
          reminderBirthday(id = "match-bday", name = "Milkman"),
          reminderBirthday(id = "no-match-bday", name = "John"),
        )

      val result = viewModel.loadMerged("milk", EventCategory.entries.toSet())

      assertEquals(setOf("match", "match-bday"), result.items.map { it.id }.toSet())
    }

  @Test
  fun `returns empty items when all categories are deselected`() =
    runTest {
      val result = viewModel.loadMerged("", emptySet())

      coVerify(exactly = 0) { getRemindersV2ByRemovedStatusUseCase(any()) }
      coVerify(exactly = 0) { birthdayRepository.getAll() }
      assertEquals(emptyList<UiEventItem>(), result.items)
    }

  @Test
  fun `returns empty items when no reminders or birthdays match the search query`() =
    runTest {
      coEvery { getRemindersV2ByRemovedStatusUseCase(removed = false) } returns
        listOf(reminderV2(id = "r1", summary = "Call mom"))
      coEvery { birthdayRepository.getAll() } returns listOf(reminderBirthday("b1", name = "John"))

      val result = viewModel.loadMerged("nonexistent-query", EventCategory.entries.toSet())

      assertEquals(emptyList<UiEventItem>(), result.items)
    }

  private fun reminderV2(
    id: String,
    summary: String = "",
    isShopping: Boolean = false,
    groupId: String? = null,
  ) = ReminderV2(
    uuId = id,
    summary = summary,
    groupId = groupId,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    action = if (isShopping) ReminderAction.Shopping else ReminderAction.None,
  )

  private fun reminderBirthday(
    id: String,
    name: String = "",
  ) = Birthday(uuId = id, name = name, syncState = SyncState.Synced)

  private fun fakeReminderItem(id: String): UiEventReminder =
    UiEventReminder(
      id = id,
      dateTime = LocalDateTime.now(),
      category = EventCategory.REMINDERS,
      mainText = UiTextElement(id, UiTextFormat(fontSize = 14f)),
      secondaryText = null,
      tertiaryText = null,
      tags = emptyList(),
      actions = UiReminderListActions(),
      state = UiReminderListState(),
    )

  private fun fakeBirthdayItem(id: String): UiEventBirthday =
    UiEventBirthday(
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
