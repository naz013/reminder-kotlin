package com.github.naz013.domain.habit

import com.github.naz013.domain.reminder.v2.SyncMetadata
import org.threeten.bp.LocalDateTime
import java.util.UUID

data class Habit(
  val id: String = UUID.randomUUID().toString(),
  val name: String = "",
  val description: String? = null,
  val frequency: HabitFrequency = HabitFrequency.DAILY,
  /** Check-ins needed per [frequency] period (per day for DAILY, per week for WEEKLY). */
  val targetCount: Int = 1,
  val color: Int = 0,
  /** Opaque icon index, same convention as `Routine.icon`; null means no icon selected. */
  val icon: Int? = null,
  val isPinned: Boolean = false,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
  val sync: SyncMetadata = SyncMetadata()
)
