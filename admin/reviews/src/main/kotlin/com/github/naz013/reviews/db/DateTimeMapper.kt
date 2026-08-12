package com.github.naz013.reviews.db

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

internal fun LocalDateTime.toEpochMillis(): Long {
  return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

internal fun Long.toLocalDateTime(): LocalDateTime {
  return LocalDateTime.ofInstant(
    Instant.ofEpochMilli(this),
    ZoneId.systemDefault()
  )
}
