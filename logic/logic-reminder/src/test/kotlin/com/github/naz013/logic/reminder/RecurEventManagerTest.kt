package com.github.naz013.logic.reminder

import com.github.naz013.icalendar.DateTimeStartTag
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.ParamValueType
import com.github.naz013.icalendar.RecurrenceDateTimeTag
import com.github.naz013.icalendar.RuleMap
import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.UtcDateTime
import com.github.naz013.icalendar.ValueParam
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class RecurEventManagerTest {
  private val api = mockk<ICalendarApi>()
  private val manager = RecurEventManager(api)
  private val originalZone = TimeZone.getDefault()

  @Before
  fun setUp() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
  }

  @After
  fun tearDown() {
    TimeZone.setDefault(originalZone)
  }

  private fun givenRdate(
    zoneId: String?,
    vararg values: LocalDateTime,
  ) {
    val rdate = RecurrenceDateTimeTag(ValueParam(ParamValueType.DATE_TIME), values.map { UtcDateTime(it) }, zoneId)
    every { api.parseObject("obj") } returns RuleMap(mapOf(TagType.RDATE to rdate))
  }

  @Test
  fun `finds next in the fixed zone of a tagged list`() {
    val first = LocalDateTime.of(2026, 10, 18, 9, 0)
    givenRdate("Europe/Berlin", first, first.plusWeeks(1), first.plusWeeks(2))

    val next = manager.getNextAfterDateTime(first.plusWeeks(1), "obj", ZoneId.of("Europe/Berlin"))

    assertEquals(first.plusWeeks(2), next)
  }

  @Test
  fun `finds next in device zone for floating list and floating reminder`() {
    val first = LocalDateTime.of(2026, 10, 25, 9, 0)
    givenRdate(null, first, first.plusWeeks(1))

    assertEquals(first.plusWeeks(1), manager.getNextAfterDateTime(first, "obj"))
    assertNull(manager.getNextAfterDateTime(first.plusWeeks(1), "obj"))
  }

  @Test
  fun `converts between caller zone and list zone`() {
    val first = LocalDateTime.of(2026, 10, 18, 9, 0)
    givenRdate("Europe/Berlin", first, first.plusWeeks(1))
    val tokyoFirst = LocalDateTime.of(2026, 10, 18, 16, 0)

    val next = manager.getNextAfterDateTime(tokyoFirst, "obj", ZoneId.of("Asia/Tokyo"))

    assertEquals(LocalDateTime.of(2026, 10, 25, 17, 0), next)
  }

  @Test
  fun `regenerate sets the zone on DTSTART and rebuilds`() {
    val start = LocalDateTime.of(2026, 10, 18, 9, 0)
    every { api.parseObject("obj") } returns RuleMap(mapOf(TagType.DTSTART to DateTimeStartTag(UtcDateTime(start))))
    val captured = slot<RuleMap>()
    every { api.createObject(capture(captured)) } returns "rebuilt"

    val result = manager.regenerate("obj", ZoneId.of("Europe/Berlin"))

    assertEquals("rebuilt", result)
    assertEquals(
      "Europe/Berlin",
      captured.captured.getTagOrNull<DateTimeStartTag>(TagType.DTSTART)?.timeZoneId,
    )
  }

  @Test
  fun `regenerate returns null when parse throws`() {
    every { api.parseObject("obj") } throws IllegalStateException("bad")

    assertNull(manager.regenerate("obj", null))
  }

  @Test
  fun `regenerate returns null when rebuild throws`() {
    val start = LocalDateTime.of(2026, 10, 18, 9, 0)
    every { api.parseObject("obj") } returns RuleMap(mapOf(TagType.DTSTART to DateTimeStartTag(UtcDateTime(start))))
    every { api.createObject(any()) } throws IllegalArgumentException("Count should be present in RRULE")

    assertNull(manager.regenerate("obj", null))
  }
}
