package com.github.naz013.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.note.ImageFile
import com.google.gson.annotations.SerializedName
import java.util.Arrays

@Entity(tableName = "ImageFile")
internal data class ImageFileEntity(
  @SerializedName("image")
  @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
  @Deprecated("use filePath")
  val image: ByteArray? = null,
  @SerializedName("noteId")
  val noteId: String = "",
  @SerializedName("id")
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  @SerializedName("filePath")
  val filePath: String = "",
  @SerializedName("fileName")
  val fileName: String = ""
) {

  constructor(imageFile: ImageFile) : this(
    image = imageFile.image,
    noteId = imageFile.noteId,
    id = imageFile.id,
    filePath = imageFile.filePath,
    fileName = imageFile.fileName
  )

  fun toDomain(): ImageFile {
    return ImageFile(
      image = image,
      noteId = noteId,
      id = id,
      filePath = filePath,
      fileName = fileName
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ImageFileEntity

    if (!Arrays.equals(image, other.image)) return false
    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    var result = image?.let { it.contentHashCode() } ?: 0
    result = 31 * result + id.hashCode()
    return result
  }
}
