package com.github.naz013.domain.routine

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime
import java.util.UUID

data class RoutineExecutionRecord(
  @SerializedName("id")
  val id: String = UUID.randomUUID().toString(),
  @SerializedName("routineId")
  val routineId: String,
  @SerializedName("executedAt")
  val executedAt: LocalDateTime,
  @SerializedName("totalTimeSpentSeconds")
  val totalTimeSpentSeconds: Int,
  @SerializedName("completedStepIds")
  val completedStepIds: List<String> = emptyList(),
  @SerializedName("totalStepsCount")
  val totalStepsCount: Int
) {
  val completedStepsCount: Int
    get() = completedStepIds.size
}
