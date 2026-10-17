package com.github.naz013.feature.home.dayoverview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

class DayOverviewCalculatorTest {

  private fun minute(hour: Int, minute: Int = 0) = hour * 60 + minute

  @Test
  fun `is free for the rest of the day when there are no items`() {
    val result = DayOverviewCalculator.calculateFreeTime(emptyList(), minute(10))

    assertTrue(result.isFreeNow)
    assertNull(result.freeUntilMinute)
    assertEquals(FreeBlock(minute(10), minute(24)), result.largestBlock)
  }

  @Test
  fun `reports free until the next item when one item is ahead`() {
    val result = DayOverviewCalculator.calculateFreeTime(listOf(minute(14)), minute(10))

    assertTrue(result.isFreeNow)
    assertEquals(minute(14), result.freeUntilMinute)
    assertEquals(FreeBlock(minute(14, 30), minute(24)), result.largestBlock)
  }

  @Test
  fun `is not free now while inside an item block`() {
    val result = DayOverviewCalculator.calculateFreeTime(listOf(minute(10)), minute(10, 10))

    assertFalse(result.isFreeNow)
    assertNull(result.freeUntilMinute)
    assertEquals(FreeBlock(minute(10, 30), minute(24)), result.largestBlock)
  }

  @Test
  fun `merges overlapping items into one busy block`() {
    val result = DayOverviewCalculator.calculateFreeTime(
      listOf(minute(12), minute(12, 15), minute(12, 40)),
      minute(8),
    )

    assertEquals(minute(12), result.freeUntilMinute)
    assertEquals(FreeBlock(minute(13, 10), minute(24)), result.largestBlock)
  }

  @Test
  fun `picks the largest gap between items`() {
    val result = DayOverviewCalculator.calculateFreeTime(
      listOf(minute(9), minute(10), minute(18), minute(23)),
      minute(8),
    )

    assertEquals(FreeBlock(minute(10, 30), minute(18)), result.largestBlock)
  }

  @Test
  fun `ignores items that are already past`() {
    val result = DayOverviewCalculator.calculateFreeTime(listOf(minute(8), minute(9)), minute(15))

    assertTrue(result.isFreeNow)
    assertNull(result.freeUntilMinute)
    assertEquals(FreeBlock(minute(15), minute(24)), result.largestBlock)
  }

  @Test
  fun `drops gaps shorter than the minimum free block`() {
    val result = DayOverviewCalculator.calculateFreeTime(listOf(minute(10, 10)), minute(10))

    assertFalse(result.isFreeNow)
    assertNull(result.freeUntilMinute)
  }

  @Test
  fun `has no free block when the last item runs to the end of the day`() {
    val result = DayOverviewCalculator.calculateFreeTime(listOf(minute(23, 45)), minute(23, 40))

    assertFalse(result.isFreeNow)
    assertNull(result.largestBlock)
  }

  @Test
  fun `finds the first item at or after now`() {
    val now = LocalDateTime.of(2026, 10, 17, 12, 0)
    val times = listOf(now.minusHours(2), now, now.plusHours(1))

    assertEquals(1, DayOverviewCalculator.findNextIndex(times, now))
  }

  @Test
  fun `finds no next item when all are past`() {
    val now = LocalDateTime.of(2026, 10, 17, 12, 0)

    assertNull(DayOverviewCalculator.findNextIndex(listOf(now.minusHours(1)), now))
  }

  @Test
  fun `finds no next item when the list is empty`() {
    assertNull(DayOverviewCalculator.findNextIndex(emptyList(), LocalDateTime.of(2026, 10, 17, 12, 0)))
  }
}
