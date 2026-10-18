package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
  tableName = "Habit",
  indices = [
    Index(value = ["isPinned"]),
    Index(value = ["syncState"])
  ]
)
@Keep
internal data class HabitEntity(
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  val name: String = "",
  val description: String? = null,
  val frequency: String,
  val targetCount: Int = 1,
  val color: Int = 0,
  val icon: Int? = null,
  val isPinned: Boolean = false,
  val createdAt: Long,
  val updatedAt: Long,
  val version: Long = 0L,
  val syncState: String
)
