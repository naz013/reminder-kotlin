package com.github.naz013.domain.note

import com.github.naz013.domain.font.FontParams
import java.io.Serializable

data class NoteWithImages(
  val note: Note? = null,
  val images: List<ImageFile> = ArrayList()
) : NoteInterface, Serializable {

  override fun getGmtTime(): String {
    return note?.date ?: ""
  }

  override fun getSummary(): String {
    return note?.content?.text ?: ""
  }

  fun getTitle(): String {
    return note?.content?.displayTitle() ?: ""
  }

  override fun getKey(): String {
    return note?.key ?: ""
  }

  override fun getColor(): Int {
    return note?.color ?: 0
  }

  override fun getStyle(): Int {
    return note?.style ?: FontParams.DEFAULT_FONT_STYLE
  }

  override fun getOpacity(): Int {
    return note?.opacity ?: 0
  }

  fun getFontSize(): Int {
    return note?.fontSize ?: FontParams.DEFAULT_FONT_SIZE
  }

  fun isPinned(): Boolean {
    return note?.isPinned ?: false
  }
}
