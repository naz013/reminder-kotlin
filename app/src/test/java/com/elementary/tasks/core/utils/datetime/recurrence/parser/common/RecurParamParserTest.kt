package com.elementary.tasks.core.utils.datetime.recurrence.parser.common

import com.elementary.tasks.core.utils.datetime.recurrence.ByDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByWeekNumberRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByYearDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.CountRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.core.utils.datetime.recurrence.FreqRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.core.utils.datetime.recurrence.IntervalRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.RecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.UntilRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class RecurParamParserTest {

  private val recurParamParser = RecurParamParser()

  @Test
  fun testParseEmptyParam() {
    val result = recurParamParser.parse("")

    assertEquals(emptyList<RecurParam>(), result)
  }

  @Test
  fun testParseWrongParam() {
    val result = recurParamParser.parse("RDATE;TZID=America/New_York:19970714T083000")

    assertEquals(emptyList<RecurParam>(), result)
  }

  @Test
  fun testParseSingleParam_Freq() {
    val result = recurParamParser.parse("FREQ=DAILY")

    val expected = listOf(
      FreqRecurParam(FreqType.DAILY)
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_Freq_WrongValue() {
    val result = recurParamParser.parse("FREQ=TIMELY")

    assertEquals(emptyList<RecurParam>(), result)
  }

  @Test
  fun testParseSingleParam_Count() {
    val result = recurParamParser.parse("COUNT=10")

    val expected = listOf(
      CountRecurParam(10)
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_Count_WrongInt() {
    val result = recurParamParser.parse("COUNT=10A")

    assertEquals(emptyList<RecurParam>(), result)
  }

  @Test
  fun testParseSingleParam_FreqAndCount() {
    val result = recurParamParser.parse("FREQ=DAILY;COUNT=10")

    val expected = listOf(
      FreqRecurParam(FreqType.DAILY),
      CountRecurParam(10),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_Until() {
    val result = recurParamParser.parse("UNTIL=19971224T000000Z")

    val expected = listOf(
      UntilRecurParam(UtcDateTime("19971224T000000Z")),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_FreqAndUntil() {
    val result = recurParamParser.parse("FREQ=DAILY;UNTIL=19971224T000000Z")

    val expected = listOf(
      FreqRecurParam(FreqType.DAILY),
      UntilRecurParam(UtcDateTime("19971224T000000Z")),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_Interval() {
    val result = recurParamParser.parse("INTERVAL=2")

    val expected = listOf(
      IntervalRecurParam(2),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_FreqAndInterval() {
    val result = recurParamParser.parse("FREQ=DAILY;INTERVAL=2")

    val expected = listOf(
      FreqRecurParam(FreqType.DAILY),
      IntervalRecurParam(2),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_FreqAndIntervalAndCount() {
    val result = recurParamParser.parse("FREQ=DAILY;INTERVAL=10;COUNT=5")

    val expected = listOf(
      FreqRecurParam(FreqType.DAILY),
      IntervalRecurParam(10),
      CountRecurParam(5),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_ByMonth() {
    val result = recurParamParser.parse("BYMONTH=1")

    val expected = listOf(
      ByMonthRecurParam(listOf(1)),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_ByDay() {
    val result = recurParamParser.parse("BYDAY=SU,MO,TU,WE,TH,FR,SA")

    val expected = listOf(
      ByDayRecurParam(
        listOf(
          DayValue("SU"),
          DayValue("MO"),
          DayValue("TU"),
          DayValue("WE"),
          DayValue("TH"),
          DayValue("FR"),
          DayValue("SA"),
        )
      ),
    )
    assertEquals(expected, result)
    assertEquals(7, (result.first() as ByDayRecurParam).value.count { it.isDefault })
  }

  @Test
  fun testParseSingleParam_ByDay_WrongValue() {
    val result = recurParamParser.parse("BYDAY=SB,MO,TU,WE,TH,FR,SA")

    assertEquals(emptyList<RecurParam>(), result)
  }

  @Test
  fun testParseSingleParam_FreqAndUntilAndMyMonthAndByDay() {
    val result = recurParamParser.parse("FREQ=YEARLY;UNTIL=20000131T140000Z;BYMONTH=1;BYDAY=SU,MO,TU,WE,TH,FR,SA")

    val expected = listOf(
      FreqRecurParam(FreqType.YEARLY),
      UntilRecurParam(UtcDateTime("20000131T140000Z")),
      ByMonthRecurParam(listOf(1)),
      ByDayRecurParam(
        listOf(
          DayValue("SU"),
          DayValue("MO"),
          DayValue("TU"),
          DayValue("WE"),
          DayValue("TH"),
          DayValue("FR"),
          DayValue("SA"),
        )
      ),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_ByMonthDay() {
    val result = recurParamParser.parse("BYMONTHDAY=-3")

    val expected = listOf(
      ByMonthDayRecurParam(listOf(-3)),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_FreqAndByMonthDay() {
    val result = recurParamParser.parse("FREQ=MONTHLY;BYMONTHDAY=-3")

    val expected = listOf(
      FreqRecurParam(FreqType.MONTHLY),
      ByMonthDayRecurParam(listOf(-3)),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_ByYearDay() {
    val result = recurParamParser.parse("BYYEARDAY=1,100,200")

    val expected = listOf(
      ByYearDayRecurParam(listOf(1, 100, 200)),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_FreqAndIntervalAndCountAndByYearDay() {
    val result = recurParamParser.parse("FREQ=YEARLY;INTERVAL=3;COUNT=10;BYYEARDAY=1,100,200")

    val expected = listOf(
      FreqRecurParam(FreqType.YEARLY),
      IntervalRecurParam(3),
      CountRecurParam(10),
      ByYearDayRecurParam(listOf(1, 100, 200)),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_FreqAndByDayNotDefault() {
    val result = recurParamParser.parse("FREQ=YEARLY;BYDAY=20MO")

    val expected = listOf(
      FreqRecurParam(FreqType.YEARLY),
      ByDayRecurParam(
        listOf(
          DayValue("20MO")
        )
      )
    )
    assertEquals(expected, result)
    assertEquals(false, (result[1] as ByDayRecurParam).value.first().isDefault)
  }

  @Test
  fun testParseSingleParam_ByWeekNumber() {
    val result = recurParamParser.parse("BYWEEKNO=20")

    val expected = listOf(
      ByWeekNumberRecurParam(listOf(20))
    )
    assertEquals(expected, result)
  }
}
