package com.github.naz013.files.model

import com.google.gson.annotations.SerializedName

data class HabitJson(
  @SerializedName("schemaVersion")
  val schemaVersion: String = "v1.0",
  @SerializedName("id")
  val id: String,
  @SerializedName("name")
  val name: String = "",
  @SerializedName("description")
  val description: String? = null,
  @SerializedName("frequency")
  val frequency: String = "DAILY",
  @SerializedName("targetCount")
  val targetCount: Int = 1,
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("icon")
  val icon: Int? = null,
  @SerializedName("isPinned")
  val isPinned: Boolean = false,
  @SerializedName("createdAt")
  val createdAt: String,
  @SerializedName("updatedAt")
  val updatedAt: String,
  @SerializedName("versionId")
  val version: Long = 0L
)
