package com.elementary.tasks.reminder.build.bi

enum class BiGroup(val types: List<BiType> = emptyList()) {
  CORE,
  PARAMS,
  ACTION,
  ICAL(
    listOf(
      BiType.ICAL_START_DATE,
      BiType.ICAL_START_TIME,
      BiType.ICAL_FREQ,
      BiType.ICAL_INTERVAL,
      BiType.ICAL_COUNT,
      BiType.ICAL_UNTIL_DATE,
      BiType.ICAL_UNTIL_TIME,
      BiType.ICAL_BYMONTH,
      BiType.ICAL_BYDAY,
      BiType.ICAL_BYMONTHDAY,
      BiType.ICAL_BYHOUR,
      BiType.ICAL_BYMINUTE,
      BiType.ICAL_BYYEARDAY,
      BiType.ICAL_BYWEEKNO,
      BiType.ICAL_BYSETPOS,
      BiType.ICAL_WEEKSTART
    )
  ),
  EXTRA
}
