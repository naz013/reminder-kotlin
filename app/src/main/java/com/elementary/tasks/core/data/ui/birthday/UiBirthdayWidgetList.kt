package com.elementary.tasks.core.data.ui.birthday

data class UiBirthdayWidgetList(
  val uuId: String,
  val name: String = "",
  val ageFormattedAndBirthdayDate: String = "",
  val remainingTimeFormatted: String = "",
  val nextBirthdayDate: Long = 0L// Milliseconds
)
