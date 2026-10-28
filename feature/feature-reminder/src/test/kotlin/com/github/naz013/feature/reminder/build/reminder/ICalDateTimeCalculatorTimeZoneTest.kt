package com.github.naz013.feature.reminder.build.reminder

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.ICalCountBuilderItem
import com.github.naz013.feature.reminder.build.ICalFrequencyBuilderItem
import com.github.naz013.feature.reminder.build.ICalStartDateBuilderItem
import com.github.naz013.feature.reminder.build.ICalStartTimeBuilderItem
import com.github.naz013.feature.reminder.build.TimeZoneBuilderItem
import com.github.naz013.feature.reminder.build.bi.ProcessedBuilderItems
import com.github.naz013.feature.reminder.build.timezone.TimeZoneCatalog
import com.github.naz013.icalendar.DateTimeStartTag
import com.github.naz013.icalendar.FreqType
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.RuleMap
import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.UtcDateTime
import com.github.naz013.testing.BaseTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class ICalDateTimeCalculatorTimeZoneTest : BaseTest() {
  private val iCalendarApi = mockk<ICalendarApi>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val berlin = ZoneId.of("Europe/Berlin")
  private val ruleMapSlot = slot<RuleMap>()

  private lateinit var calculator: ICalDateTimeCalculator

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.getCurrentDateTime() } returns NOW
    every { dateTimeManager.getCurrentDateTime(berlin) } returns NOW.plusHours(2)
    every { iCalendarApi.createObject(capture(ruleMapSlot)) } returns "RRULE:FREQ=DAILY;COUNT=3"
    every { iCalendarApi.generate(any()) } returns listOf(UtcDateTime(NOW.plusDays(1)))
    calculator = ICalDateTimeCalculator(iCalendarApi, dateTimeManager)
  }

  @Test
  fun `writes the fixed zone id into DTSTART`() {
    calculator(itemsOf(zoneItem("Europe/Berlin")))

    val start = ruleMapSlot.captured.map[TagType.DTSTART] as DateTimeStartTag
    assertEquals("Europe/Berlin", start.timeZoneId)
  }

  @Test
  fun `keeps DTSTART floating without a zone item`() {
    calculator(itemsOf())

    val start = ruleMapSlot.captured.map[TagType.DTSTART] as DateTimeStartTag
    assertNull(start.timeZoneId)
  }

  @Test
  fun `compares generated occurrences with now in the fixed zone`() {
    calculator(itemsOf(zoneItem("Europe/Berlin")))

    verify { dateTimeManager.getCurrentDateTime(berlin) }
  }

  private fun itemsOf(vararg extra: BuilderItem<*>) =
    ProcessedBuilderItems(
      listOf(
        ICalStartDateBuilderItem(title = "sd", description = null, formatter = mockk(relaxed = true)).apply {
          modifier.update(NOW.toLocalDate())
        },
        ICalStartTimeBuilderItem(title = "st", description = null, formatter = mockk(relaxed = true)).apply {
          modifier.update(NOW.toLocalTime())
        },
        ICalFrequencyBuilderItem(title = "f", description = null, formatter = mockk(relaxed = true)).apply {
          modifier.update(FreqType.DAILY)
        },
        ICalCountBuilderItem(title = "c", description = null, formatter = mockk(relaxed = true)).apply {
          modifier.update(3)
        },
      ) + extra,
    )

  private fun zoneItem(zoneId: String) =
    TimeZoneBuilderItem(title = "z", description = null, catalog = TimeZoneCatalog("Device")).apply {
      modifier.update(zoneId)
    }

  private companion object {
    val NOW: LocalDateTime = LocalDateTime.of(2026, 7, 24, 10, 0)
  }
}
