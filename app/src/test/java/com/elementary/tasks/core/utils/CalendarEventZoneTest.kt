package com.elementary.tasks.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarEventZoneTest {
  @Test
  fun `uses reminder zone when valid`() {
    val id = CalendarEventZone.resolveId("Europe/Berlin", allDay = false, deviceZoneId = "America/New_York")

    assertEquals("Europe/Berlin", id)
  }

  @Test
  fun `uses device zone when reminder is floating`() {
    val id = CalendarEventZone.resolveId(null, allDay = false, deviceZoneId = "America/New_York")

    assertEquals("America/New_York", id)
  }

  @Test
  fun `uses device zone when reminder zone id is invalid`() {
    val id = CalendarEventZone.resolveId("Not/AZone", allDay = false, deviceZoneId = "Asia/Tokyo")

    assertEquals("Asia/Tokyo", id)
  }

  @Test
  fun `uses UTC for all day events`() {
    val id = CalendarEventZone.resolveId("Europe/Berlin", allDay = true, deviceZoneId = "Asia/Tokyo")

    assertEquals("UTC", id)
  }
}
