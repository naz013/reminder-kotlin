package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.repository.converters.ListStringTypeConverter
import java.util.UUID

@Entity(
  tableName = "RoutineExecution",
  indices = [
    Index(value = ["routineId"]),
    Index(value = ["executedAt"])
  ]
)
@TypeConverters(ListStringTypeConverter::class)
@Keep
internal data class RoutineExecutionEntity(
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  val routineId: String,
  val executedAt: Long,
  val totalTimeSpentSeconds: Int = 0,
  val completedStepIds: List<String> = emptyList(),
  val totalStepsCount: Int = 0
)
