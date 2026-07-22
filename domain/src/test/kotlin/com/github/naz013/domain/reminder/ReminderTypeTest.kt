package com.github.naz013.domain.reminder

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderTypeTest {

  @Test
  fun `constructor from base and kind sums their values`() {
    val type = ReminderType(ReminderType.Base.DATE, ReminderType.Kind.CALL)

    assertTrue(type.isSameAs(11))
  }

  @Test
  fun `hasCallAction is true only for the call kind`() {
    val call = ReminderType(ReminderType.Base.DATE, ReminderType.Kind.CALL)
    val sms = ReminderType(ReminderType.Base.DATE, ReminderType.Kind.SMS)

    assertTrue(call.hasCallAction())
    assertFalse(sms.hasCallAction())
  }

  @Test
  fun `hasSmsAction is true only for the sms kind`() {
    val sms = ReminderType(ReminderType.Base.WEEKDAY, ReminderType.Kind.SMS)

    assertTrue(sms.hasSmsAction())
    assertFalse(sms.hasCallAction())
  }

  @Test
  fun `isDateTime is true only for the date base`() {
    val dateType = ReminderType(ReminderType.Base.DATE.value)
    val timerType = ReminderType(ReminderType.Base.TIMER.value)

    assertTrue(dateType.isDateTime())
    assertFalse(timerType.isDateTime())
  }

  @Test
  fun `isCountdown is true only for the timer base`() {
    val timerType = ReminderType(ReminderType.Base.TIMER.value)

    assertTrue(timerType.isCountdown())
    assertFalse(timerType.isDateTime())
  }

  @Test
  fun `baseOf is true for any kind offset within the ten-block`() {
    val type = ReminderType(ReminderType.Base.MONTHLY.value + ReminderType.Kind.LINK.value)

    assertTrue(type.baseOf(ReminderType.Base.MONTHLY))
    assertFalse(type.baseOf(ReminderType.Base.YEARLY))
  }

  @Test
  fun `isGpsType is true for location and place bases`() {
    val locationIn = ReminderType(ReminderType.Base.LOCATION_IN.value)
    val locationOut = ReminderType(ReminderType.Base.LOCATION_OUT.value)
    val place = ReminderType(ReminderType.Base.PLACE.value)
    val date = ReminderType(ReminderType.Base.DATE.value)

    assertTrue(locationIn.isGpsType())
    assertTrue(locationOut.isGpsType())
    assertTrue(place.isGpsType())
    assertFalse(date.isGpsType())
  }

  @Test
  fun `isSameAs compares against the raw int value`() {
    val type = ReminderType(42)

    assertTrue(type.isSameAs(42))
    assertFalse(type.isSameAs(43))
  }
}
