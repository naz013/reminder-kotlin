package com.elementary.tasks.core.utils.datetime.recurrence

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationTest {

  @Test
  fun testEmptyStringInput_shouldDoNothing() {
    val duration = Duration("")

    assertEquals(0, duration.weeks)
    assertEquals(0, duration.days)
    assertEquals(0, duration.hours)
    assertEquals(0, duration.minutes)
    assertEquals(0, duration.seconds)
  }

  @Test
  fun testValueConstructor_shouldHaveCorrectValues() {
    val duration = Duration(weeks = 2, days = 0, hours = 0, minutes = 5, seconds = 0)

    assertEquals(2, duration.weeks)
    assertEquals(0, duration.days)
    assertEquals(0, duration.hours)
    assertEquals(5, duration.minutes)
    assertEquals(0, duration.seconds)
  }

  @Test
  fun testUpdate_shouldChangeValues() {
    val duration = Duration(weeks = 2, days = 0, hours = 0, minutes = 5, seconds = 0)

    duration.update(days = 3)

    assertEquals(2, duration.weeks)
    assertEquals(3, duration.days)
    assertEquals(0, duration.hours)
    assertEquals(5, duration.minutes)
    assertEquals(0, duration.seconds)
  }

  @Test
  fun testBuildWithNoValues_shouldReturnEmptyString() {
    val duration = Duration(weeks = 0, days = 0, hours = 0, minutes = 0, seconds = 0)

    assertEquals("", duration.buildString())
  }

  @Test
  fun testBuildWithWeeks_shouldReturnCorrectString() {
    val duration = Duration(weeks = 4, days = 0, hours = 0, minutes = 0, seconds = 0)

    assertEquals("P4W", duration.buildString())
  }

  @Test
  fun testBuildWithWeeksAndDays_shouldReturnCorrectString() {
    val duration = Duration(weeks = 4, days = 15, hours = 0, minutes = 0, seconds = 0)

    assertEquals("P4W15D", duration.buildString())
  }

  @Test
  fun testBuildWithDays_shouldReturnCorrectString() {
    val duration = Duration(weeks = 0, days = 15, hours = 0, minutes = 0, seconds = 0)

    assertEquals("P15D", duration.buildString())
  }

  @Test
  fun testBuildWithDaysAndTime_shouldReturnCorrectString() {
    val duration = Duration(weeks = 0, days = 15, hours = 5, minutes = 0, seconds = 10)

    assertEquals("P15DT5H10S", duration.buildString())
  }

  @Test
  fun testBuildWithTimeAndNoDaysOrWeeks_shouldReturnCorrectString() {
    val duration = Duration(weeks = 0, days = 0, hours = 0, minutes = 25, seconds = 0)

    assertEquals("PT25M", duration.buildString())
  }

  @Test
  fun testBuildWithAllValues_shouldReturnCorrectString() {
    val duration = Duration(weeks = 2, days = 5, hours = 3, minutes = 25, seconds = 30)

    assertEquals("P2W5DT3H25M30S", duration.buildString())
  }

  @Test
  fun testParseWrongString_shouldDoNothing() {
    val duration = Duration("5W")

    assertEquals(0, duration.weeks)
    assertEquals(0, duration.days)
    assertEquals(0, duration.hours)
    assertEquals(0, duration.minutes)
    assertEquals(0, duration.seconds)
  }

  @Test
  fun testParseWeeksDuration_shouldPutCorrectValues() {
    val duration = Duration("P5W")

    assertEquals(5, duration.weeks)
    assertEquals(0, duration.days)
    assertEquals(0, duration.hours)
    assertEquals(0, duration.minutes)
    assertEquals(0, duration.seconds)
  }

  @Test
  fun testParseDaysDuration_shouldPutCorrectValues() {
    val duration = Duration("P5D")

    assertEquals(0, duration.weeks)
    assertEquals(5, duration.days)
    assertEquals(0, duration.hours)
    assertEquals(0, duration.minutes)
    assertEquals(0, duration.seconds)
  }

  @Test
  fun testParseHoursDuration_shouldPutCorrectValues() {
    val duration = Duration("PT15H")

    assertEquals(0, duration.weeks)
    assertEquals(0, duration.days)
    assertEquals(15, duration.hours)
    assertEquals(0, duration.minutes)
    assertEquals(0, duration.seconds)
  }

  @Test
  fun testParseMinutesDuration_shouldPutCorrectValues() {
    val duration = Duration("PT25M")

    assertEquals(0, duration.weeks)
    assertEquals(0, duration.days)
    assertEquals(0, duration.hours)
    assertEquals(25, duration.minutes)
    assertEquals(0, duration.seconds)
  }

  @Test
  fun testParseSecondsDuration_shouldPutCorrectValues() {
    val duration = Duration("PT60S")

    assertEquals(0, duration.weeks)
    assertEquals(0, duration.days)
    assertEquals(0, duration.hours)
    assertEquals(0, duration.minutes)
    assertEquals(60, duration.seconds)
  }

  @Test
  fun testParseFullDuration_shouldPutCorrectValues() {
    val duration = Duration("P2W10DT5H30M60S")

    assertEquals(2, duration.weeks)
    assertEquals(10, duration.days)
    assertEquals(5, duration.hours)
    assertEquals(30, duration.minutes)
    assertEquals(60, duration.seconds)
  }
}
