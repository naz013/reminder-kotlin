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

  @Test
  fun testGenerate_MissingDtStart_ReturnsEmptyList() {
    val map = mutableMapOf<TagType, Tag>().apply {
      put(
        TagType.RRULE,
        RecurrenceRuleTag(
          listOf(FreqRecurParam(FreqType.DAILY), CountRecurParam(5))
        )
      )
    }

    val result = iCalendarApi.generate(RuleMap(map))

    Assert.assertTrue(result.isEmpty())
  }

  @Test
  fun testGenerate_MissingRrule_ReturnsEmptyList() {
    val startDateTime = LocalDateTime.of(2023, 5, 16, 12, 0, 0)
    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
    }

    val result = iCalendarApi.generate(RuleMap(map))

    Assert.assertTrue(result.isEmpty())
  }

  @Test(expected = IllegalArgumentException::class)
  fun testCreateObject_MissingCount_Throws() {
    val startDateTime = LocalDateTime.of(2023, 5, 16, 12, 0, 0)
    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
      put(
        TagType.RRULE,
        RecurrenceRuleTag(listOf(FreqRecurParam(FreqType.DAILY), IntervalRecurParam(1)))
      )
    }

    iCalendarApi.createObject(RuleMap(map))
  }

  @Test(expected = IllegalArgumentException::class)
  fun testCreateObject_MissingDtStart_Throws() {
    val map = mutableMapOf<TagType, Tag>().apply {
      put(
        TagType.RRULE,
        RecurrenceRuleTag(listOf(FreqRecurParam(FreqType.DAILY), CountRecurParam(5)))
      )
    }

    iCalendarApi.createObject(RuleMap(map))
  }

  @Test
  fun testGenerate_Weekly_ByDay_Interval() {
    // 2023-05-15 is a Monday.
    val startDateTime = LocalDateTime.of(2023, 5, 15, 9, 0, 0)

    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
      put(
        TagType.RRULE,
        RecurrenceRuleTag(
          listOf(
            FreqRecurParam(FreqType.WEEKLY),
            IntervalRecurParam(2),
            ByDayRecurParam(listOf(DayValue(Day.MO), DayValue(Day.WE))),
            CountRecurParam(4)
          )
        )
      )
    }

    val result = iCalendarApi.generate(RuleMap(map))

    val expected = listOf(
      UtcDateTime(startDateTime),
      UtcDateTime(startDateTime.plusDays(2)),
      UtcDateTime(startDateTime.plusWeeks(2)),
      UtcDateTime(startDateTime.plusWeeks(2).plusDays(2))
    )

    Assert.assertEquals(expected, result)
  }

  @Test
  fun testParseObject_RoundTripsCreatedRrule() {
    val startDateTime = LocalDateTime.of(2023, 5, 16, 12, 0, 0)

    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
      put(
        TagType.RRULE,
        RecurrenceRuleTag(
          listOf(FreqRecurParam(FreqType.DAILY), IntervalRecurParam(1), CountRecurParam(5))
        )
      )
    }

    val created = iCalendarApi.createObject(RuleMap(map))
    val parsed = iCalendarApi.parseObject(created)

    Assert.assertEquals(map[TagType.DTSTART], parsed?.map?.get(TagType.DTSTART))
    Assert.assertEquals(map[TagType.RRULE], parsed?.map?.get(TagType.RRULE))
  }
}
