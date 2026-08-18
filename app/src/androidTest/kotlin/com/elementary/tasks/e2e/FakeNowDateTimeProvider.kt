package com.elementary.tasks.e2e

import com.github.naz013.datecalc.NowDateTimeProvider
import org.koin.core.module.Module
import org.koin.dsl.module
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Test double for [NowDateTimeProvider] that lets a test pin "today" to a fixed [LocalDate] so
 * date-relative recurrence calculations (day-of-month/day-of-year rollover across leap years and
 * short months) are deterministic instead of depending on the real device clock. Time-of-day is
 * left as the real clock's - only the date is ever asserted on by callers of this fake.
 */
class FakeNowDateTimeProvider(private var date: LocalDate = LocalDate.now()) : NowDateTimeProvider {
  fun setDate(date: LocalDate) {
    this.date = date
  }

  override fun nowDate(): LocalDate = date

  override fun nowTime(): LocalTime = LocalTime.now()

  override fun nowDateTime(): LocalDateTime = LocalDateTime.of(date, LocalTime.now())
}

/** Overrides the production [NowDateTimeProvider] binding with [fakeNowDateTimeProvider]. */
fun testDateTimeModule(fakeNowDateTimeProvider: FakeNowDateTimeProvider): Module =
  module {
    single<NowDateTimeProvider> { fakeNowDateTimeProvider }
  }
