package com.github.naz013.domain.note

import com.google.gson.annotations.SerializedName

@Deprecated("Old data class for sync of old backup, do not use in new code")
data class OldNote(
  @SerializedName("summary")
  val summary: String = "",
  @SerializedName("key")
  val key: String = "",
  @SerializedName("date")
  val date: String = "",
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("palette")
  val palette: Int = 0,
  @SerializedName("style")
  val style: Int = 0,
  @SerializedName("images")
  val images: List<OldImageFile> = ArrayList(),
  @SerializedName("updatedAt")
  val updatedAt: String? = null,
  @SerializedName("uniqueId")
  val uniqueId: Int = 0,
  @SerializedName("fontSize")
  val fontSize: Int = -1,
  @SerializedName("archived")
  val archived: Boolean = false,
  @SerializedName("versionId")
  var version: Long = 0L,
)
