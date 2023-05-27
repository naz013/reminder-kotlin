package com.elementary.tasks.core.utils.datetime.recurrence.builder

import org.junit.Assert.*
import org.junit.Test

class LineNormalizerTest {

  private val lineNormalizer = LineNormalizer()

  @Test
  fun testEmptyList() {
    val lines = emptyList<String>()

    val result = lineNormalizer.normalize(lines)

    assertEquals(emptyList<String>(), result)
  }

  @Test
  fun testListWithOneShortString() {
    val lines = listOf(
      "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5"
    )

    val result = lineNormalizer.normalize(lines)

    assertEquals(lines, result)
  }

  @Test
  fun testListWithOneLongString() {
    val lines = listOf(
      "RRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,SA"
    )

    val result = lineNormalizer.normalize(lines)

    val expectedLines = listOf(
      "RRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,",
      "\n SA"
    )
    assertEquals(expectedLines, result)
  }

  @Test
  fun testListWithOneShortAndLongLinesString() {
    val lines = listOf(
      "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5",
      "RRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,SA",
      "RRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,SA"
    )

    val result = lineNormalizer.normalize(lines)

    val expectedLines = listOf(
      "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5",
      "\nRRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,",
      "\n SA",
      "\nRRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,",
      "\n SA"
    )
    assertEquals(expectedLines, result)
  }

  @Test
  fun testListWithOneExtraLongString() {
    val lines = listOf(
      "RRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,SA:RRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,SA:RRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,SA"
    )

    val result = lineNormalizer.normalize(lines)

    val expectedLines = listOf(
      "RRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,",
      "\n SA:RRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,",
      "\n  FR,SA:RRULE:FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,",
      "\n   TH,FR,SA"
    )
    assertEquals(expectedLines, result)
  }
}
