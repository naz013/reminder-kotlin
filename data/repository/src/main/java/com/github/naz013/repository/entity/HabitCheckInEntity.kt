package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index

/** [date] is an ISO local date (`yyyy-MM-dd`) so lexicographic range queries match calendar order. */
@Entity(
  tableName = "HabitCheckIn",
  primaryKeys = ["habitId", "date"],
  indices = [Index(value = ["date"])]
)
@Keep
internal data class HabitCheckInEntity(
  val habitId: String,
  val date: String,
  val count: Int = 0,
  val isCompleted: Boolean = false,
  val createdAt: Long,
  val updatedAt: Long
)
