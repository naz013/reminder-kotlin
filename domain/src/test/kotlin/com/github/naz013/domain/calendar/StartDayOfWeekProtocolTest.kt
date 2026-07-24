package com.github.naz013.domain.calendar

import org.junit.Assert.assertEquals
import org.junit.Test

class StartDayOfWeekProtocolTest {

  @Test
  fun `getForCalendar maps zero to seven`() {
    val protocol = StartDayOfWeekProtocol(0)

    assertEquals(7, protocol.getForCalendar())
  }

  @Test
  fun `getForCalendar returns the raw value when it is not zero`() {
    val protocol = StartDayOfWeekProtocol(3)

    assertEquals(3, protocol.getForCalendar())
  }

  @Test
  fun `getForDatePicker returns the value shifted by one`() {
    val protocol = StartDayOfWeekProtocol(0)

    assertEquals(1, protocol.getForDatePicker())
  }
}
