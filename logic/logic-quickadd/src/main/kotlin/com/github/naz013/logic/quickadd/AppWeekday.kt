package com.github.naz013.logic.quickadd

import org.threeten.bp.DayOfWeek

/** The app's reminder domain uses 0=Sunday..6=Saturday (see
 * [com.github.naz013.domain.reminder.v2.RecurrenceRule.RelativeMonthly]), while [DayOfWeek] is
 * ISO-8601's 1=Monday..7=Sunday. These convert between the two. */
internal fun DayOfWeek.toAppWeekday(): Int = if (this == DayOfWeek.SUNDAY) 0 else value

internal fun appWeekdayToIso(appWeekday: Int): Int = if (appWeekday == 0) 7 else appWeekday
