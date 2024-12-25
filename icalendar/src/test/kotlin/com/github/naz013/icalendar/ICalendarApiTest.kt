package com.github.naz013.icalendar

import com.github.naz013.icalendar.builder.RuleBuilder
import com.github.naz013.icalendar.parser.TagParser
import org.junit.Assert
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ICalendarApiTest {

  private val ruleBuilder = RuleBuilder()
  private val tagParser = TagParser()

  private val iCalendarApi = ICalendarApiImpl(
    ruleBuilder = ruleBuilder,
    tagParser = tagParser
  )

  @Test
  fun testCreateObject_May_Daily() {
    val startDateTime = LocalDateTime.of(2023, 5, 16, 12, 0, 0)

    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
      put(
        TagType.RRULE,
        RecurrenceRuleTag(
          listOf(
            FreqRecurParam(FreqType.DAILY),
            IntervalRecurParam(1),
            CountRecurParam(5)
          )
        )
      )
    }

    val result = iCalendarApi.createObject(RuleMap(map))

    println(result)

    val expected = "DTSTART:20230516T120000\n" +
      "RRULE:FREQ=DAILY;INTERVAL=1;COUNT=5\n" +
      "RDATE;VALUE=DATE-TIME:20230516T120000,20230517T120000,20230518T120000,20230\n" +
      " 519T120000,20230520T120000"

    Assert.assertEquals(expected, result)
  }

  @Test
  fun testGenerate_May_Daily() {
    val startDateTime = LocalDateTime.of(2023, 5, 16, 12, 0, 0)

    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
      put(
        TagType.RRULE,
        RecurrenceRuleTag(
          listOf(
            FreqRecurParam(FreqType.DAILY),
            IntervalRecurParam(1),
            CountRecurParam(5)
          )
        )
      )
    }
    val ruleMap = RuleMap(map)
    val result = iCalendarApi.generate(ruleMap)

    val expected = listOf(
      UtcDateTime(startDateTime),
      UtcDateTime(startDateTime.plusDays(1)),
      UtcDateTime(startDateTime.plusDays(2)),
      UtcDateTime(startDateTime.plusDays(3)),
      UtcDateTime(startDateTime.plusDays(4))
    )

    println(result)

    Assert.assertEquals(expected.size, result.size)
    Assert.assertTrue(result.containsAll(expected))
  }

  @Test
  fun testCreateObject_December_Weekly() {
    val startDateTime = LocalDateTime.of(2023, 12, 16, 12, 0, 0)

    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
      put(
        TagType.RRULE,
        RecurrenceRuleTag(
          listOf(
            FreqRecurParam(FreqType.WEEKLY),
            IntervalRecurParam(1),
            CountRecurParam(5)
          )
        )
      )
    }

    val result = iCalendarApi.createObject(RuleMap(map))

    println(result)

    val expected = "DTSTART:20231216T120000\n" +
      "RRULE:FREQ=WEEKLY;INTERVAL=1;COUNT=5\n" +
      "RDATE;VALUE=DATE-TIME:20231216T120000,20231223T120000,20231230T120000,20240\n" +
      " 106T120000,20240113T120000"

    Assert.assertEquals(expected, result)
  }

  @Test
  fun testGenerate_December_Weekly() {
    val startDateTime = LocalDateTime.of(2023, 12, 16, 12, 0, 0)

    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
      put(
        TagType.RRULE,
        RecurrenceRuleTag(
          listOf(
            FreqRecurParam(FreqType.WEEKLY),
            IntervalRecurParam(1),
            CountRecurParam(5)
          )
        )
      )
    }
    val ruleMap = RuleMap(map)
    val result = iCalendarApi.generate(ruleMap)

    val expected = listOf(
      UtcDateTime(startDateTime),
      UtcDateTime(startDateTime.plusWeeks(1)),
      UtcDateTime(startDateTime.plusWeeks(2)),
      UtcDateTime(startDateTime.plusWeeks(3)),
      UtcDateTime(startDateTime.plusWeeks(4))
    )

    println(result)

    Assert.assertEquals(expected.size, result.size)
    Assert.assertTrue(result.containsAll(expected))
  }

  @Test
  fun testCreateObject_May_Monthly_WithDayOfMonth() {
    val startDateTime = LocalDateTime.of(2023, 5, 16, 12, 0, 0)

    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
      put(
        TagType.RRULE,
        RecurrenceRuleTag(
          listOf(
            FreqRecurParam(FreqType.MONTHLY),
            IntervalRecurParam(1),
            ByMonthDayRecurParam(listOf(20, 21)),
            ByHourRecurParam(listOf(15)),
            CountRecurParam(5)
          )
        )
      )
    }

    val result = iCalendarApi.createObject(RuleMap(map))

    println(result)

    val expected = "DTSTART:20230516T120000\n" +
      "RRULE:FREQ=MONTHLY;INTERVAL=1;BYMONTHDAY=20,21;BYHOUR=15;COUNT=5\n" +
      "RDATE;VALUE=DATE-TIME:20230520T150000,20230521T150000,20230620T150000,20230\n" +
      " 621T150000,20230720T150000"

    Assert.assertEquals(expected, result)
  }

  @Test
  fun testGenerate_May_Monthly_WithDayOfMonth() {
    val startDateTime = LocalDateTime.of(2023, 5, 16, 12, 0, 0)

    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
      put(
        TagType.RRULE,
        RecurrenceRuleTag(
          listOf(
            FreqRecurParam(FreqType.MONTHLY),
            IntervalRecurParam(1),
            ByMonthDayRecurParam(listOf(20, 21)),
            ByHourRecurParam(listOf(15)),
            CountRecurParam(5)
          )
        )
      )
    }
    val ruleMap = RuleMap(map)
    val result = iCalendarApi.generate(ruleMap)

    val expectedDateTimeStart = startDateTime.withHour(15).withDayOfMonth(20)

    val expected = listOf(
      UtcDateTime(expectedDateTimeStart),
      UtcDateTime(expectedDateTimeStart.plusDays(1)),
      UtcDateTime(expectedDateTimeStart.plusMonths(1)),
      UtcDateTime(expectedDateTimeStart.plusMonths(1).plusDays(1)),
      UtcDateTime(expectedDateTimeStart.plusMonths(2))
    )

    println(result)

    Assert.assertEquals(expected.size, result.size)
    Assert.assertTrue(result.containsAll(expected))
  }
}
