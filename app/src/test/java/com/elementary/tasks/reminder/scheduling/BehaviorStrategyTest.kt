package com.elementary.tasks.reminder.scheduling

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.elementary.tasks.reminder.scheduling.behavior.BehaviorStrategyResolver
import com.elementary.tasks.reminder.scheduling.behavior.IntervalRepeatStrategy
import com.elementary.tasks.reminder.scheduling.behavior.LocationBasedStrategy
import com.elementary.tasks.reminder.scheduling.behavior.MonthlyRepeatStrategy
import com.elementary.tasks.reminder.scheduling.behavior.RecurRepeatStrategy
import com.elementary.tasks.reminder.scheduling.behavior.SimpleDateStrategy
import com.elementary.tasks.reminder.scheduling.behavior.TimerRepeatStrategy
import com.elementary.tasks.reminder.scheduling.behavior.WeekdayRepeatStrategy
import com.elementary.tasks.reminder.scheduling.behavior.YearlyRepeatStrategy
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for BehaviorStrategyResolver.
 *
 * Tests the property-based strategy resolution logic to ensure the correct
 * strategy is selected based on reminder properties without relying on reminderType.
 */
class BehaviorStrategyResolverTest : BaseTest() {

  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var recurEventManager: RecurEventManager
  private lateinit var resolver: BehaviorStrategyResolver

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    recurEventManager = mockk()
    resolver = BehaviorStrategyResolver(
      dateTimeManager,
      recurEventManager
    )
  }

  @Test
  fun `resolve returns LocationBasedStrategy when reminder has places`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Reminder at office",
      places = listOf(
        Place(
          latitude = 40.7128,
          longitude = -74.0060,
          name = "Office",
          syncState = SyncState.Synced
        )
      ),
      syncState = SyncState.Synced
    )

    // Act
    val strategy = resolver.resolve(reminder)

    // Assert
    assertTrue(strategy is LocationBasedStrategy)
  }

  @Test
  fun `resolve returns RecurRepeatStrategy when reminder has recurDataObject`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Recurrence reminder",
      recurDataObject = "RRULE:FREQ=WEEKLY;BYDAY=TU,TH",
      syncState = SyncState.Synced
    )

    // Act
    val strategy = resolver.resolve(reminder)

    // Assert
    assertTrue(strategy is RecurRepeatStrategy)
  }

  @Test
  fun `resolve returns TimerRepeatStrategy when reminder has from, to, and hours`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Timer reminder",
      from = "09:00",
      to = "17:00",
      hours = listOf(9, 12, 15, 17),
      after = 1000L,
      syncState = SyncState.Synced
    )

    // Act
    val strategy = resolver.resolve(reminder)

    // Assert
    assertTrue(strategy is TimerRepeatStrategy)
  }

  @Test
  fun `resolve returns YearlyRepeatStrategy when reminder has dayOfMonth and monthOfYear`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Birthday",
      dayOfMonth = 15,
      monthOfYear = 3, // March
      syncState = SyncState.Synced
    )

    // Act
    val strategy = resolver.resolve(reminder)

    // Assert
    assertTrue(strategy is YearlyRepeatStrategy)
  }

  @Test
  fun `resolve returns MonthlyRepeatStrategy when reminder has dayOfMonth only`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Monthly reminder",
      dayOfMonth = 15,
      monthOfYear = -1,
      syncState = SyncState.Synced
    )

    // Act
    val strategy = resolver.resolve(reminder)

    // Assert
    assertTrue(strategy is MonthlyRepeatStrategy)
  }

  @Test
  fun `resolve returns WeekdayRepeatStrategy when reminder has weekdays`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Weekly reminder",
      weekdays = listOf(1, 3, 5), // Monday, Wednesday, Friday
      syncState = SyncState.Synced
    )

    // Act
    val strategy = resolver.resolve(reminder)

    // Assert
    assertTrue(strategy is WeekdayRepeatStrategy)
  }

  @Test
  fun `resolve returns IntervalRepeatStrategy when reminder has repeatInterval`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Interval reminder",
      repeatInterval = 86400000L, // Daily
      syncState = SyncState.Synced
    )

    // Act
    val strategy = resolver.resolve(reminder)

    // Assert
    assertTrue(strategy is IntervalRepeatStrategy)
  }

  @Test
  fun `resolve returns SimpleDateStrategy when reminder has no repeat properties`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "Simple reminder",
      eventTime = "2025-01-06T10:00:00Z",
      syncState = SyncState.Synced
    )

    // Act
    val strategy = resolver.resolve(reminder)

    // Assert
    assertTrue(strategy is SimpleDateStrategy)
  }

  @Test
  fun `resolve prioritizes location over other properties`() = runTest {
    // Arrange - Has both places AND weekdays
    val reminder = Reminder(
      summary = "Location with weekdays",
      places = listOf(Place(latitude = 40.7128, longitude = -74.0060, name = "Office", syncState = SyncState.Synced)),
      weekdays = listOf(1, 3, 5),
      syncState = SyncState.Synced
    )

    // Act
    val strategy = resolver.resolve(reminder)

    // Assert - Location should take priority
    assertTrue(strategy is LocationBasedStrategy)
  }

  @Test
  fun `resolve prioritizes recur over other time-based properties`() = runTest {
    // Arrange - Has both recurDataObject AND weekdays
    val reminder = Reminder(
      summary = "Recur with weekdays",
      recurDataObject = "RRULE:FREQ=WEEKLY;BYDAY=TU,TH",
      weekdays = listOf(1, 3, 5),
      syncState = SyncState.Synced
    )

    // Act
    val strategy = resolver.resolve(reminder)

    // Assert - Recur should take priority
    assertTrue(strategy is RecurRepeatStrategy)
  }

  @Test
  fun `resolve prioritizes yearly over monthly when both properties set`() = runTest {
    // Arrange - Has both dayOfMonth AND monthOfYear
    val reminder = Reminder(
      summary = "Yearly reminder",
      dayOfMonth = 15,
      monthOfYear = 3,
      syncState = SyncState.Synced
    )

    // Act
    val strategy = resolver.resolve(reminder)

    // Assert - Yearly should be selected
    assertTrue(strategy is YearlyRepeatStrategy)
  }
}

/**
 * Unit tests for IntervalRepeatStrategy.
 */
class IntervalRepeatStrategyTest : BaseTest() {

  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var strategy: IntervalRepeatStrategy

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    strategy = IntervalRepeatStrategy(dateTimeManager)
  }

  @Test
  fun `calculateNextOccurrence returns next time when not limit exceeded`() = runTest {
    // Arrange
    val reminder = Reminder(
      eventTime = "2025-01-06T10:00:00Z",
      repeatInterval = 3600000L, // 1 hour
      repeatLimit = 10,
      eventCount = 5,
      syncState = SyncState.Synced
    )

    val fromTime = LocalDateTime.of(2025, 1, 6, 10, 0)

    every {
      dateTimeManager.fromGmtToLocal(any())
    } returns fromTime

    every {
      dateTimeManager.getCurrentDateTime()
    } returns fromTime

    // Act
    val result = strategy.calculateNextOccurrence(reminder, fromTime)

    // Assert
    assertNotNull(result)
  }

  @Test
  fun `calculateNextOccurrence returns null when limit exceeded`() = runTest {
    // Arrange
    val reminder = Reminder(
      eventTime = "2025-01-06T10:00:00Z",
      repeatInterval = 3600000L,
      repeatLimit = 10,
      eventCount = 10, // Limit reached
      syncState = SyncState.Synced
    )

    val fromTime = LocalDateTime.of(2025, 1, 6, 10, 0)

    // Act
    val result = strategy.calculateNextOccurrence(reminder, fromTime)

    // Assert
    assertNull(result)
  }

  @Test
  fun `canSkip returns true when has repeatInterval and not limit exceeded`() = runTest {
    // Arrange
    val reminder = Reminder(
      repeatInterval = 3600000L,
      repeatLimit = 10,
      eventCount = 5,
      syncState = SyncState.Synced
    )

    // Act
    val result = strategy.canSkip(reminder)

    // Assert
    assertTrue(result)
  }

  @Test
  fun `canSkip returns false when limit exceeded`() = runTest {
    // Arrange
    val reminder = Reminder(
      repeatInterval = 3600000L,
      repeatLimit = 10,
      eventCount = 10,
      syncState = SyncState.Synced
    )

    // Act
    val result = strategy.canSkip(reminder)

    // Assert
    assertEquals(false, result)
  }

  @Test
  fun `canSnooze returns true for interval reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      repeatInterval = 3600000L,
      syncState = SyncState.Synced
    )

    // Act
    val result = strategy.canSnooze(reminder)

    // Assert
    assertTrue(result)
  }

  @Test
  fun `canStartImmediately returns true for interval reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      repeatInterval = 3600000L,
      eventTime = "2025-01-06T10:00:00Z",
      syncState = SyncState.Synced
    )

    every {
      dateTimeManager.isCurrent(any<String>())
    } returns true

    // Act
    val result = strategy.canStartImmediately(reminder)

    // Assert
    assertTrue(result)
  }
}

/**
 * Unit tests for SimpleDateStrategy.
 */
class SimpleDateStrategyTest : BaseTest() {

  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var strategy: SimpleDateStrategy

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    strategy = SimpleDateStrategy(dateTimeManager)
  }

  @Test
  fun `calculateNextOccurrence always returns null for simple reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "One-time reminder",
      syncState = SyncState.Synced
    )

    val fromTime = LocalDateTime.of(2025, 1, 6, 10, 0)

    // Act
    val result = strategy.calculateNextOccurrence(reminder, fromTime)

    // Assert
    assertNull(result)
  }

  @Test
  fun `canSkip always returns false for simple reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "One-time reminder",
      syncState = SyncState.Synced
    )

    // Act
    val result = strategy.canSkip(reminder)

    // Assert
    assertEquals(false, result)
  }

  @Test
  fun `canSnooze returns true for simple reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      summary = "One-time reminder",
      syncState = SyncState.Synced
    )

    // Act
    val result = strategy.canSnooze(reminder)

    // Assert
    assertTrue(result)
  }

  @Test
  fun `canStartImmediately returns true for simple reminders`() = runTest {
    every { dateTimeManager.isCurrent(any<String>()) } returns true

    // Arrange
    val reminder = Reminder(
      summary = "One-time reminder",
      syncState = SyncState.Synced
    )

    // Act
    val result = strategy.canStartImmediately(reminder)

    // Assert
    assertTrue(result)
  }
}

/**
 * Unit tests for LocationBasedStrategy.
 */
class LocationBasedStrategyTest : BaseTest() {

  private lateinit var strategy: LocationBasedStrategy

  @Before
  override fun setUp() {
    super.setUp()
    strategy = LocationBasedStrategy
  }

  @Test
  fun `calculateNextOccurrence returns null for location reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      places = listOf(Place(latitude = 40.7128, longitude = -74.0060, name = "Office", syncState = SyncState.Synced)),
      syncState = SyncState.Synced
    )

    val fromTime = LocalDateTime.of(2025, 1, 6, 10, 0)

    // Act
    val result = strategy.calculateNextOccurrence(reminder, fromTime)

    // Assert
    assertNull(result)
  }

  @Test
  fun `canSkip returns false for location reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      places = listOf(Place(latitude = 40.7128, longitude = -74.0060, name = "Office", syncState = SyncState.Synced)),
      syncState = SyncState.Synced
    )

    // Act
    val result = strategy.canSkip(reminder)

    // Assert
    assertEquals(false, result)
  }

  @Test
  fun `requiresBackgroundService returns true when has places`() = runTest {
    // Arrange
    val reminder = Reminder(
      places = listOf(Place(latitude = 40.7128, longitude = -74.0060, name = "Office", syncState = SyncState.Synced)),
      syncState = SyncState.Synced
    )

    // Act
    val result = strategy.requiresBackgroundService(reminder)

    // Assert
    assertTrue(result)
  }

  @Test
  fun `requiresTimeScheduling returns false for location reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      places = listOf(Place(latitude = 40.7128, longitude = -74.0060, name = "Office", syncState = SyncState.Synced)),
      syncState = SyncState.Synced
    )

    // Act
    val result = strategy.requiresTimeScheduling(reminder)

    // Assert
    assertEquals(false, result)
  }

  @Test
  fun `canSnooze returns false for location reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      places = listOf(Place(latitude = 40.7128, longitude = -74.0060, name = "Office", syncState = SyncState.Synced)),
      syncState = SyncState.Synced
    )

    // Act
    val result = strategy.canSnooze(reminder)

    // Assert
    assertEquals(false, result)
  }

  @Test
  fun `canStartImmediately returns true for location reminders`() = runTest {
    // Arrange
    val reminder = Reminder(
      places = listOf(Place(latitude = 40.7128, longitude = -74.0060, name = "Office", syncState = SyncState.Synced)),
      syncState = SyncState.Synced
    )

    // Act
    val result = strategy.canStartImmediately(reminder)

    // Assert
    assertTrue(result)
  }
}

