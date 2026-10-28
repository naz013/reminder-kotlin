package com.github.naz013.feature.reminder.build.logic.builderstate

import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.RuleMap
import com.github.naz013.icalendar.UtcDateTime
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class ReminderPredictionCalculatorTimeZoneTest {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val iCalendarApi = mockk<ICalendarApi>()
  private val textProvider = mockk<TextProvider>()
  private val berlin = ZoneId.of("Europe/Berlin")
  private val ruleMap = RuleMap(emptyMap())

  private lateinit var calculator: ReminderPredictionCalculator

  @Before
  fun setUp() {
    every { textProvider.getString(any()) } returns ""
    every { dateTimeManager.utcToLocal(EVENT) } returns EVENT
    every { dateTimeManager.getCurrentDateTime() } returns NOW
    every { dateTimeManager.getDateTime(any()) } answers { "dt:" + firstArg<LocalDateTime>() }
    every { iCalendarApi.parseObject(any()) } returns ruleMap
    calculator = ReminderPredictionCalculator(dateTimeManager, iCalendarApi, textProvider)
  }

  @Test
  fun `fixed zone picks occurrences by now in that zone and prints them in device time`() {
    val wallClock = LocalDateTime.of(2026, 7, 24, 9, 0)
    every { dateTimeManager.getCurrentDateTime(berlin) } returns wallClock.plusHours(1)
    every { iCalendarApi.generate(ruleMap) } returns listOf(UtcDateTime(wallClock), UtcDateTime(wallClock.plusDays(1)))
    every { dateTimeManager.localToUtc(wallClock.plusDays(1), berlin) } returns wallClock.plusDays(1).minusHours(2)
    every { dateTimeManager.utcToLocal(wallClock.plusDays(1).minusHours(2)) } returns DEVICE_TIME

    val message = (calculator(reminder("Europe/Berlin")) as ReminderPrediction.SuccessPrediction).message

    assertTrue(message, message.contains("dt:$DEVICE_TIME"))
    assertTrue(message, !message.contains("dt:${wallClock.plusDays(1)}"))
  }

  @Test
  fun `floating reminder compares with device now and prints entries unchanged`() {
    every { iCalendarApi.generate(ruleMap) } returns listOf(UtcDateTime(NOW.plusDays(1)))

    val message = (calculator(reminder(null)) as ReminderPrediction.SuccessPrediction).message

    assertTrue(message, message.contains("dt:${NOW.plusDays(1)}"))
    verify(exactly = 0) { dateTimeManager.localToUtc(any(), any()) }
  }

  private fun reminder(zoneId: String?) =
    ReminderV2(
      recurrence = RecurrenceRule.ICalendar("RRULE"),
      schedule = ReminderSchedule(startDateTime = EVENT, eventDateTime = EVENT),
      timeZoneId = zoneId,
    )

  private companion object {
    val NOW: LocalDateTime = LocalDateTime.of(2026, 7, 20, 10, 0)
    val EVENT: LocalDateTime = LocalDateTime.of(2026, 7, 25, 10, 0)
    val DEVICE_TIME: LocalDateTime = LocalDateTime.of(2026, 7, 25, 3, 0)
  }
}
