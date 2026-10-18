package com.github.naz013.files.model

import com.google.gson.annotations.SerializedName

/** Wire shape for [com.github.naz013.domain.habit.HabitCheckIn]. Same treatment as
 * [RoutineExecutionJson]: carried by the local backup archive via the shared converter, not a
 * cloud-synced `DataType` of its own (it has no sync metadata). [date] is an ISO `yyyy-MM-dd`. */
data class HabitCheckInJson(
  @SerializedName("schemaVersion")
  val schemaVersion: String = "v1.0",
  @SerializedName("habitId")
  val habitId: String,
  @SerializedName("date")
  val date: String,
  @SerializedName("count")
  val count: Int = 0,
  @SerializedName("isCompleted")
  val isCompleted: Boolean = false,
  @SerializedName("createdAt")
  val createdAt: String,
  @SerializedName("updatedAt")
  val updatedAt: String
)
