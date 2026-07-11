package com.elementary.tasks.calendar.monthview.monthgrid

import com.elementary.tasks.core.utils.params.Prefs
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class MonthGridFactoryTest {
  private fun factory(startDay: Int): MonthGridFactory {
    val prefs = mockk<Prefs>()
    every { prefs.startDay } returns startDay
    return MonthGridFactory(prefs)
  }

  @Test
  fun `grid always contains 42 cells`() {
    val grid = factory(startDay = 0).buildGrid(LocalDate.of(2026, 7, 1))
    assertEquals(42, grid.size)
  }

  @Test
  fun `first cell falls on Sunday when start day is Sunday`() {
    val grid = factory(startDay = 0).buildGrid(LocalDate.of(2026, 7, 1))
    assertEquals(7, grid.first().date.dayOfWeek.value)
  }

  @Test
  fun `first cell falls on Monday when start day is Monday`() {
    val grid = factory(startDay = 1).buildGrid(LocalDate.of(2026, 7, 1))
    assertEquals(1, grid.first().date.dayOfWeek.value)
  }

  @Test
  fun `days belonging to the requested month are flagged as current month`() {
    val monthDate = LocalDate.of(2026, 7, 1)
    val grid = factory(startDay = 0).buildGrid(monthDate)

    val currentMonthDays = grid.filter { it.isCurrentMonth }
    assertEquals(monthDate.lengthOfMonth(), currentMonthDays.size)
    assertEquals(0, currentMonthDays.count { it.date.monthValue != 7 || it.date.year != 2026 })
  }

  @Test
  fun `leading and trailing days from adjacent months are flagged as not current month`() {
    val monthDate = LocalDate.of(2026, 7, 1)
    val grid = factory(startDay = 0).buildGrid(monthDate)

    val nonCurrentMonthDays = grid.filterNot { it.isCurrentMonth }
    assertEquals(42 - monthDate.lengthOfMonth(), nonCurrentMonthDays.size)
  }

  @Test
  fun `only today's date is flagged isToday`() {
    val grid = factory(startDay = 0).buildGrid(LocalDate.now())
    val todayCells = grid.filter { it.isToday }
    assertEquals(1, todayCells.size)
    assertEquals(LocalDate.now(), todayCells.first().date)
  }
}
