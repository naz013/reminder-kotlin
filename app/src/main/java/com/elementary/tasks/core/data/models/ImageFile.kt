package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity
@Parcelize
data class ImageFile(
  @SerializedName("image")
  @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
  @Deprecated("use filePath")
  var image: ByteArray? = null,
  @SerializedName("noteId")
  val noteId: String = "",
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  @SerializedName("filePath")
  var filePath: String = "",
  @SerializedName("fileName")
  var fileName: String = ""
) : Parcelable {

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
