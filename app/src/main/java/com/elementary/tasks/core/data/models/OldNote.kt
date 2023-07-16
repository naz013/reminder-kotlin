package com.elementary.tasks.core.data.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
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
  val archived: Boolean = false
)
