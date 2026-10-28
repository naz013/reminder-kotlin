package com.github.naz013.feature.reminder.build.timezone

import com.github.naz013.feature.reminder.build.bi.TimeZoneModifier
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime

class TimeZoneCatalogTest {
  private val catalog =
    TimeZoneCatalog(
      deviceLabel = "Device time zone",
      availableIds = { setOf("Europe/Berlin", "America/New_York", "Asia/Kolkata", "Etc/GMT+5", "UTC", "NotAZone") },
      nowProvider = { Instant.parse("2026-07-01T00:00:00Z") },
    )

  @Test
  fun `lists the device option first and orders zones by current offset`() {
    val options = catalog.options("")

    assertNull(options.first().id)
    assertEquals(listOf("America/New_York", "UTC", "Europe/Berlin", "Asia/Kolkata"), options.drop(1).map { it.id })
  }

  @Test
  fun `leaves out Etc zones and ids that are not real zones`() {
    val ids = catalog.options("").map { it.id }

    assertFalse(ids.contains("Etc/GMT+5"))
    assertFalse(ids.contains("NotAZone"))
  }

  @Test
  fun `formats the current UTC offset including daylight saving`() {
    val offsets = catalog.options("").associate { it.id to it.offset }

    assertEquals("UTC+02:00", offsets["Europe/Berlin"])
    assertEquals("UTC-04:00", offsets["America/New_York"])
    assertEquals("UTC+05:30", offsets["Asia/Kolkata"])
  }

  @Test
  fun `filters by zone name ignoring case`() {
    assertEquals(listOf("Europe/Berlin"), catalog.options("BERLIN").map { it.id })
  }

  @Test
  fun `matches names written with spaces instead of underscores`() {
    assertEquals(listOf("America/New_York"), catalog.options("new york").map { it.id })
  }

  @Test
  fun `filters by offset text`() {
    assertEquals(listOf("Asia/Kolkata"), catalog.options("+05:30").map { it.id })
  }

  @Test
  fun `keeps the device option when the query matches its label`() {
    val options = catalog.options("device")

    assertEquals(listOf<String?>(null), options.map { it.id })
  }

  @Test
  fun `returns nothing for a query that matches no zone`() {
    assertTrue(catalog.options("zzz").isEmpty())
  }

  @Test
  fun `labels a zone with its offset and falls back to the device label otherwise`() {
    assertEquals("Europe/Berlin (UTC+02:00)", catalog.label("Europe/Berlin"))
    assertEquals("Device time zone", catalog.label(null))
    assertEquals("Device time zone", catalog.label("Bogus/Zone"))
  }

  @Test
  fun `modifier writes a valid zone id into the reminder`() {
    val modifier = TimeZoneModifier(catalog)
    modifier.update("Europe/Berlin")

    assertEquals("Europe/Berlin", modifier.putInto(reminder()).timeZoneId)
  }

  @Test
  fun `modifier writes null for the device option and for an unknown id`() {
    val modifier = TimeZoneModifier(catalog)

    assertNull(modifier.putInto(reminder("Europe/Berlin")).timeZoneId)
    modifier.update("Bogus/Zone")
    assertNull(modifier.putInto(reminder("Europe/Berlin")).timeZoneId)
  }

  private fun reminder(zoneId: String? = null) =
    ReminderV2(schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 1, 7, 0)), timeZoneId = zoneId)

  @Test
  fun `only date based and recurring rules support a zone`() {
    val unsupported =
      listOf(
        com.github.naz013.domain.reminder.v2.RecurrenceRule.Countdown(after = 1000L),
        com.github.naz013.domain.reminder.v2.RecurrenceRule.LocationEnter,
        com.github.naz013.domain.reminder.v2.RecurrenceRule.LocationExit,
      )

    assertTrue(unsupported.none { it.supportsTimeZone() })
    assertTrue(com.github.naz013.domain.reminder.v2.RecurrenceRule.Once.supportsTimeZone())
    assertTrue(com.github.naz013.domain.reminder.v2.RecurrenceRule.ICalendar("RRULE").supportsTimeZone())
  }
}
