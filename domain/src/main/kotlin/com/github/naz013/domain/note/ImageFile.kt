package com.github.naz013.domain.note

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Arrays

data class ImageFile(
  @SerializedName("image")
  @Deprecated("use filePath")
  var image: ByteArray? = null,
  @SerializedName("noteId")
  val noteId: String = "",
  @SerializedName("id")
  val id: Int = 0,
  @SerializedName("filePath")
  var filePath: String = "",
  @SerializedName("fileName")
  var fileName: String = ""
) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ImageFile

    if (!Arrays.equals(image, other.image)) return false
    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    var result = image?.let { it.contentHashCode() } ?: 0
    result = 31 * result + id.hashCode()
    return result
  }

  override fun toString(): String {
    return "ImageFile(noteId='$noteId', id=$id"
  }
}
