package com.github.naz013.feature.common

import org.threeten.bp.LocalDateTime

fun LocalDateTime.minusMillis(millis: Long): LocalDateTime {
  return minusSeconds(millis / 1000L)
}

fun LocalDateTime.plusMillis(millis: Long): LocalDateTime {
  return plusSeconds(millis / 1000L)
}
