package com.github.naz013.feature.home.scheduleview

import android.content.Context
import com.github.naz013.testing.BaseTest
import com.github.naz013.feature.home.HomeEvent
import com.github.naz013.feature.home.HomePreferences
import com.github.naz013.feature.home.ResolvedEventAction
import com.github.naz013.ui.reminder.ShopItemsFormatter
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.theme.ThemeProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class GetActiveEventsForTheDayUseCaseTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val birthdayRepository = mockk<com.github.naz013.repository.BirthdayRepository>(relaxed = true)
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val modelDateTimeFormatter = mockk<ModelDateTimeFormatter>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val shopItemsFormatter = mockk<ShopItemsFormatter>(relaxed = true)
  private val contextProvider = mockk<ContextProvider>()
  private val homePreferences = mockk<HomePreferences>(relaxed = true)

  private lateinit var useCase: GetActiveEventsForTheDayUseCase

  private val day = LocalDateTime.of(2026, 7, 24, 9, 0)

  private fun reminder(
    id: String = "1",
    summary: String = "Test reminder",
    groupId: String? = null,
    action: ReminderAction = ReminderAction.None,
    shoppingItems: List<ShopItemV2> = emptyList(),
    eventDateTime: LocalDateTime? = day.plusHours(1),
  ) = ReminderV2(
    uuId = id,
    summary = summary,
    groupId = groupId,
    schedule = ReminderSchedule(startDateTime = day, eventDateTime = eventDateTime),
    action = action,
    shoppingItems = shoppingItems,
  )

  private fun birthday(
    id: String = "b1",
    name: String = "Test birthday",
    showedYear: Int = 0,
  ) = Birthday(
    uuId = id,
    name = name,
    showedYear = showedYear,
    syncState = SyncState.Synced,
  )

  @Before
  override fun setUp() {
    super.setUp()
    mockkObject(ThemeProvider.Companion)
    every { ThemeProvider.themedColor(any(), any()) } returns 0
    every { ThemeProvider.colorBirthdayCalendar(any(), any()) } returns 0
    every { contextProvider.themedContext } returns mockk<Context>(relaxed = true)
    every { contextProvider.context } returns mockk<Context>(relaxed = true)
    every { dateTimeManager.getCurrentDateTime() } returns day
    every { modelDateTimeFormatter.getRemaining(any<LocalDateTime>(), any<LocalDateTime>()) } returns "remaining"
    every {
      modelDateTimeFormatter.getFutureBirthdayDate(any(), any(), any(), any())
    } returns day
    every { reminderV2Repository.observeActiveInRange(any(), any(), any()) } returns flowOf(emptyList())
    coEvery { groupV2Repository.getAll() } returns emptyList()
    every { birthdayRepository.observeAll(any()) } returns flowOf(emptyList())

    useCase = GetActiveEventsForTheDayUseCase(
      dateTimeManager = dateTimeManager,
      reminderV2Repository = reminderV2Repository,
      birthdayRepository = birthdayRepository,
      groupV2Repository = groupV2Repository,
      modelDateTimeFormatter = modelDateTimeFormatter,
      textProvider = textProvider,
      shopItemsFormatter = shopItemsFormatter,
      contextProvider = contextProvider,
      homePreferences = homePreferences,
    )
  }

  @After
  override fun tearDown() {
    super.tearDown()
    unmockkObject(ThemeProvider.Companion)
  }

  @Test
  fun `maps a reminder into a HomeEvent with its group name and color`() = runTest {
    val group = GroupV2(uuId = "g1", title = "Work", color = 3)
    coEvery { groupV2Repository.getAll() } returns listOf(group)
    every { reminderV2Repository.observeActiveInRange(any(), any(), any()) } returns
      flowOf(listOf(reminder(groupId = "g1")))

    val events = useCase(day).first()

    val event = events.single()
    assertEquals("Test reminder", event.text)
    assertEquals("Work", event.groupName)
  }

  @Test
  fun `skips a reminder with no event date time`() = runTest {
    every { reminderV2Repository.observeActiveInRange(any(), any(), any()) } returns
      flowOf(listOf(reminder(eventDateTime = null)))

    val events = useCase(day).first()

    assertEquals(emptyList<Any>(), events)
  }

  @Test
  fun `maps a Call action into a MakeCall event action`() = runTest {
    every { reminderV2Repository.observeActiveInRange(any(), any(), any()) } returns
      flowOf(listOf(reminder(action = ReminderAction.Call("+123"))))

    val event = useCase(day).first().single()

    val action = event.action?.value as ResolvedEventAction.MakeCall
    assertEquals("+123", action.phoneNumber)
  }

  @Test
  fun `has no event action for a reminder with no action`() = runTest {
    every { reminderV2Repository.observeActiveInRange(any(), any(), any()) } returns
      flowOf(listOf(reminder(action = ReminderAction.None)))

    val event = useCase(day).first().single()

    assertNull(event.action)
  }

  @Test
  fun `formats the shopping list as the secondary text for a Shopping action`() = runTest {
    every { shopItemsFormatter.formatV2(any()) } returns "formatted list"
    val items = listOf(ShopItemV2(summary = "Milk", createdAt = day))
    every { reminderV2Repository.observeActiveInRange(any(), any(), any()) } returns
      flowOf(listOf(reminder(action = ReminderAction.Shopping, shoppingItems = items)))

    val event = useCase(day).first().single()

    assertEquals("formatted list", event.description)
  }

  @Test
  fun `maps a birthday into a HomeEvent`() = runTest {
    every { birthdayRepository.observeAll(any()) } returns flowOf(listOf(birthday(name = "Jane")))

    val event = useCase(day).first().single()

    assertEquals("Jane", event.text)
    assertEquals(HomeEvent.EventType.Birthday, event.type)
  }

  @Test
  fun `excludes a birthday already shown this year`() = runTest {
    every { birthdayRepository.observeAll(any()) } returns
      flowOf(listOf(birthday(showedYear = day.year)))

    val events = useCase(day).first()

    assertTrue(events.isEmpty())
  }

  @Test
  fun `includes a birthday shown in a previous year`() = runTest {
    every { birthdayRepository.observeAll(any()) } returns
      flowOf(listOf(birthday(showedYear = day.year - 1)))

    val events = useCase(day).first()

    assertEquals(1, events.size)
  }
}
