package com.github.naz013.datecalc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DateValidatorTest {
  private lateinit var validator: DateValidator

  @Before
  fun setUp() {
    validator = DateValidator()
  }

  @Test
  fun `isLegacyMonthValid accepts the zero-based range 0 to 11`() {
    assertTrue(validator.isLegacyMonthValid(0))
    assertTrue(validator.isLegacyMonthValid(11))
    assertFalse(validator.isLegacyMonthValid(12))
    assertFalse(validator.isLegacyMonthValid(-1))
  }

  @Test
  fun `isMonthValid accepts the one-based range 1 to 12`() {
    assertTrue(validator.isMonthValid(1))
    assertTrue(validator.isMonthValid(12))
    assertFalse(validator.isMonthValid(0))
    assertFalse(validator.isMonthValid(13))
  }
}
