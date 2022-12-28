package com.elementary.tasks.core.utils.datetime

import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.params.Prefs
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DateTimeManagerTest {

  private val prefs = mockk<Prefs>()
  private val textProvider = mockk<TextProvider>()
  private val dateTimeManager = DateTimeManager(prefs, textProvider)

  @Before
  fun setUp() {
    every { prefs.appLanguage } returns 1
  }

  @Test
  fun testBirthdayDate() {
    val formatted = "1995-12-23"
    val localDate = dateTimeManager.parseBirthdayDate(formatted)

    assertEquals(LocalDate.of(1995, 12, 23), localDate)
    assertEquals(formatted, dateTimeManager.formatBirthdayDate(localDate))
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
  fun testGmtToLocal() {
    val gmtDateTime = "2023-01-20 15:05:00.000+0000"

    assertEquals(
      LocalDateTime.of(2023, 1, 20, 16, 5, 0),
      dateTimeManager.fromGmtToLocal(gmtDateTime)
    )
  }

  @Test
  fun testToGmtFormat() {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.set(2022, 11, 25, 15, 15, 15)
    calendar.set(Calendar.MILLISECOND, 0)

    val localDateTime = LocalDateTime.of(2022, 12, 25, 15, 15, 15)

    assertEquals(toGmt(calendar), dateTimeManager.getGmtFromDateTime(localDateTime))
  }

  private fun toGmt(calendar: Calendar): String {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)
    format.timeZone = TimeZone.getTimeZone("GMT")
    return try {
      format.format(calendar.time)
    } catch (e: Throwable) {
      ""
    }
  }
}
