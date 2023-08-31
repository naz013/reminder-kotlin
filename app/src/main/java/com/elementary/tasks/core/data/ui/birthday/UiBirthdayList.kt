package com.elementary.tasks.core.data.ui.birthday

data class UiBirthdayList(
  val uuId: String,
  val name: String = "",
  val number: String = "",
  val birthdayDate: String = "",
  val birthdayDateFormatted: String = "",
  val ageFormatted: String = "",
  val remainingTimeFormatted: String = "",
  val nextBirthdayDateFormatted: String = "",
  val nextBirthdayDate: Long = 0L // Milliseconds
)
