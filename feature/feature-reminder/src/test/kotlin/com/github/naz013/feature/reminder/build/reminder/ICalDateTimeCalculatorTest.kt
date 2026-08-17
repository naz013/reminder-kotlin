package com.github.naz013.feature.reminder.build.reminder

import com.github.naz013.testing.BaseTest
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.ICalCountBuilderItem
import com.github.naz013.feature.reminder.build.ICalFrequencyBuilderItem
import com.github.naz013.feature.reminder.build.ICalIntervalBuilderItem
import com.github.naz013.feature.reminder.build.ICalStartDateBuilderItem
import com.github.naz013.feature.reminder.build.ICalStartTimeBuilderItem
import com.github.naz013.feature.reminder.build.ICalUntilDateBuilderItem
import com.github.naz013.feature.reminder.build.ICalUntilTimeBuilderItem
import com.github.naz013.feature.reminder.build.bi.ProcessedBuilderItems
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.icalendar.CountRecurParam
import com.github.naz013.icalendar.FreqType
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.IntervalRecurParam
import com.github.naz013.icalendar.RecurrenceRuleTag
import com.github.naz013.icalendar.RuleMap
import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.UntilRecurParam
import com.github.naz013.icalendar.UtcDateTime
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class ICalDateTimeCalculatorTest : BaseTest() {
  private val iCalendarApi = mockk<ICalendarApi>()
  private val dateTimeManager = mockk<DateTimeManager>()

  private lateinit var calculator: ICalDateTimeCalculator

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.getCurrentDateTime() } returns NOW
    calculator = ICalDateTimeCalculator(iCalendarApi, dateTimeManager)
  }

  @Test
  fun `missing start date and time produces no event data`() {
    val items = itemsOf(frequencyItem(), countItem(5))

    val result = calculator(items)

    assertNull(result)
  }

  @Test
  fun `no iCal items at all produces no event data`() {
    val result = calculator(ProcessedBuilderItems(emptyList()))

    assertNull(result)
  }

  @Test
  fun `createObject failure produces no event data instead of throwing`() {
    every { iCalendarApi.createObject(any()) } throws IllegalArgumentException("Count should be present in RRULE")

    val items = itemsOf(startDateItem(NOW.toLocalDate()), startTimeItem(NOW.toLocalTime()), frequencyItem())

    val result = calculator(items)

    assertNull(result)
  }

  @Test
  fun `a future generated occurrence is returned as the next event`() {
    every { iCalendarApi.createObject(any()) } returns "RRULE:FREQ=DAILY;COUNT=3"
    every { iCalendarApi.generate(any()) } returns
      listOf(
        UtcDateTime(NOW.minusDays(1)),
        UtcDateTime(NOW),
        UtcDateTime(NOW.plusDays(1)),
      )

    val items =
      itemsOf(startDateItem(NOW.toLocalDate()), startTimeItem(NOW.toLocalTime()), frequencyItem(), countItem(3))

    val result = calculator(items)

    assertEquals(NOW, result?.startDateTime)
    assertEquals("RRULE:FREQ=DAILY;COUNT=3", result?.recurObject)
  }

  @Test
  fun `an exhausted recurrence whose occurrences are all in the past produces no event data`() {
    every { iCalendarApi.createObject(any()) } returns "RRULE:FREQ=DAILY;COUNT=2"
    every { iCalendarApi.generate(any()) } returns
      listOf(
        UtcDateTime(NOW.minusDays(2)),
        UtcDateTime(NOW.minusDays(1)),
      )

    val items =
      itemsOf(startDateItem(NOW.toLocalDate()), startTimeItem(NOW.toLocalTime()), frequencyItem(), countItem(2))

    val result = calculator(items)

    assertNull(result)
  }

  @Test
  fun `an empty generated list produces no event data`() {
    every { iCalendarApi.createObject(any()) } returns "RRULE:FREQ=DAILY;COUNT=1"
    every { iCalendarApi.generate(any()) } returns emptyList()

    val items =
      itemsOf(startDateItem(NOW.toLocalDate()), startTimeItem(NOW.toLocalTime()), frequencyItem(), countItem(1))

    val result = calculator(items)

    assertNull(result)
  }

  @Test
  fun `generate throwing is treated the same as an empty list`() {
    every { iCalendarApi.createObject(any()) } returns "RRULE:FREQ=DAILY;COUNT=1"
    every { iCalendarApi.generate(any()) } throws RuntimeException("boom")

    val items =
      itemsOf(startDateItem(NOW.toLocalDate()), startTimeItem(NOW.toLocalTime()), frequencyItem(), countItem(1))

    val result = calculator(items)

    assertNull(result)
  }

  @Test
  fun `a complete until date and time is included as an UntilRecurParam`() {
    val ruleMapSlot = slot<RuleMap>()
    every { iCalendarApi.createObject(capture(ruleMapSlot)) } returns "RRULE:FREQ=DAILY;COUNT=1;UNTIL=..."
    every { iCalendarApi.generate(any()) } returns listOf(UtcDateTime(NOW))

    val until = NOW.plusDays(10)
    val items =
      itemsOf(
        startDateItem(NOW.toLocalDate()),
        startTimeItem(NOW.toLocalTime()),
        frequencyItem(),
        countItem(1),
        untilDateItem(until.toLocalDate()),
        untilTimeItem(until.toLocalTime()),
      )

    calculator(items)

    val rrule = ruleMapSlot.captured.map[TagType.RRULE] as RecurrenceRuleTag
    assertTrue(rrule.params.any { it is UntilRecurParam && it.value == UtcDateTime(until) })
  }

  @Test
  fun `an incomplete until date without a time is not included`() {
    val ruleMapSlot = slot<RuleMap>()
    every { iCalendarApi.createObject(capture(ruleMapSlot)) } returns "RRULE:FREQ=DAILY;COUNT=1"
    every { iCalendarApi.generate(any()) } returns listOf(UtcDateTime(NOW))

    val items =
      itemsOf(
        startDateItem(NOW.toLocalDate()),
        startTimeItem(NOW.toLocalTime()),
        frequencyItem(),
        countItem(1),
        untilDateItem(NOW.plusDays(10).toLocalDate()),
      )

    calculator(items)

    val rrule = ruleMapSlot.captured.map[TagType.RRULE] as RecurrenceRuleTag
    assertTrue(rrule.params.none { it is UntilRecurParam })
  }

  @Test
  fun `every selected iCal recur param is collected into the RRULE tag`() {
    val ruleMapSlot = slot<RuleMap>()
    every { iCalendarApi.createObject(capture(ruleMapSlot)) } returns "RRULE:FREQ=WEEKLY;INTERVAL=2;COUNT=4"
    every { iCalendarApi.generate(any()) } returns listOf(UtcDateTime(NOW))

    val items =
      itemsOf(
        startDateItem(NOW.toLocalDate()),
        startTimeItem(NOW.toLocalTime()),
        frequencyItem(FreqType.WEEKLY),
        intervalItem(2),
        countItem(4),
      )

    calculator(items)

    val rrule = ruleMapSlot.captured.map[TagType.RRULE] as RecurrenceRuleTag
    assertEquals(3, rrule.params.size)
    assertTrue(rrule.params.any { it is CountRecurParam && it.value == 4 })
    assertTrue(rrule.params.any { it is IntervalRecurParam && it.value == 2 })
  }

  private fun itemsOf(vararg items: BuilderItem<*>) = ProcessedBuilderItems(items.toList())

  private fun startDateItem(value: LocalDate) =
    ICalStartDateBuilderItem(title = "sd", description = null, formatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun startTimeItem(value: LocalTime) =
    ICalStartTimeBuilderItem(title = "st", description = null, formatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun untilDateItem(value: LocalDate) =
    ICalUntilDateBuilderItem(title = "ud", description = null, formatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun untilTimeItem(value: LocalTime) =
    ICalUntilTimeBuilderItem(title = "ut", description = null, formatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun frequencyItem(value: FreqType = FreqType.DAILY) =
    ICalFrequencyBuilderItem(title = "f", description = null, formatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun countItem(value: Int) =
    ICalCountBuilderItem(title = "c", description = null, formatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun intervalItem(value: Int) =
    ICalIntervalBuilderItem(title = "i", description = null, formatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  companion object {
    private val NOW: LocalDateTime = LocalDateTime.of(2026, 7, 24, 10, 0)
  }
}
