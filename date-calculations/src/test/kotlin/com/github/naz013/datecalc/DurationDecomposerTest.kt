package com.github.naz013.datecalc

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationDecomposerTest {

  @Test
  fun `decompose returns zero seconds for a zero duration`() {
    val result = DurationDecomposer.decompose(0)

    assertEquals(DurationDecomposer.Duration(0, DurationDecomposer.Unit.SECOND), result)
  }

  @Test
  fun `decompose picks MONTH when the duration is an exact number of months`() {
    val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000

    val result = DurationDecomposer.decompose(thirtyDaysMillis)

    assertEquals(DurationDecomposer.Duration(1, DurationDecomposer.Unit.MONTH), result)
  }

  @Test
  fun `decompose picks WEEK when the duration is exact weeks but not exact months`() {
    val fourteenDaysMillis = 14L * 24 * 60 * 60 * 1000

    val result = DurationDecomposer.decompose(fourteenDaysMillis)

    assertEquals(DurationDecomposer.Duration(2, DurationDecomposer.Unit.WEEK), result)
  }

  @Test
  fun `decompose picks DAY when the duration is exact days but not exact weeks`() {
    val threeDaysMillis = 3L * 24 * 60 * 60 * 1000

    val result = DurationDecomposer.decompose(threeDaysMillis)

    assertEquals(DurationDecomposer.Duration(3, DurationDecomposer.Unit.DAY), result)
  }

  @Test
  fun `decompose picks HOUR when the duration is exact hours but not exact days`() {
    val fiveHoursMillis = 5L * 60 * 60 * 1000

    val result = DurationDecomposer.decompose(fiveHoursMillis)

    assertEquals(DurationDecomposer.Duration(5, DurationDecomposer.Unit.HOUR), result)
  }

  @Test
  fun `decompose picks MINUTE when the duration is exact minutes but not exact hours`() {
    val ninetyMinutesMillis = 90L * 60 * 1000

    val result = DurationDecomposer.decompose(ninetyMinutesMillis)

    assertEquals(DurationDecomposer.Duration(90, DurationDecomposer.Unit.MINUTE), result)
  }

  @Test
  fun `decompose picks SECOND when the duration is exact seconds but not exact minutes`() {
    val fortyFiveSecondsMillis = 45L * 1000

    val result = DurationDecomposer.decompose(fortyFiveSecondsMillis)

    assertEquals(DurationDecomposer.Duration(45, DurationDecomposer.Unit.SECOND), result)
  }

  @Test
  fun `decompose falls back to zero seconds when the duration has a sub-second remainder`() {
    val result = DurationDecomposer.decompose(1500)

    assertEquals(DurationDecomposer.Duration(0, DurationDecomposer.Unit.SECOND), result)
  }
}
