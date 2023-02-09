package com.elementary.tasks.core.data.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.*

@Keep
data class OldImageFile(
  @SerializedName("image")
  val image: ByteArray? = null,
  @SerializedName("noteId")
  val noteId: String = ""
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as OldImageFile

    if (image != null) {
      if (other.image == null) return false
      if (!image.contentEquals(other.image)) return false
    } else if (other.image != null) return false
    if (noteId != other.noteId) return false

    return true
  }

  override fun hashCode(): Int {
    var result = image?.contentHashCode() ?: 0
    result = 31 * result + noteId.hashCode()
    return result
  }
}
