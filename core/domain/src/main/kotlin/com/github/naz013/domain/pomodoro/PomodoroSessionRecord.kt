package com.github.naz013.domain.pomodoro

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime
import java.util.UUID

/** A single completed-or-interrupted Pomodoro work interval. Breaks aren't logged - they don't
 * count as "focused minutes" for Streaks & Insights. Local-only history, same scope decision as
 * [com.github.naz013.domain.routine.RoutineExecutionRecord]: no `*Json` counterpart, not part of
 * cloud/local backup. */
data class PomodoroSessionRecord(
  @SerializedName("id")
  val id: String = UUID.randomUUID().toString(),
  @SerializedName("linkedReminderId")
  val linkedReminderId: String? = null,
  @SerializedName("startedAt")
  val startedAt: LocalDateTime,
  @SerializedName("plannedFocusSeconds")
  val plannedFocusSeconds: Int,
  @SerializedName("actualFocusSeconds")
  val actualFocusSeconds: Int,
  @SerializedName("wasCompleted")
  val wasCompleted: Boolean
)
