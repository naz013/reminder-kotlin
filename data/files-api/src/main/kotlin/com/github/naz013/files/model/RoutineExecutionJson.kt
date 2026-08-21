package com.github.naz013.files.model

import com.google.gson.annotations.SerializedName

/** Wire shape for [com.github.naz013.domain.routine.RoutineExecutionRecord]. Included in the
 * local encrypted backup archive (via [com.github.naz013.files.DataConverter], reused by
 * `BackupArchiveWriter`/`Reader`) but deliberately not cloud-synced as its own `DataType` - the
 * record has no `SyncMetadata`/`syncState`, same treatment as `EventHistoricalRecord`. */
data class RoutineExecutionJson(
  @SerializedName("schemaVersion")
  val schemaVersion: String = "v1.0",
  @SerializedName("id")
  val id: String,
  @SerializedName("routineId")
  val routineId: String,
  @SerializedName("executedAt")
  val executedAt: String,
  @SerializedName("totalTimeSpentSeconds")
  val totalTimeSpentSeconds: Int = 0,
  @SerializedName("completedStepIds")
  val completedStepIds: List<String> = emptyList(),
  @SerializedName("totalStepsCount")
  val totalStepsCount: Int = 0
)
