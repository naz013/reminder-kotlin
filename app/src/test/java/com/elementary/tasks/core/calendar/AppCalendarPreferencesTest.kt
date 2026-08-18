package com.elementary.tasks.core.calendar

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.calendar.CalendarViewMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class AppCalendarPreferencesTest {
  private val prefs = mockk<Prefs>(relaxed = true)
  private val preferences = AppCalendarPreferences(prefs)

  @Test
  fun `reads the stored view mode by its enum name`() {
    every { prefs.calendarViewMode } returns "SEVEN_DAY"

    assertEquals(CalendarViewMode.SEVEN_DAY, preferences.lastViewMode)
  }

  @Test
  fun `falls back to MONTH when the stored value is empty`() {
    every { prefs.calendarViewMode } returns ""

    assertEquals(CalendarViewMode.MONTH, preferences.lastViewMode)
  }

  @Test
  fun `falls back to MONTH when the stored value is unrecognized`() {
    every { prefs.calendarViewMode } returns "SOMETHING_ELSE"

    assertEquals(CalendarViewMode.MONTH, preferences.lastViewMode)
  }

  @Test
  fun `writes the view mode as its enum name`() {
    preferences.lastViewMode = CalendarViewMode.THREE_DAY

    verify { prefs.calendarViewMode = "THREE_DAY" }
  }
}
