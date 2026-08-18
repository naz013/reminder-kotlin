package com.github.naz013.datecalc

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

interface NowDateTimeProvider {
  fun nowDate(): LocalDate
  fun nowTime(): LocalTime
  fun nowDateTime(): LocalDateTime
}

class NowDateTimeProviderImpl : NowDateTimeProvider {
  override fun nowDate(): LocalDate = LocalDate.now()
  override fun nowTime(): LocalTime = LocalTime.now()
  override fun nowDateTime(): LocalDateTime = LocalDateTime.now()
}
