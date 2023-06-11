package com.elementary.tasks.core.utils.datetime.recurrence

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

data class UtcDateTime(
  private val utcDateTime: String // Format 19971210T080000Z
) : Buildable {

  var dateTime: LocalDateTime? = null
    private set

  init {
    parse()
  }

  constructor(localDateTime: LocalDateTime) : this("") {
    this.dateTime = localDateTime
  }

  override fun buildString(): String {
    return dateTime?.format(LOCAL_FORMATTER) ?: ""
  }

  private fun parse() {
    if (utcDateTime.isEmpty()) return
    runCatching {
      dateTime = if (utcDateTime.endsWith(UTC)) {
        LocalDateTime.parse(utcDateTime, UTC_FORMATTER)
      } else {
        LocalDateTime.parse(utcDateTime, LOCAL_FORMATTER)
      }
    }
  }

  override fun toString(): String {
    return "UtcDateTime(localDateTime=$dateTime)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as UtcDateTime

    return dateTime == other.dateTime
  }

  override fun hashCode(): Int {
    return dateTime?.hashCode() ?: 0
  }

  companion object {
    const val UTC = "Z"
    private val UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.US)
    private val LOCAL_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss", Locale.US)
  }
}
