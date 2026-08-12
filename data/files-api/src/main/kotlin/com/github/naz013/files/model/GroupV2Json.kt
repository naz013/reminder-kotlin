package com.github.naz013.files.model

import com.google.gson.annotations.SerializedName

data class GroupV2Json(
  @SerializedName("schemaVersion")
  val schemaVersion: String = "v1.0",
  @SerializedName("uuId")
  val uuId: String,
  @SerializedName("title")
  val title: String = "",
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("isDefault")
  val isDefault: Boolean = false,
  @SerializedName("notification")
  val notification: NotificationSettingsOverrideJson,
  @SerializedName("createdAt")
  val createdAt: String,
  @SerializedName("versionId")
  val version: Long = 0L
)
