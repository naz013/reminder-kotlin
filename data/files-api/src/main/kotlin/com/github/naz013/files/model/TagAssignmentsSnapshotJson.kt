package com.github.naz013.files.model

import com.google.gson.annotations.SerializedName

/**
 * Synced as one whole-collection snapshot file (id "app"), the same shape as [SettingsModel] -
 * a row's absence is meaningful (it means "not tagged"), so this is threaded through
 * DataPostProcessor on download as a destructive replace rather than the per-id upsert path
 * every other synced entity uses.
 */
data class TagAssignmentsSnapshotJson(
  @SerializedName("schemaVersion")
  val schemaVersion: String = "v1.0",
  @SerializedName("assignments")
  val assignments: List<TagAssignmentRowJson> = emptyList()
)

data class TagAssignmentRowJson(
  @SerializedName("tagId")
  val tagId: String,
  @SerializedName("itemId")
  val itemId: String,
  @SerializedName("itemType")
  val itemType: String
)
