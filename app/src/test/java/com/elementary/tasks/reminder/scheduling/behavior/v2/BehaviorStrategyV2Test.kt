package com.elementary.tasks.reminder.scheduling.behavior.v2

import com.elementary.tasks.BaseTest
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logic.reminder.RecurEventManager
import com.github.naz013.logic.reminder.behavior.IntervalRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.LocationBasedStrategyV2
import com.github.naz013.logic.reminder.behavior.MonthlyRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.NoReminderStrategyV2
import com.github.naz013.logic.reminder.behavior.RecurRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.SimpleDateStrategyV2
import com.github.naz013.logic.reminder.behavior.TimerRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.WeekdayRepeatStrategyV2
import com.github.naz013.logic.reminder.behavior.YearlyRepeatStrategyV2
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

private val NOW: LocalDateTime = LocalDateTime.of(2025, 1, 6, 10, 0)

private fun reminderV2(
  recurrence: RecurrenceRule = RecurrenceRule.Once,
  eventDateTime: LocalDateTime? = NOW,
  places: List<Place> = emptyList(),
  shoppingItems: List<com.github.naz013.domain.reminder.v2.ShopItemV2> = emptyList(),
  notification: NotificationSettingsOverride = NotificationSettingsOverride(),
  eventCount: Long = 0,
) = ReminderV2(
  recurrence = recurrence,
  schedule = ReminderSchedule(startDateTime = NOW, eventDateTime = eventDateTime),
  places = places,
  shoppingItems = shoppingItems,
  notification = notification,
  eventCount = eventCount,
)

class SimpleDateStrategyV2Test : BaseTest() {
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var strategy: SimpleDateStrategyV2

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    recurrenceCalculator = mockk(relaxed = true)
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    strategy = SimpleDateStrategyV2(dateTimeManager)
  }

  @Test
  fun `calculateNextOccurrence always returns null`() {
    assertNull(strategy.calculateNextOccurrence(reminderV2(), NOW))
  }

  @Test
  fun `canSkip always returns false`() {
    assertEquals(false, strategy.canSkip(reminderV2()))
  }

  @Test
  fun `canSnooze returns true`() {
    assertTrue(strategy.canSnooze(reminderV2()))
  }

  @Test
  fun `canStartImmediately reflects whether the event time is current`() {
    every { dateTimeManager.isCurrent(any<LocalDateTime>()) } returns true

    assertTrue(strategy.canStartImmediately(reminderV2(eventDateTime = NOW)))
  }

  @Test
  fun `canStartImmediately is false with no event time`() {
    assertEquals(false, strategy.canStartImmediately(reminderV2(eventDateTime = null)))
  }
}

class LocationBasedStrategyV2Test : BaseTest() {
  private val place = Place(latitude = 40.7128, longitude = -74.0060, name = "Office", syncState = SyncState.Synced)

  @Test
  fun `calculateNextOccurrence returns null for location reminders`() {
    assertNull(LocationBasedStrategyV2.calculateNextOccurrence(reminderV2(RecurrenceRule.LocationEnter, places = listOf(place)), NOW))
  }

  @Test
  fun `canSkip returns false`() {
    assertEquals(false, LocationBasedStrategyV2.canSkip(reminderV2(RecurrenceRule.LocationEnter, places = listOf(place))))
  }

  @Test
  fun `requiresBackgroundService is true when it has places`() {
    assertTrue(LocationBasedStrategyV2.requiresBackgroundService(reminderV2(RecurrenceRule.LocationEnter, places = listOf(place))))
  }

  @Test
  fun `requiresTimeScheduling is false`() {
    assertEquals(false, LocationBasedStrategyV2.requiresTimeScheduling(reminderV2(RecurrenceRule.LocationEnter, places = listOf(place))))
  }

  @Test
  fun `canSnooze is false`() {
    assertEquals(false, LocationBasedStrategyV2.canSnooze(reminderV2(RecurrenceRule.LocationEnter, places = listOf(place))))
  }

  @Test
  fun `canStartImmediately is true`() {
    assertTrue(LocationBasedStrategyV2.canStartImmediately(reminderV2(RecurrenceRule.LocationEnter, places = listOf(place))))
  }
}

class NoReminderStrategyV2Test : BaseTest() {
  @Test
  fun `calculateNextOccurrence returns null`() {
    assertNull(NoReminderStrategyV2.calculateNextOccurrence(reminderV2(eventDateTime = null), NOW))
  }

  @Test
  fun `canSkip returns false`() {
    assertEquals(false, NoReminderStrategyV2.canSkip(reminderV2()))
  }

  @Test
  fun `canSnooze and canStartImmediately are true`() {
    assertTrue(NoReminderStrategyV2.canSnooze(reminderV2()))
    assertTrue(NoReminderStrategyV2.canStartImmediately(reminderV2()))
  }
}

class RecurRepeatStrategyV2Test : BaseTest() {
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var recurEventManager: RecurEventManager
  private lateinit var strategy: RecurRepeatStrategyV2

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    recurEventManager = mockk()
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    strategy = RecurRepeatStrategyV2(dateTimeManager, recurEventManager)
  }

  @Test
  fun `calculateNextOccurrence delegates to RecurEventManager with the rrule`() {
    val rrule = "RRULE:FREQ=WEEKLY;BYDAY=TU,TH"
    every { recurEventManager.getNextAfterDateTime(NOW, rrule) } returns NOW.plusWeeks(1)

    val result = strategy.calculateNextOccurrence(reminderV2(RecurrenceRule.ICalendar(rrule)), NOW)

    assertEquals(NOW.plusWeeks(1), result)
  }

  @Test
  fun `calculateNextOccurrence returns null for a non-ICalendar recurrence`() {
    assertNull(strategy.calculateNextOccurrence(reminderV2(RecurrenceRule.Once), NOW))
  }

  @Test
  fun `canSkip is true when a next occurrence exists`() {
    val rrule = "RRULE:FREQ=WEEKLY;BYDAY=TU,TH"
    every { recurEventManager.getNextAfterDateTime(NOW, rrule) } returns NOW.plusWeeks(1)

    assertTrue(strategy.canSkip(reminderV2(RecurrenceRule.ICalendar(rrule), eventDateTime = NOW)))
  }
}

class TimerRepeatStrategyV2Test : BaseTest() {
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var strategy: TimerRepeatStrategyV2

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    recurrenceCalculator = mockk(relaxed = true)
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.toLocalTime(any()) } returns null
    every { dateTimeManager.getCurrentDateTime() } returns NOW
    strategy = TimerRepeatStrategyV2(dateTimeManager, recurrenceCalculator)
  }

  @Test
  fun `calculateNextOccurrence returns null when limit exceeded`() {
    val reminder =
      reminderV2(
        RecurrenceRule.Countdown(after = 3600000L, repeatInterval = 3600000L, repeatLimit = 10),
        eventCount = 10,
      )

    assertNull(strategy.calculateNextOccurrence(reminder, NOW))
  }

  @Test
  fun `calculateNextOccurrence returns null when repeatInterval is not positive`() {
    val reminder = reminderV2(RecurrenceRule.Countdown(after = 3600000L, repeatInterval = 0L))

    assertNull(strategy.calculateNextOccurrence(reminder, NOW))
  }

  @Test
  fun `canSkip is true while under the limit`() {
    val reminder =
      reminderV2(RecurrenceRule.Countdown(after = 3600000L, repeatInterval = 3600000L, repeatLimit = 10), eventCount = 5)

    assertTrue(strategy.canSkip(reminder))
  }

  @Test
  fun `canSkip is false once the limit is exceeded`() {
    val reminder =
      reminderV2(RecurrenceRule.Countdown(after = 3600000L, repeatInterval = 3600000L, repeatLimit = 10), eventCount = 10)

    assertEquals(false, strategy.canSkip(reminder))
  }
}

class WeekdayRepeatStrategyV2Test : BaseTest() {
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var strategy: WeekdayRepeatStrategyV2

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    recurrenceCalculator = mockk(relaxed = true)
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.getCurrentDateTime() } returns NOW
    strategy = WeekdayRepeatStrategyV2(dateTimeManager, recurrenceCalculator)
  }

  @Test
  fun `calculateNextOccurrence returns null when weekdays is empty`() {
    assertNull(strategy.calculateNextOccurrence(reminderV2(RecurrenceRule.Weekly(weekdays = emptyList())), NOW))
  }

  @Test
  fun `canSkip is true with weekdays set and not limit exceeded`() {
    assertTrue(strategy.canSkip(reminderV2(RecurrenceRule.Weekly(weekdays = listOf(1, 3, 5)))))
  }

  @Test
  fun `canSkip is false once the limit is exceeded`() {
    val reminder = reminderV2(RecurrenceRule.Weekly(weekdays = listOf(1, 3, 5), repeatLimit = 2), eventCount = 2)

    assertEquals(false, strategy.canSkip(reminder))
  }
}

class YearlyRepeatStrategyV2Test : BaseTest() {
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var strategy: YearlyRepeatStrategyV2

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    recurrenceCalculator = mockk(relaxed = true)
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.getCurrentDateTime() } returns NOW
    strategy = YearlyRepeatStrategyV2(dateTimeManager, recurrenceCalculator)
  }

  @Test
  fun `calculateNextOccurrence returns null when limit exceeded`() {
    val reminder = reminderV2(RecurrenceRule.Yearly(dayOfMonth = 15, monthOfYear = 2, repeatLimit = 1), eventCount = 1)

    assertNull(strategy.calculateNextOccurrence(reminder, NOW))
  }

  @Test
  fun `canSkip is true while not limit exceeded`() {
    assertTrue(strategy.canSkip(reminderV2(RecurrenceRule.Yearly(dayOfMonth = 15, monthOfYear = 2))))
  }
}

class MonthlyRepeatStrategyV2Test : BaseTest() {
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var strategy: MonthlyRepeatStrategyV2

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    recurrenceCalculator = mockk()
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.getCurrentDateTime() } returns NOW
    strategy = MonthlyRepeatStrategyV2(dateTimeManager, recurrenceCalculator)
  }

  @Test
  fun `canSkip is true with a valid dayOfMonth and not limit exceeded`() {
    assertTrue(strategy.canSkip(reminderV2(RecurrenceRule.Monthly(dayOfMonth = 15))))
  }

  @Test
  fun `canSkip is false once the limit is exceeded`() {
    val reminder = reminderV2(RecurrenceRule.Monthly(dayOfMonth = 15, repeatLimit = 3), eventCount = 3)

    assertEquals(false, strategy.canSkip(reminder))
  }
}

class IntervalRepeatStrategyV2Test : BaseTest() {
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var recurrenceCalculator: RecurrenceCalculator
  private lateinit var strategy: IntervalRepeatStrategyV2

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    recurrenceCalculator = mockk(relaxed = true)
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.getCurrentDateTime() } returns NOW
    strategy = IntervalRepeatStrategyV2(dateTimeManager, recurrenceCalculator)
  }

  @Test
  fun `calculateNextOccurrence returns next time for a repeating Daily reminder`() {
    val reminder = reminderV2(RecurrenceRule.Daily(repeatInterval = 3600000L, repeatLimit = 10), eventCount = 5)

    assertNotNull(strategy.calculateNextOccurrence(reminder, NOW))
  }

  @Test
  fun `calculateNextOccurrence returns next time for an exclusion-less repeating Countdown`() {
    val reminder = reminderV2(RecurrenceRule.Countdown(after = 3600000L, repeatInterval = 3600000L, repeatLimit = 10))

    assertNotNull(strategy.calculateNextOccurrence(reminder, NOW))
  }

  @Test
  fun `calculateNextOccurrence returns null when limit exceeded`() {
    val reminder = reminderV2(RecurrenceRule.Daily(repeatInterval = 3600000L, repeatLimit = 10), eventCount = 10)

    assertNull(strategy.calculateNextOccurrence(reminder, NOW))
  }

  @Test
  fun `canSkip is false when limit exceeded`() {
    val reminder = reminderV2(RecurrenceRule.Daily(repeatInterval = 3600000L, repeatLimit = 10), eventCount = 10)

    assertEquals(false, strategy.canSkip(reminder))
  }

  @Test
  fun `canSnooze returns true`() {
    assertTrue(strategy.canSnooze(reminderV2(RecurrenceRule.Daily(repeatInterval = 3600000L))))
  }
}
