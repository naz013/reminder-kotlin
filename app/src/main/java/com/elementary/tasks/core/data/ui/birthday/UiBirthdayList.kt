package com.elementary.tasks.core.data.ui.birthday

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

data class UiBirthdayList(
  val uuId: String,
  val name: String = "",
  val number: String = "",
  val birthdayDate: LocalDate? = null,
  val birthdayDateFormatted: String = "",
  val ageFormatted: String = "",
  val remainingTimeFormatted: String? = null,
  val color: Int,
  val contrastColor: Int,
  val nextBirthdayDateFormatted: String = "",
  val nextBirthdayTimeFormatted: String = "",
  val nextBirthdayDate: LocalDateTime,
  val nextBirthdayDateMillis: Long = 0L // Milliseconds
)
