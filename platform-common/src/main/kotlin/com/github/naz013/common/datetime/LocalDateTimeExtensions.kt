package com.github.naz013.common.datetime

import org.threeten.bp.LocalDateTime

fun LocalDateTime.minusMillis(millis: Long): LocalDateTime {
  return minusSeconds(millis / 1000L)
}

fun LocalDateTime.plusMillis(millis: Long): LocalDateTime {
  return plusSeconds(millis / 1000L)
}
