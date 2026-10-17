package com.github.naz013.feature.home.scheduleview

import android.content.Context
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.home.HomeEvent
import com.github.naz013.feature.home.HomePreferences
import com.github.naz013.logic.reminder.today.TodayBucket
import com.github.naz013.logic.reminder.today.TodayItem
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.reminder.ShopItemsFormatter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class TodayItemHomeEventMapperTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val modelDateTimeFormatter = mockk<ModelDateTimeFormatter>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val shopItemsFormatter = mockk<ShopItemsFormatter>(relaxed = true)
  private val contextProvider = mockk<ContextProvider>(relaxed = true)
  private val homePreferences = mockk<HomePreferences>(relaxed = true)
  private val now = LocalDateTime.of(2026, 7, 24, 12, 0)

  private lateinit var mapper: TodayItemHomeEventMapper

  @Before
  override fun setUp() {
    super.setUp()
    mockkObject(ThemeProvider)
    every { ThemeProvider.colorBirthdayCalendar(any<Context>(), any()) } returns 0
    every { ThemeProvider.themedColor(any<Context>(), any()) } returns 0
    coEvery { groupV2Repository.getAll() } returns emptyList()
    every { dateTimeManager.getBirthdayLocalTime() } returns null
    every { dateTimeManager.parseBirthdayDate(any()) } returns LocalDate.of(1990, 7, 24)
    mapper = TodayItemHomeEventMapper(
      dateTimeManager,
      groupV2Repository,
      modelDateTimeFormatter,
      textProvider,
      shopItemsFormatter,
      contextProvider,
      homePreferences,
    )
  }

  @After
  override fun tearDown() {
    unmockkObject(ThemeProvider)
    super.tearDown()
  }

  private fun reminderItem(isOverdue: Boolean, dateTime: LocalDateTime = now.plusHours(1)) =
    TodayItem.Reminder(ReminderV2(uuId = "r1", summary = "Call", schedule = ReminderSchedule(startDateTime = dateTime, eventDateTime = dateTime)), dateTime, isOverdue)

  private fun birthday() = Birthday(uuId = "b1", name = "Ann", syncState = SyncState.Synced)

  @Test
  fun `marks reminders from earlier days as overdue`() = runTest {
    val result = mapper(listOf(reminderItem(isOverdue = true, dateTime = now.minusDays(1))), now)

    assertTrue(result.single().isOverdue)
  }

  @Test
  fun `does not mark upcoming reminders as overdue`() = runTest {
    val result = mapper(listOf(reminderItem(isOverdue = false)), now)

    assertFalse(result.single().isOverdue)
  }

  @Test
  fun `computes birthday remaining from the future birthday date for all-day birthdays`() = runTest {
    val future = LocalDateTime.of(2026, 7, 24, 12, 0)
    every { modelDateTimeFormatter.getFutureBirthdayDate(any(), any(), any(), any(), ) } returns future
    every { modelDateTimeFormatter.getBirthdayRemaining(future, any(), now) } returns "in 3 years"
    val item = TodayItem.Birthday(birthday(), now.toLocalDate().atStartOfDay(), TodayBucket.AllDay)

    val event = mapper(listOf(item), now).single()

    assertEquals("in 3 years", event.remaining)
    assertEquals(future.toLocalDate(), event.date)
    assertEquals(future.toLocalTime(), event.time)
    assertEquals(HomeEvent.EventType.Birthday, event.type)
  }

  @Test
  fun `computes birthday remaining from the future birthday date for timed birthdays`() = runTest {
    every { dateTimeManager.getBirthdayLocalTime() } returns LocalTime.of(9, 0)
    val future = LocalDateTime.of(2026, 7, 24, 9, 0)
    every { modelDateTimeFormatter.getFutureBirthdayDate(LocalTime.of(9, 0), any(), any(), any()) } returns future
    every { modelDateTimeFormatter.getBirthdayRemaining(future, any(), now) } returns "earlier"
    val item = TodayItem.Birthday(birthday(), future, TodayBucket.Timed)

    val event = mapper(listOf(item), now).single()

    assertEquals("earlier", event.remaining)
    assertEquals(LocalTime.of(9, 0), event.time)
  }

  @Test
  fun `skips calendar events and tasks`() = runTest {
    val calendar = TodayItem.CalendarEvent(mockk<GoogleCalendarEvent>(), now, null)
    val task = TodayItem.Task(mockk<GoogleTask>(), now, TodayBucket.Anytime)

    val result = mapper(listOf(calendar, task), now)

    assertTrue(result.isEmpty())
  }

  @Test
  fun `uses the reminder group name for the row`() = runTest {
    coEvery { groupV2Repository.getAll() } returns listOf(GroupV2(uuId = "g1", title = "Work", color = 3))
    val item = TodayItem.Reminder(ReminderV2(uuId = "r1", summary = "Call", groupId = "g1", schedule = ReminderSchedule(startDateTime = now, eventDateTime = now)), now.plusHours(1), false)

    val event = mapper(listOf(item), now).single()

    assertEquals("Work", event.groupName)
  }
}
