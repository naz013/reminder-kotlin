package com.github.naz013.domain

import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.sync.SyncState
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
  val description: String?,
  @SerializedName("isDefault")
  val isDefault: Boolean = false,
  @SerializedName("recurItemsToAdd")
  val recurItemsToAdd: String?,
  @SerializedName("versionId")
  val version: Long = 0L,
  @SerializedName("syncState")
  val syncState: SyncState = SyncState.WaitingForUpload,
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
