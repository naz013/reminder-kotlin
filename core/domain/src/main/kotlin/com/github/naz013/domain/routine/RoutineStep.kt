package com.github.naz013.domain.routine

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class RoutineStep(
  @SerializedName("id")
  val id: String = UUID.randomUUID().toString(),
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
