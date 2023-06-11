package com.elementary.tasks.core.utils.datetime

import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import com.elementary.tasks.core.utils.datetime.recurrence.builder.RuleBuilder
import com.elementary.tasks.core.utils.datetime.recurrence.parser.TagParser
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class RecurEventManagerTest {

  private val dateTimeManager = mockk<DateTimeManager>()
  private val ruleBuilder = RuleBuilder()
  private val tagParser = TagParser()

  private val recurrenceManager = RecurrenceManager(ruleBuilder, tagParser, dateTimeManager)
  private val recurEventManager = RecurEventManager(recurrenceManager)

  @Test
  fun testFindNext_onEmptyObject() {
    val input = ""
    val currentDateTime = UtcDateTime("20230517T120000Z")
    val nextDateTime = recurEventManager.getNextAfterDateTime(currentDateTime.dateTime, input)
    assertNull(nextDateTime)
  }

  @Test
  fun testFindNext_May_Daily() {
    val input = "DTSTART:20230516T120000Z\n" +
      "RRULE:FREQ=DAILY;INTERVAL=1;COUNT=5\n" +
      "RDATE;VALUE=DATE-TIME:20230516T120000Z,20230517T120000Z,20230518T120000Z,20\n" +
      " 230519T120000Z,20230520T120000Z"

    val currentDateTime = UtcDateTime("20230517T120000Z")

    val nextDateTime = recurEventManager.getNextAfterDateTime(currentDateTime.dateTime, input)

    val expected = UtcDateTime("20230518T120000Z")
    println("expected=$expected, nextDateTime=$nextDateTime")
    assertEquals(expected.dateTime, nextDateTime)

    val nextDateTime2 = recurEventManager.getNextAfterDateTime(expected.dateTime, input)

    val expected2 = UtcDateTime("20230519T120000Z")
    println("expected=$expected2, nextDateTime=$nextDateTime2")
    assertEquals(expected2.dateTime, nextDateTime2)

    val nextDateTime3 = recurEventManager.getNextAfterDateTime(expected2.dateTime, input)

    val expected3 = UtcDateTime("20230520T120000Z")
    println("expected=$expected3, nextDateTime=$nextDateTime3")
    assertEquals(expected3.dateTime, nextDateTime3)
  }

  @Test
  fun testFindNext_May_Daily_last_shouldReturnNull() {
    val input = "DTSTART:20230516T120000Z\n" +
      "RRULE:FREQ=DAILY;INTERVAL=1;COUNT=5\n" +
      "RDATE;VALUE=DATE-TIME:20230516T120000Z,20230517T120000Z,20230518T120000Z,20\n" +
      " 230519T120000Z,20230520T120000Z"

    val currentDateTime = UtcDateTime("20230520T120000Z")

    val nextDateTime = recurEventManager.getNextAfterDateTime(currentDateTime.dateTime, input)

    assertNull(nextDateTime)
  }
}
