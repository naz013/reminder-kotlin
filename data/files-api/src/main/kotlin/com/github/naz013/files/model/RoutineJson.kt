package com.github.naz013.files.model

import com.google.gson.annotations.SerializedName

data class RoutineJson(
  @SerializedName("schemaVersion")
  val schemaVersion: String = "v1.0",
  @SerializedName("id")
  val id: String,
  @SerializedName("title")
  val title: String = "",
  @SerializedName("description")
  val description: String? = null,
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("isPinned")
  val isPinned: Boolean = false,
  @SerializedName("icon")
  val icon: String? = null,
  @SerializedName("steps")
  val steps: List<RoutineStepJson> = emptyList(),
  @SerializedName("autoAdvance")
  val autoAdvance: Boolean = true,
  @SerializedName("soundAlertsEnabled")
  val soundAlertsEnabled: Boolean = true,
  @SerializedName("recurrenceType")
  val recurrenceType: String = "NONE",
  @SerializedName("recurrencePayload")
  val recurrencePayload: String = "",
  @SerializedName("reminderId")
  val reminderId: String? = null,
  @SerializedName("lastResetAt")
  val lastResetAt: String? = null,
  @SerializedName("createdAt")
  val createdAt: String,
  @SerializedName("updatedAt")
  val updatedAt: String,
  @SerializedName("versionId")
  val version: Long = 0L
)

data class RoutineStepJson(
  @SerializedName("id")
  val id: String,
  @SerializedName("title")
  val title: String = "",
  @SerializedName("description")
  val description: String? = null,
  @SerializedName("durationSeconds")
  val durationSeconds: Int = 0,
  @SerializedName("scheduledTime")
  val scheduledTime: String? = null,
  @SerializedName("isCompleted")
  val isCompleted: Boolean = false,
  @SerializedName("order")
  val order: Int = 0
)
