package com.github.naz013.feature.calendar.timeline

data class TimelineScreenState(
  val title: String = "",
  /** 24 hour labels ("00:00" .. "23:00"), formatted for the user's 12/24-hour preference. */
  val hourLabels: List<String> = emptyList(),
)
