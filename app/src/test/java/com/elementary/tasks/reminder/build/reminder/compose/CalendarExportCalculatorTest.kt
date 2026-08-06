package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarDurationBuilderItem
import com.elementary.tasks.reminder.build.bi.CalendarDuration
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.github.naz013.domain.reminder.v2.CalendarExportSettings
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarExportCalculatorTest : BaseTest() {
  private val calculator = CalendarExportCalculator()

  @Test
  fun `no calendar item produces no export settings`() {
    val result = calculator(itemsOf())

    assertNull(result)
  }

  @Test
  fun `calendar alone without a duration item exports with zero duration`() {
    val calendar = calendarItem(id = 7L)

    val result = calculator(itemsOf(calendar))

    assertEquals(CalendarExportSettings(calendarId = 7L, duration = 0L, allDay = false), result)
  }

  @Test
  fun `calendar and duration merge into one CalendarExportSettings without one overwriting the other`() {
    val calendar = calendarItem(id = 7L)
    val duration = durationItem(allDay = true, millis = 3_600_000L)

    val result = calculator(itemsOf(calendar, duration))

    assertEquals(CalendarExportSettings(calendarId = 7L, duration = 3_600_000L, allDay = true), result)
  }

  private fun itemsOf(vararg items: BuilderItem<*>) = ProcessedBuilderItems(items.toList()).typeMap

  private fun calendarItem(id: Long) =
    GoogleCalendarBuilderItem(title = "cal", description = null).apply {
      modifier.update(GoogleCalendarUtils.CalendarItem(name = "Cal", id = id))
    }

  private fun durationItem(allDay: Boolean, millis: Long) =
    GoogleCalendarDurationBuilderItem(title = "dur", description = null, calendarDurationFormatter = mockk(relaxed = true)).apply {
      modifier.update(CalendarDuration(allDay = allDay, millis = millis))
    }
}
