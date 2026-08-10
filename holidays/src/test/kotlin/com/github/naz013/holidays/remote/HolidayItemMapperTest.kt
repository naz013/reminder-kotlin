package com.github.naz013.holidays.remote

import com.github.naz013.domain.PublicHoliday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDate

class HolidayItemMapperTest {

  @Test
  fun `maps every field, including name_local, from the raw Firestore map`() {
    val raw = mapOf(
      "name" to "New Year's Day",
      "name_local" to "Jour de l'An",
      "date" to "2026-01-01",
      "type" to "National",
      "location" to "Paris",
    )

    val result = raw.toPublicHolidayOrNull("FR")

    assertEquals(
      PublicHoliday(
        id = PublicHoliday.id("FR", LocalDate.of(2026, 1, 1), "New Year's Day"),
        countryCode = "FR",
        date = LocalDate.of(2026, 1, 1),
        name = "New Year's Day",
        nameLocal = "Jour de l'An",
        type = "National",
        location = "Paris",
      ),
      result,
    )
  }

  @Test
  fun `nameLocal, type and location default sensibly when absent`() {
    val raw = mapOf(
      "name" to "New Year's Day",
      "date" to "2026-01-01",
    )

    val result = raw.toPublicHolidayOrNull("US")

    assertEquals(null, result?.nameLocal)
    assertEquals("", result?.type)
    assertNull(result?.location)
  }

  @Test
  fun `returns null when name is missing or blank`() {
    assertNull(mapOf("date" to "2026-01-01").toPublicHolidayOrNull("US"))
    assertNull(mapOf("name" to "  ", "date" to "2026-01-01").toPublicHolidayOrNull("US"))
  }

  @Test
  fun `returns null when date is missing or unparseable`() {
    assertNull(mapOf("name" to "New Year's Day").toPublicHolidayOrNull("US"))
    assertNull(mapOf("name" to "New Year's Day", "date" to "not-a-date").toPublicHolidayOrNull("US"))
  }
}
