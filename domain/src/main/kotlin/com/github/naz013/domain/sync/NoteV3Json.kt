package com.github.naz013.domain.sync

import com.google.gson.annotations.SerializedName

data class NoteV3Json(
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
  val images: List<NoteV3Image> = emptyList(),
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

data class NoteV3Image(
  @SerializedName("fileName")
  val fileName: String = "",
  @SerializedName("size")
  val size: Int = 0,
  @SerializedName("id")
  val id: String = "",
)
