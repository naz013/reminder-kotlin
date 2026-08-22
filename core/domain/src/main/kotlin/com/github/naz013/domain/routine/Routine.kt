package com.github.naz013.domain.routine

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime
import java.util.UUID

data class Routine(
  @SerializedName("id")
  val id: String = UUID.randomUUID().toString(),
  @SerializedName("title")
  val title: String = "",
  @SerializedName("description")
  val description: String? = null,
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("isPinned")
  val isPinned: Boolean = false,
  /** Index into `RoutineIconSet.ALL` (`ui-routine`) - null means no icon selected. An opaque
   * index rather than a drawable resource id so this model stays free of any UI/resource
   * dependency; only the fixed order of that list gives the index meaning. */
  @SerializedName("icon")
  val icon: Int? = null,
  @SerializedName("steps")
  val steps: List<RoutineStep> = emptyList(),
  @SerializedName("autoAdvance")
  val autoAdvance: Boolean = true,
  @SerializedName("soundAlertsEnabled")
  val soundAlertsEnabled: Boolean = true,
  @SerializedName("recurrence")
  val recurrence: RecurrenceRule? = null,
  @SerializedName("lastResetAt")
  val lastResetAt: LocalDateTime? = null,
  @SerializedName("createdAt")
  val createdAt: LocalDateTime,
  @SerializedName("updatedAt")
  val updatedAt: LocalDateTime,
  @SerializedName("sync")
  val sync: SyncMetadata = SyncMetadata()
) {
  val totalDurationSeconds: Int
    get() = steps.sumOf { it.durationSeconds }

  val sortedSteps: List<RoutineStep>
    get() = steps.sortedWith(RoutineStepComparator)
}
