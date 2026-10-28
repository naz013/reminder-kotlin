package com.github.naz013.domain.reminder.v2

import java.util.TimeZone

private val knownTimeZoneIds: Set<String> by lazy { TimeZone.getAvailableIDs().toSet() }

/** Returns this IANA zone id if the runtime knows it, otherwise null (floating). Never throws. */
fun String?.toKnownTimeZoneIdOrNull(): String? {
  val id = this?.trim()
  return if (id.isNullOrEmpty() || id !in knownTimeZoneIds) null else id
}
