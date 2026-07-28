package com.github.naz013.files.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class SharedNote(
  @SerializedName("text")
  val text: String = "",
  @SerializedName("title")
  val title: String = "",
  @SerializedName("titleFontSize")
  val titleFontSize: Int = -1,
  @SerializedName("titleFontStyle")
  val titleFontStyle: Int = -1,
  @SerializedName("id")
  val id: String = UUID.randomUUID().toString(),
  @SerializedName("date")
  val date: String = "",
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("style")
  val style: Int = 0,
  @SerializedName("palette")
  val palette: Int = 0,
  @SerializedName("updatedAt")
  val updatedAt: String? = null,
  @SerializedName("opacity")
  val opacity: Int = 100,
  @SerializedName("fontSize")
  val fontSize: Int = -1,
) {
  companion object {
    const val FILE_EXTENSION = ".etnote"
  }
}
