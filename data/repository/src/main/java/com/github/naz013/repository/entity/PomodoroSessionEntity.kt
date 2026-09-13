package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
  tableName = "PomodoroSession",
  indices = [
    Index(value = ["linkedReminderId"]),
    Index(value = ["startedAt"])
  ]
)
@Keep
internal data class PomodoroSessionEntity(
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  val linkedReminderId: String? = null,
  val startedAt: Long,
  val plannedFocusSeconds: Int = 0,
  val actualFocusSeconds: Int = 0,
  val wasCompleted: Boolean = false
)
