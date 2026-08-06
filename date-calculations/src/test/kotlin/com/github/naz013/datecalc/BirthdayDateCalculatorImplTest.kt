package com.github.naz013.datecalc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class BirthdayDateCalculatorImplTest {
  private lateinit var calculator: BirthdayDateCalculator

  @Before
  fun setup() {
    calculator = BirthdayDateCalculatorImpl()
  }

  @Test
  fun `getNextOccurrence returns this year when birthday has not passed`() {
    val birthDate = LocalDate.of(1994, 6, 17)
    val time = LocalTime.of(12, 0)
    val nowDateTime = LocalDateTime.of(2023, 5, 8, 15, 0)

    val result = calculator.getNextOccurrence(birthDate, time, ignoreYear = false, showedYear = 0, nowDateTime = nowDateTime)

    assertEquals(LocalDateTime.of(2023, 6, 17, 12, 0), result)
  }

  @Test
  fun `getNextOccurrence rolls forward a year when birthday already passed`() {
    val birthDate = LocalDate.of(1994, 6, 17)
    val time = LocalTime.of(12, 0)
    val nowDateTime = LocalDateTime.of(2020, 6, 17, 13, 0)

    val result = calculator.getNextOccurrence(birthDate, time, ignoreYear = false, showedYear = 0, nowDateTime = nowDateTime)

    assertEquals(LocalDateTime.of(2021, 6, 17, 12, 0), result)
  }

  @Test
  fun `getNextOccurrence with ignoreYear does not roll forward until showedYear catches up`() {
    val birthDate = LocalDate.of(2022, 7, 17)
    val time = LocalTime.of(12, 0)
    val nowDateTime = LocalDateTime.of(2023, 5, 8, 15, 0)

    val result = calculator.getNextOccurrence(birthDate, time, ignoreYear = true, showedYear = 0, nowDateTime = nowDateTime)

    assertEquals(LocalDateTime.of(2023, 7, 17, 12, 0), result)
  }

  @Test
  fun `getNextOccurrence does not throw for Feb 29 birthday in a non-leap current year`() {
    val birthDate = LocalDate.of(1996, 2, 29)
    val time = LocalTime.of(9, 0)
    val nowDateTime = LocalDateTime.of(2023, 3, 1, 0, 0) // 2023 is not a leap year

    val result = calculator.getNextOccurrence(birthDate, time, ignoreYear = false, showedYear = 0, nowDateTime = nowDateTime)

    // Birthday already passed (clamped to Feb 28 in 2023), so it should roll to 2024 (a leap year) and land on Feb 29
    assertEquals(LocalDateTime.of(2024, 2, 29, 9, 0), result)
  }

  @Test
  fun `getOccurrenceWindow returns to Feb 29 once the window reaches a leap year`() {
    val birthDate = LocalDate.of(1996, 2, 29)
    val time = LocalTime.of(9, 0)

    // 2022, 2023 are not leap years; 2024 is
    val result = calculator.getOccurrenceWindow(birthDate, time, occurrenceCount = 2, fromYear = 2022)

    assertEquals(3, result.size)
    assertEquals(LocalDate.of(2022, 2, 28), result[0].toLocalDate())
    assertEquals(LocalDate.of(2023, 2, 28), result[1].toLocalDate())
    assertEquals(LocalDate.of(2024, 2, 29), result[2].toLocalDate())
  }

  @Test
  fun `isBirthdayOn returns true when today matches the birthday`() {
    val today = LocalDate.of(2023, 6, 17)

    val result = calculator.isBirthdayOn(birthMonth1Based = 6, birthDay = 17, targetDate = today, daysBefore = 0)

    assertTrue(result)
  }

  @Test
  fun `isBirthdayOn returns true when today matches the shifted before-date`() {
    val today = LocalDate.of(2023, 6, 15)

    val result = calculator.isBirthdayOn(birthMonth1Based = 6, birthDay = 17, targetDate = today, daysBefore = 2)

    assertTrue(result)
  }

  @Test
  fun `isBirthdayOn returns false for a non-matching date`() {
    val today = LocalDate.of(2023, 6, 18)

    val result = calculator.isBirthdayOn(birthMonth1Based = 6, birthDay = 17, targetDate = today, daysBefore = 0)

    assertFalse(result)
  }

  @Test
  fun `isBirthdayOn does not throw when today's day does not exist in the birthday month`() {
    // Today is the 31st, target birth month (April) only has 30 days
    val today = LocalDate.of(2023, 5, 31)

    val result = calculator.isBirthdayOn(birthMonth1Based = 4, birthDay = 30, targetDate = today, daysBefore = 0)

    assertFalse(result)
  }

  @Test
  fun `getAge computes whole years between birth date and now`() {
    val birthDate = LocalDate.of(1995, 12, 23)
    val nowDate = LocalDate.of(2020, 6, 15)

    val result = calculator.getAge(birthDate, nowDate)

    assertEquals(24, result)
  }
}
