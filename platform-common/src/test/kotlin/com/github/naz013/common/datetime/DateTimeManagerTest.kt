package com.github.naz013.common.datetime

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.DateTimePreferences
import com.github.naz013.datecalc.NowDateTimeProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.Locale

class DateTimeManagerTest {
  private val nowDateTimeProvider = mockk<NowDateTimeProvider>()
  private val dateTimePreferences = mockk<DateTimePreferences>()
  private lateinit var manager: DateTimeManager

  @Before
  fun setUp() {
    every { dateTimePreferences.is24HourFormat } returns true
    every { dateTimePreferences.locale } returns Locale.US
    manager = DateTimeManager(nowDateTimeProvider, dateTimePreferences)
  }

  @Test
  fun `doNotDisturbRange returns zero range when from equals to`() {
    val range = manager.doNotDisturbRange("22:00", "22:00")

    assertEquals(0L, range.first)
    assertEquals(0L, range.last)
  }

  @Test
  fun `doNotDisturbRange returns zero range for an unparsable time`() {
    val range = manager.doNotDisturbRange("not-a-time", "22:00")

    assertEquals(0L, range.first)
    assertEquals(0L, range.last)
  }

  @Test
  fun `doNotDisturbRange spans into the next day when the window wraps midnight`() {
    val range = manager.doNotDisturbRange("22:00", "08:00")

    val spanMillis = range.last - range.first
    assertEquals(10 * DateTimeManager.HOUR, spanMillis)
  }

  @Test
  fun `doNotDisturbRange stays within the same day when the window does not wrap`() {
    val range = manager.doNotDisturbRange("08:00", "22:00")

    val spanMillis = range.last - range.first
    assertEquals(14 * DateTimeManager.HOUR, spanMillis)
  }

  @Test
  fun `toLocalTime parses the standard HH-mm pattern`() {
    val time = manager.toLocalTime("09:05")

    assertEquals(LocalTime.of(9, 5), time)
  }

  @Test
  fun `toLocalTime falls back to the single-digit pattern`() {
    val time = manager.toLocalTime("9:5")

    assertEquals(LocalTime.of(9, 5), time)
  }

  @Test
  fun `toLocalTime returns null for an unparsable string`() {
    assertNull(manager.toLocalTime("not-a-time"))
  }

  @Test
  fun `to24HourString formats a time with leading zeros`() {
    assertEquals("09:05", manager.to24HourString(LocalTime.of(9, 5)))
  }

  @Test
  fun `isSameDay ignores the year`() {
    val birthDate = LocalDate.of(1990, 6, 17)
    val current = LocalDate.of(2023, 6, 17)

    assertTrue(manager.isSameDay(birthDate, current))
  }

  @Test
  fun `isSameDay is false when day or month differ`() {
    val birthDate = LocalDate.of(1990, 6, 17)
    val current = LocalDate.of(2023, 6, 18)

    assertFalse(manager.isSameDay(birthDate, current))
  }

  @Test
  fun `localDayOfWeekToOld maps Sunday to one and keeps Monday through Saturday shifted by one`() {
    assertEquals(1, manager.localDayOfWeekToOld(org.threeten.bp.DayOfWeek.SUNDAY))
    assertEquals(2, manager.localDayOfWeekToOld(org.threeten.bp.DayOfWeek.MONDAY))
    assertEquals(7, manager.localDayOfWeekToOld(org.threeten.bp.DayOfWeek.SATURDAY))
  }

  @Test
  fun `parseBirthdayDate round trips through formatBirthdayDate`() {
    val date = LocalDate.of(1994, 6, 17)

    val formatted = manager.formatBirthdayDate(date)
    val parsed = manager.parseBirthdayDate(formatted)

    assertEquals(date, parsed)
  }

  @Test
  fun `parseBirthdayDate returns null for an unparsable string`() {
    assertNull(manager.parseBirthdayDate("not-a-date"))
  }

  @Test
  fun `findBirthdayDate tries every legacy format until one matches`() {
    assertEquals(LocalDate.of(1994, 6, 17), manager.findBirthdayDate("1994-06-17"))
    assertEquals(LocalDate.of(1994, 6, 17), manager.findBirthdayDate("19940617"))
    assertEquals(LocalDate.of(1994, 6, 17), manager.findBirthdayDate("1994.06.17"))
  }

  @Test
  fun `findBirthdayDate returns null when no format matches`() {
    assertNull(manager.findBirthdayDate("not-a-date-at-all"))
  }

  @Test
  fun `getGmtFromDateTime round trips through fromGmtToLocal`() {
    val dateTime = LocalDateTime.of(2023, 6, 17, 9, 30, 0)

    val gmt = manager.getGmtFromDateTime(dateTime)
    val restored = manager.fromGmtToLocal(gmt)

    assertEquals(dateTime, restored)
  }

  @Test
  fun `fromGmtToLocal returns null for an empty string`() {
    assertNull(manager.fromGmtToLocal(""))
    assertNull(manager.fromGmtToLocal(null))
  }

  @Test
  fun `localToUtc round trips through utcToLocal`() {
    val dateTime = LocalDateTime.of(2026, 7, 22, 9, 30, 0)

    val utc = manager.localToUtc(dateTime)
    val restored = manager.utcToLocal(utc)

    assertEquals(dateTime, restored)
  }

  @Test
  fun `utcToLocal round trips through localToUtc`() {
    val dateTime = LocalDateTime.of(2026, 7, 22, 9, 30, 0)

    val local = manager.utcToLocal(dateTime)
    val restored = manager.localToUtc(local)

    assertEquals(dateTime, restored)
  }

  @Test
  fun `getMillisToBirthdayTime returns the gap to today's birthday time when it has not passed yet`() {
    val today = LocalDate.of(2023, 6, 17)
    val now = LocalDateTime.of(today, LocalTime.of(9, 0))
    every { nowDateTimeProvider.nowDateTime() } returns now
    every { nowDateTimeProvider.nowDate() } returns today
    every { nowDateTimeProvider.nowTime() } returns LocalTime.of(9, 0)
    every { dateTimePreferences.birthdayTime } returns "10:00"

    val millis = manager.getMillisToBirthdayTime()

    assertEquals(DateTimeManager.HOUR, millis)
  }
}
