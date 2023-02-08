package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import com.elementary.tasks.core.interfaces.NoteInterface
import kotlinx.parcelize.Parcelize
import java.util.*

@Keep
@Parcelize
data class NoteWithImages(
  @Embedded
  val note: Note? = null,
  @Relation(parentColumn = "key", entityColumn = "noteId")
  val images: List<ImageFile> = ArrayList()
) : NoteInterface, Parcelable {

  override fun getGmtTime(): String {
    return note?.date ?: ""
  }

  override fun getSummary(): String {
    return note?.summary ?: ""
  }

  override fun getKey(): String {
    return note?.key ?: ""
  }

  override fun getColor(): Int {
    return note?.color ?: 0
  }

  override fun getStyle(): Int {
    return note?.style ?: 0
  }

  override fun getOpacity(): Int {
    return note?.opacity ?: 0
  }

  override fun getPalette(): Int {
    return note?.palette ?: 0
  }
}
