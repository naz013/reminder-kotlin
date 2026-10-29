package com.github.naz013.ui.common.datetime

import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.BirthdayDateCalculator
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.DateTimePreferences
import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.BirthdayEventType
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.ui.common.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import java.util.Locale

class ModelDateTimeFormatterEventTest {

  private val textProvider = mockk<TextProvider>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val dateTimePreferences = mockk<DateTimePreferences>()
  private val nowDateTimeProvider = mockk<NowDateTimeProvider>()
  private val birthdayDateCalculator = mockk<BirthdayDateCalculator>()
  private val nowDate = LocalDate.of(2026, 5, 1)

  private lateinit var formatter: ModelDateTimeFormatter

  @Before
  fun setUp() {
    every { dateTimePreferences.locale } returns Locale.ENGLISH
    every { nowDateTimeProvider.nowDate() } returns nowDate
    every { dateTimeManager.parseBirthdayDate(any()) } returns LocalDate.of(2000, 5, 1)
    every { birthdayDateCalculator.getAge(any(), any()) } returns 26
    every { textProvider.getText(R.string.x_years, "26") } returns "26 years"
    every { textProvider.getQuantityString(R.plurals.x_year_anniversary, 26) } returns "26-year anniversary"
    formatter =
      ModelDateTimeFormatter(
        textProvider,
        dateTimeManager,
        dateTimePreferences,
        nowDateTimeProvider,
        birthdayDateCalculator,
      )
  }

  @Test
  fun `returns plain years for birthday`() {
    val result = formatter.getEventSubtitle(birthday(BirthdayEventType.BIRTHDAY))

    assertEquals("26 years", result)
  }

  @Test
  fun `returns anniversary wording for anniversary`() {
    val result = formatter.getEventSubtitle(birthday(BirthdayEventType.ANNIVERSARY))

    assertEquals("26-year anniversary", result)
  }

  @Test
  fun `returns plain years for memorial`() {
    val result = formatter.getEventSubtitle(birthday(BirthdayEventType.MEMORIAL))

    assertEquals("26 years", result)
  }

  @Test
  fun `returns label before years for other with custom label`() {
    val result = formatter.getEventSubtitle(birthday(BirthdayEventType.OTHER, label = " Moved in "))

    assertEquals("Moved in | 26 years", result)
  }

  @Test
  fun `returns only years for other with blank label`() {
    val result = formatter.getEventSubtitle(birthday(BirthdayEventType.OTHER, label = "  "))

    assertEquals("26 years", result)
  }

  @Test
  fun `ignores custom label for non other types`() {
    val result = formatter.getEventLabel(birthday(BirthdayEventType.MEMORIAL, label = "Label"))

    assertNull(result)
  }

  @Test
  fun `returns null age for every type when year is ignored`() {
    BirthdayEventType.entries.forEach {
      assertNull(formatter.getEventAgeFormatted(birthday(it, ignoreYear = true)))
    }
  }

  @Test
  fun `returns only label for other when year is ignored`() {
    val result =
      formatter.getEventSubtitle(birthday(BirthdayEventType.OTHER, label = "Moved in", ignoreYear = true))

    assertEquals("Moved in", result)
  }

  @Test
  fun `returns null subtitle for birthday when year is ignored`() {
    val result = formatter.getEventSubtitle(birthday(BirthdayEventType.BIRTHDAY, ignoreYear = true))

    assertNull(result)
  }

  private fun birthday(
    type: BirthdayEventType,
    label: String? = null,
    ignoreYear: Boolean = false,
  ) = Birthday(
    name = "Name",
    date = "2000-05-01",
    ignoreYear = ignoreYear,
    eventType = type,
    customLabel = label,
    syncState = SyncState.Synced,
  )
}
