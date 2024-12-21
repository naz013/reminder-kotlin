package com.github.naz013.domain

import com.github.naz013.domain.reminder.BiType
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime
import java.util.UUID

data class RecurPreset(
  @SerializedName("id")
  val id: String = UUID.randomUUID().toString(),
  @SerializedName("recurObject")
  val recurObject: String,
  @SerializedName("name")
  var name: String,
  @SerializedName("type")
  val type: PresetType,
  @SerializedName("createdAt")
  val createdAt: LocalDateTime,
  @SerializedName("builderScheme")
  val builderScheme: List<PresetBuilderScheme> = emptyList(),
  @SerializedName("useCount")
  val useCount: Int,
  @SerializedName("description")
  val description: String?
)

enum class PresetType {
  RECUR,
  BUILDER
}

data class PresetBuilderScheme(
  @SerializedName("type")
  val type: BiType,
  @SerializedName("position")
  val position: Int,
  @SerializedName("value")
  val value: String
)
