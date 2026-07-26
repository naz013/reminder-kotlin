package com.elementary.tasks.reminder.scheduling

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.elementary.tasks.reminder.scheduling.behavior.BehaviorStrategyResolver
import com.elementary.tasks.reminder.scheduling.behavior.ReminderBehaviorStrategy
import com.elementary.tasks.reminder.scheduling.behavior.v2.BehaviorStrategyResolverV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.ReminderBehaviorStrategyV2
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.reminder.migration.toReminderV2
import com.github.naz013.domain.sync.SyncState
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * The load-bearing safety gate for Phase C (see Finding 2 in the migration plan): asserts the V1
 * [BehaviorStrategyResolver] and the dark, not-yet-wired [BehaviorStrategyResolverV2] pick the
 * *same strategy family* for the same underlying reminder, converted via the existing, already
 * production-used [toReminderV2] mapper. No sub-phase past C1 may cut over a use case onto the V2
 * resolver until every case here (and any further cases a real data audit surfaces) passes.
 */
class BehaviorStrategyResolverParityTest : BaseTest() {
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var recurEventManager: RecurEventManager
  private lateinit var resolverV1: BehaviorStrategyResolver
  private lateinit var resolverV2: BehaviorStrategyResolverV2

  @Before
  override fun setUp() {
    super.setUp()
    dateTimeManager = mockk()
    recurEventManager = mockk()
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.isCurrent(any<LocalDateTime>()) } returns false
    resolverV1 = BehaviorStrategyResolver(dateTimeManager, recurEventManager)
    resolverV2 = BehaviorStrategyResolverV2(dateTimeManager, recurEventManager)
  }

  private fun assertSameStrategyFamily(reminder: Reminder) {
    val v1 = resolverV1.resolve(reminder)
    val v2 = resolverV2.resolve(reminder.toReminderV2())
    assertEquals("${v1::class.simpleName}V2", v2::class.simpleName)
  }

  @Test
  fun `location reminder resolves to the same family`() {
    val reminder =
      Reminder(
        type = Reminder.BY_LOCATION,
        places = listOf(Place(latitude = 40.7128, longitude = -74.0060, name = "Office", syncState = SyncState.Synced)),
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  @Test
  fun `recur reminder resolves to the same family`() {
    val reminder =
      Reminder(
        type = Reminder.BY_RECUR,
        recurDataObject = "RRULE:FREQ=WEEKLY;BYDAY=TU,TH",
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  @Test
  fun `timer-exclusion reminder resolves to the same family`() {
    val reminder =
      Reminder(
        type = Reminder.BY_TIME,
        from = "09:00",
        to = "17:00",
        hours = listOf(9, 12, 15, 17),
        after = 1000L,
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  @Test
  fun `yearly reminder resolves to the same family`() {
    val reminder =
      Reminder(
        type = Reminder.BY_DAY_OF_YEAR,
        dayOfMonth = 15,
        monthOfYear = 3,
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  @Test
  fun `monthly reminder resolves to the same family`() {
    val reminder =
      Reminder(
        type = Reminder.BY_MONTH,
        dayOfMonth = 15,
        monthOfYear = -1,
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  @Test
  fun `weekday reminder resolves to the same family`() {
    val reminder =
      Reminder(
        type = Reminder.BY_WEEK,
        weekdays = listOf(1, 3, 5),
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  @Test
  fun `repeating by-date reminder (interval strategy) resolves to the same family`() {
    val reminder =
      Reminder(
        type = Reminder.BY_DATE,
        eventTime = "2025-01-06 10:00:00.000+0000",
        repeatInterval = 86400000L,
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  @Test
  fun `repeating countdown without an exclusion window resolves to the same family (interval, not timer)`() {
    val reminder =
      Reminder(
        type = Reminder.BY_TIME,
        after = 3600000L,
        repeatInterval = 3600000L,
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  @Test
  fun `plain one-time reminder resolves to the same family`() {
    val reminder =
      Reminder(
        type = Reminder.BY_DATE,
        eventTime = "2025-01-06 10:00:00.000+0000",
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  @Test
  fun `location wins over weekdays in both resolvers`() {
    val reminder =
      Reminder(
        type = Reminder.BY_LOCATION,
        places = listOf(Place(latitude = 40.7128, longitude = -74.0060, name = "Office", syncState = SyncState.Synced)),
        weekdays = listOf(1, 3, 5),
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  @Test
  fun `recur wins over weekdays in both resolvers`() {
    val reminder =
      Reminder(
        type = Reminder.BY_RECUR,
        recurDataObject = "RRULE:FREQ=WEEKLY;BYDAY=TU,TH",
        weekdays = listOf(1, 3, 5),
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  @Test
  fun `yearly wins over monthly in both resolvers`() {
    val reminder =
      Reminder(
        type = Reminder.BY_DAY_OF_YEAR,
        dayOfMonth = 15,
        monthOfYear = 3,
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }

  /**
   * The exact Finding-2 risk made concrete: a legacy row whose `type` and raw fields disagree
   * (weekdays populated on a `BY_DATE`-typed row). V1's resolver ignores `type` entirely and
   * follows the raw `weekdays` field; the forward mapper's `toRecurrenceRule()` derives
   * [com.github.naz013.domain.reminder.v2.RecurrenceRule] from `type` alone, so this is the one
   * shape where the two resolvers are EXPECTED to diverge if V2 trusted `recurrence` blindly with
   * no raw-field fallback for weekday routing - proving the harness can actually catch it.
   */
  @Test
  fun `a malformed legacy row where type and raw fields disagree is expected to diverge`() {
    val reminder =
      Reminder(
        type = Reminder.BY_DATE,
        eventTime = "2025-01-06 10:00:00.000+0000",
        weekdays = listOf(1, 3, 5),
        syncState = SyncState.Synced,
      )

    val v1 = resolverV1.resolve(reminder)
    val v2 = resolverV2.resolve(reminder.toReminderV2())

    // V1 follows the raw `weekdays` field (WeekdayRepeatStrategy); V2's `recurrence` was derived
    // from `type` (BY_DATE -> Once/Daily), so it does NOT see the stray weekdays field at all.
    // This asserts the *documented, expected* divergence for this specific malformed shape -
    // not something Phase C needs to reconcile, since a real reminder should never have a type/
    // field mismatch like this (the builder always keeps them consistent); it's here purely to
    // prove `assertSameStrategyFamily`'s comparison actually discriminates when they truly diverge.
    assertNotEquals("${v1::class.simpleName}V2", v2::class.simpleName)
  }

  @Test
  fun `shopping list with no due date resolves to the same family`() {
    val reminder =
      Reminder(
        type = Reminder.BY_DATE_SHOP,
        shoppings = listOf(ShopItem(summary = "Bread", createTime = "2025-01-06 10:00:00.000+0000")),
        syncState = SyncState.Synced,
      )

    assertSameStrategyFamily(reminder)
  }
}
