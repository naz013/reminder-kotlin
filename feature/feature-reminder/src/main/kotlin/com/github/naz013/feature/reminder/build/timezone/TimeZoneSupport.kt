package com.github.naz013.feature.reminder.build.timezone

import com.github.naz013.domain.reminder.v2.RecurrenceRule

/** Countdown is an elapsed duration and location rules have no time component, so neither carries a zone. */
internal fun RecurrenceRule.supportsTimeZone(): Boolean =
  when (this) {
    is RecurrenceRule.Countdown,
    RecurrenceRule.LocationEnter,
    RecurrenceRule.LocationExit,
    -> false

    else -> true
  }
