package com.elementary.tasks.core.utils.datetime

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.params.Prefs
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DateTimeManagerTest {

  private val prefs = mockk<Prefs>()
  private val textProvider = mockk<TextProvider>()
  private val language = mockk<Language>()
  private val nowDateTimeProvider = mockk<NowDateTimeProvider>()
  private val dateTimeManager = DateTimeManager(prefs, textProvider, language, nowDateTimeProvider)
  private val oldTimeUtil = OldTimeUtil()

  @Before
  fun setUp() {
    every { prefs.appLanguage } returns 1

    every { nowDateTimeProvider.nowDate() } returns LocalDate.now()
    every { nowDateTimeProvider.nowDateTime() } returns LocalDateTime.now()
    every { nowDateTimeProvider.nowTime() } returns LocalTime.now()
  }

  @Test
  fun testFutureBirthdayDate() {
    val nowDate = LocalDate.of(2023, 5, 8)
    val time = LocalTime.of(12, 0)
    val date = "1994-06-17"

    val nowDateTime = LocalDateTime.of(nowDate, LocalTime.now())

    every { nowDateTimeProvider.nowDate() } returns nowDate
    every { nowDateTimeProvider.nowDateTime() } returns nowDateTime

    val result = dateTimeManager.getFutureBirthdayDate(time, date)

    val expected = DateTimeManager.BirthDate(
      dateTime = LocalDateTime.of(LocalDate.of(2023, 6, 17), time),
      year = 1994
    )

    assertEquals(expected, result)
  }

  @Test
  fun testBirthdayDate() {
    val formatted = "1995-12-23"
    val localDate = dateTimeManager.parseBirthdayDate(formatted)

    assertEquals(LocalDate.of(1995, 12, 23), localDate)
    assertEquals(formatted, dateTimeManager.formatBirthdayDate(localDate!!))
  }

  @Test
  fun testNowToMillis() {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.set(2022, 11, 25, 15, 15, 15)
    calendar.set(Calendar.MILLISECOND, 0)

    val dateTime = LocalDateTime.of(2022, 12, 25, 15, 15, 15)

    assertEquals(
      calendar.timeInMillis,
      dateTimeManager.toMillis(dateTime)
    )
  }

  @Test
  fun testToGmtFormat() {
    val millis = getMillis(2022, 12, 25, 15, 15, 15)
    val localDateTime = getDateTime(2022, 12, 25, 15, 15, 15)

    assertEquals(toGmt(millis), dateTimeManager.getGmtFromDateTime(localDateTime))
  }

  @Test
  fun testGetNextMonthDayTime() {
    val dayOfMonth = 29
    val startTime = getDateTime(2022, 12, dayOfMonth, 14, 30)
    val reminder = Reminder(
      dayOfMonth = dayOfMonth,
      remindBefore = 0,
      eventTime = dateTimeManager.getGmtFromDateTime(startTime),
      repeatInterval = 1
    )

    val nowDateTime = getDateTime(2022, 12, dayOfMonth, 14, 30)
    val calculatedDateTime = dateTimeManager.getNewNextMonthDayTime(reminder, nowDateTime)

    val nowMillis = getMillis(2022, 12, dayOfMonth, 14, 30)
    val calculatedMillis = oldTimeUtil.getNextMonthDayTime(reminder, nowMillis)

    assertEquals(toGmt(calculatedMillis), dateTimeManager.getGmtFromDateTime(calculatedDateTime))
  }

  @Test
  fun testGetNextMonthDayTime2() {
    val dayOfMonth = 14
    val startTime = getDateTime(2022, 12, dayOfMonth, 14, 30)
    val reminder = Reminder(
      dayOfMonth = dayOfMonth,
      remindBefore = 0,
      eventTime = dateTimeManager.getGmtFromDateTime(startTime),
      repeatInterval = 2
    )

    val nowDateTime = getDateTime(2022, 12, dayOfMonth, 14, 30)
    val calculatedDateTime = dateTimeManager.getNewNextMonthDayTime(reminder, nowDateTime)

    val nowMillis = getMillis(2022, 12, dayOfMonth, 14, 30)
    val calculatedMillis = oldTimeUtil.getNextMonthDayTime(reminder, nowMillis)

    assertEquals(toGmt(calculatedMillis), dateTimeManager.getGmtFromDateTime(calculatedDateTime))
  }

  @Test
  fun testGetNextMonthDayTimeLastDay() {
    var reminder = Reminder(
      dayOfMonth = 0,
      remindBefore = 0,
      eventTime = dateTimeManager.getGmtFromDateTime(getDateTime(2022, 12, 31, 14, 30)),
      repeatInterval = 1
    )

    var calculatedDateTime = dateTimeManager.getNewNextMonthDayTime(
      reminder,
      getDateTime(2022, 12, 31, 14, 30)
    )
    reminder = reminder.copy(eventTime = dateTimeManager.getGmtFromDateTime(calculatedDateTime))
    assertEquals(
      dateTimeManager.getGmtFromDateTime(getDateTime(2023, 1, 31, 14, 30)),
      reminder.eventTime
    )

    calculatedDateTime = dateTimeManager.getNewNextMonthDayTime(
      reminder,
      getDateTime(2023, 1, 31, 14, 30)
    )
    reminder = reminder.copy(eventTime = dateTimeManager.getGmtFromDateTime(calculatedDateTime))
    assertEquals(
      dateTimeManager.getGmtFromDateTime(getDateTime(2023, 2, 28, 14, 30)),
      reminder.eventTime
    )
  }

  private fun getDateTime(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int = 0
  ): LocalDateTime {
    return LocalDateTime.of(year, month, day, hour, minute, second)
  }

  private fun getMillis(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int = 0
  ): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.set(year, month - 1, day, hour, minute, second)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
  }

  private fun toGmt(millis: Long): String {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)
    format.timeZone = TimeZone.getTimeZone("GMT")
    return try {
      format.format(Date(millis))
    } catch (e: Throwable) {
      ""
    }
  }
}
