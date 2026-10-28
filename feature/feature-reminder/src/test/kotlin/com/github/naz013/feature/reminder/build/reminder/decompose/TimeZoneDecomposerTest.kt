package com.github.naz013.feature.reminder.build.reminder.decompose

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.reminder.build.DateBuilderItem
import com.github.naz013.feature.reminder.build.TimeBuilderItem
import com.github.naz013.feature.reminder.build.TimeZoneBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId

class TimeZoneDecomposerTest {
  private val biFactory = mockk<BiFactory>()

  @Test
  fun `restores the zone item for a fixed reminder`() = runBlocking {
    coEvery { biFactory.createWithValue(BiType.TIME_ZONE, "Europe/Berlin", TimeZoneBuilderItem::class.java) } returns null

    TimeZoneDecomposer(biFactory)(reminder("Europe/Berlin"))

    coVerify { biFactory.createWithValue(BiType.TIME_ZONE, "Europe/Berlin", TimeZoneBuilderItem::class.java) }
  }

  @Test
  fun `adds no zone item for a floating reminder`() = runBlocking {
    assertTrue(TimeZoneDecomposer(biFactory)(reminder(null)).isEmpty())
  }

  @Test
  fun `adds no zone item for a countdown reminder`() = runBlocking {
    val countdown = reminder("Europe/Berlin").copy(recurrence = RecurrenceRule.Countdown(after = 1000L))

    assertTrue(TimeZoneDecomposer(biFactory)(countdown).isEmpty())
  }

  @Test
  fun `shows the date and time as wall-clock in the reminder zone`() = runBlocking {
    val berlin = ZoneId.of("Europe/Berlin")
    val dateTimeManager = mockk<DateTimeManager>()
    every { dateTimeManager.utcToLocal(UTC, berlin) } returns LocalDateTime.of(2026, 7, 1, 9, 0)
    coEvery { biFactory.createWithValue(BiType.DATE, LocalDate.of(2026, 7, 1), DateBuilderItem::class.java) } returns null
    coEvery { biFactory.createWithValue(BiType.TIME, LocalTime.of(9, 0), TimeBuilderItem::class.java) } returns null

    ByDateDecomposer(dateTimeManager, biFactory)(reminder("Europe/Berlin"))

    coVerify { biFactory.createWithValue(BiType.DATE, LocalDate.of(2026, 7, 1), DateBuilderItem::class.java) }
    coVerify { biFactory.createWithValue(BiType.TIME, LocalTime.of(9, 0), TimeBuilderItem::class.java) }
  }

  private fun reminder(zoneId: String?) =
    ReminderV2(schedule = ReminderSchedule(startDateTime = UTC, eventDateTime = UTC), timeZoneId = zoneId)

  private companion object {
    val UTC: LocalDateTime = LocalDateTime.of(2026, 7, 1, 7, 0)
  }
}
