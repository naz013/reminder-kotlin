package com.elementary.tasks.notes

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class SharedNote(
  @SerializedName("text")
  val text: String = "",
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
) : Parcelable {

  companion object {
    const val FILE_EXTENSION = ".etnote"
  }
}
