package com.github.naz013.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderTest {

  @Test
  fun `isLimited returns false when repeatLimit is not set`() {
    val reminder = Reminder(repeatLimit = -1)

    assertFalse(reminder.isLimited())
  }

  @Test
  fun `isLimited returns true when repeatLimit is positive`() {
    val reminder = Reminder(repeatLimit = 3)

    assertTrue(reminder.isLimited())
  }

  @Test
  fun `isLimitExceed is false while events remain`() {
    val reminder = Reminder(repeatLimit = 3, eventCount = 2)

    assertFalse(reminder.isLimitExceed())
  }

  @Test
  fun `isLimitExceed is true once the limit is used up`() {
    val reminder = Reminder(repeatLimit = 3, eventCount = 3)

    assertTrue(reminder.isLimitExceed())
  }

  @Test
  fun `isLimitExceed is false when not limited regardless of eventCount`() {
    val reminder = Reminder(repeatLimit = -1, eventCount = 100)

    assertFalse(reminder.isLimitExceed())
  }

  @Test
  fun `copy resets identity fields and progress`() {
    val original = Reminder(
      summary = "Original",
      eventCount = 5,
      delay = 2,
      isActive = false,
      isRemoved = true
    )

    val copy = original.copy(updatedAt = "2023-01-01")

    assertEquals("Original", copy.summary)
    assertEquals(0L, copy.eventCount)
    assertEquals(0, copy.delay)
    assertTrue(copy.isActive)
    assertFalse(copy.isRemoved)
    assertNotEquals(original.uuId, copy.uuId)
    assertNotEquals(original.uniqueId, copy.uniqueId)
  }

  @Test
  fun `full copy constructor preserves identity fields`() {
    val original = Reminder(summary = "Original", updatedAt = "old")

    val fullCopy = Reminder(original, fullCopy = true, updatedAt = "new")

    assertEquals(original.uuId, fullCopy.uuId)
    assertEquals(original.uniqueId, fullCopy.uniqueId)
    assertEquals(original.updatedAt, fullCopy.updatedAt)
  }

  @Test
  fun `partial copy constructor assigns a new identity and the given updatedAt`() {
    val original = Reminder(summary = "Original", updatedAt = "old")

    val partialCopy = Reminder(original, fullCopy = false, updatedAt = "new")

    assertNotEquals(original.uuId, partialCopy.uuId)
    assertNotEquals(original.uniqueId, partialCopy.uniqueId)
    assertEquals("new", partialCopy.updatedAt)
  }

  @Test
  fun `isBase is true for offsets within the same ten-block`() {
    assertTrue(Reminder.isBase(Reminder.BY_DATE_SHOP, Reminder.BY_DATE))
  }

  @Test
  fun `isBase is false for offsets in the next ten-block`() {
    assertFalse(Reminder.isBase(Reminder.BY_TIME, Reminder.BY_DATE))
  }

  @Test
  fun `isKind matches the action encoded in the type's last digit`() {
    assertTrue(Reminder.isKind(Reminder.BY_LOCATION_CALL, Reminder.Action.CALL))
    assertFalse(Reminder.isKind(Reminder.BY_LOCATION_CALL, Reminder.Action.SMS))
  }

  @Test
  fun `isSame requires an exact type match`() {
    assertTrue(Reminder.isSame(Reminder.BY_DATE, Reminder.BY_DATE))
    assertFalse(Reminder.isSame(Reminder.BY_DATE_APP, Reminder.BY_DATE))
  }

  @Test
  fun `isGpsType is true for location and out types`() {
    assertTrue(Reminder.isGpsType(Reminder.BY_LOCATION_SMS))
    assertTrue(Reminder.isGpsType(Reminder.BY_OUT_CALL))
    assertFalse(Reminder.isGpsType(Reminder.BY_DATE))
  }

  @Test
  fun `gpsTypes contains every gps-related type constant`() {
    val types = Reminder.gpsTypes().toList()

    assertEquals(9, types.size)
    assertTrue(types.contains(Reminder.BY_LOCATION))
    assertTrue(types.contains(Reminder.BY_OUT_SMS))
  }
}
