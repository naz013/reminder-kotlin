package com.elementary.tasks.core.utils.datetime

import org.threeten.bp.LocalTime

object ScheduleTimes {
  val MORNING: LocalTime = LocalTime.of(0, 0)
  val NOON: LocalTime = LocalTime.of(12, 0)
  val EVENING: LocalTime = LocalTime.of(18, 0)
}
