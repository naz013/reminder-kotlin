package com.elementary.tasks.core.utils.datetime

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class NowDateTimeProvider {
  fun nowDate(): LocalDate = LocalDate.now()
  fun nowTime(): LocalTime = LocalTime.now()
  fun nowDateTime(): LocalDateTime = LocalDateTime.now()
}
