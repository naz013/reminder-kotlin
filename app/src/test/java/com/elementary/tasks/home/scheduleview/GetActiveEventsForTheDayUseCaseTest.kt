package com.elementary.tasks.home.scheduleview

import android.content.Context
import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.eventaction.ResolvedEventAction
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.reminder.build.formatter.`object`.ShopItemsFormatter
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.usecase.reminders.GetRemindersV2InRangeUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class GetActiveEventsForTheDayUseCaseTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val getRemindersV2InRangeUseCase = mockk<GetRemindersV2InRangeUseCase>()
  private val birthdayRepository = mockk<com.github.naz013.repository.BirthdayRepository>(relaxed = true)
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val modelDateTimeFormatter = mockk<ModelDateTimeFormatter>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val shopItemsFormatter = mockk<ShopItemsFormatter>(relaxed = true)
  private val contextProvider = mockk<ContextProvider>()
  private val prefs = mockk<Prefs>(relaxed = true)

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

  @Before
  override fun setUp() {
    super.setUp()
    mockkObject(ThemeProvider.Companion)
    every { ThemeProvider.themedColor(any(), any()) } returns 0
    every { contextProvider.themedContext } returns mockk<Context>(relaxed = true)
    every { dateTimeManager.getCurrentDateTime() } returns day
    every { modelDateTimeFormatter.getRemaining(any<LocalDateTime>(), any<LocalDateTime>()) } returns "remaining"
    coEvery { getRemindersV2InRangeUseCase(any(), any()) } returns emptyList()
    coEvery { groupV2Repository.getAll() } returns emptyList()
    coEvery { birthdayRepository.getAll(any()) } returns emptyList()

    useCase = GetActiveEventsForTheDayUseCase(
      dispatcherProvider = mockDispatcherProvider(),
      dateTimeManager = dateTimeManager,
      getRemindersV2InRangeUseCase = getRemindersV2InRangeUseCase,
      birthdayRepository = birthdayRepository,
      groupV2Repository = groupV2Repository,
      modelDateTimeFormatter = modelDateTimeFormatter,
      textProvider = textProvider,
      shopItemsFormatter = shopItemsFormatter,
      contextProvider = contextProvider,
      prefs = prefs,
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
    coEvery { getRemindersV2InRangeUseCase(any(), any()) } returns listOf(reminder(groupId = "g1"))

    val events = useCase(this, day)

    val event = events.single()
    assertEquals("Test reminder", event.text)
    assertEquals("Work", event.groupName)
  }

  @Test
  fun `skips a reminder with no event date time`() = runTest {
    coEvery { getRemindersV2InRangeUseCase(any(), any()) } returns listOf(reminder(eventDateTime = null))

    val events = useCase(this, day)

    assertEquals(emptyList<Any>(), events)
  }

  @Test
  fun `maps a Call action into a MakeCall event action`() = runTest {
    coEvery { getRemindersV2InRangeUseCase(any(), any()) } returns
      listOf(reminder(action = ReminderAction.Call("+123")))

    val event = useCase(this, day).single()

    val action = event.action?.value as ResolvedEventAction.MakeCall
    assertEquals("+123", action.phoneNumber)
  }

  @Test
  fun `has no event action for a reminder with no action`() = runTest {
    coEvery { getRemindersV2InRangeUseCase(any(), any()) } returns listOf(reminder(action = ReminderAction.None))

    val event = useCase(this, day).single()

    assertNull(event.action)
  }

  @Test
  fun `formats the shopping list as the secondary text for a Shopping action`() = runTest {
    every { shopItemsFormatter.formatV2(any()) } returns "formatted list"
    val items = listOf(ShopItemV2(summary = "Milk", createdAt = day))
    coEvery { getRemindersV2InRangeUseCase(any(), any()) } returns
      listOf(reminder(action = ReminderAction.Shopping, shoppingItems = items))

    val event = useCase(this, day).single()

    assertEquals("formatted list", event.description)
  }
}
