package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.repository.converters.RoutineStepsTypeConverter
import java.util.UUID

@Entity(
  tableName = "Routine",
  indices = [
    Index(value = ["isPinned"]),
    Index(value = ["syncState"])
  ]
)
@TypeConverters(RoutineStepsTypeConverter::class)
@Keep
internal data class RoutineEntity(
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  val title: String = "",
  val description: String? = null,
  val color: Int = 0,
  val isPinned: Boolean = false,
  val icon: String? = null,
  val steps: List<RoutineStep> = emptyList(),
  val autoAdvance: Boolean = true,
  val soundAlertsEnabled: Boolean = true,

  val recurrenceType: String,
  val recurrencePayload: String,

  val reminderId: String? = null,
  val lastResetAt: Long? = null,

  val createdAt: Long,
  val updatedAt: Long,

  val version: Long = 0L,
  val syncState: String
)
