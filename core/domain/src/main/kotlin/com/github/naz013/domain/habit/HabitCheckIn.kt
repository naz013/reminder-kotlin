package com.github.naz013.domain.habit

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/** One habit's progress for one local day; ([habitId], [date]) is the identity. */
data class HabitCheckIn(
  val habitId: String,
  val date: LocalDate,
  val count: Int = 0,
  val isCompleted: Boolean = false,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime
)
