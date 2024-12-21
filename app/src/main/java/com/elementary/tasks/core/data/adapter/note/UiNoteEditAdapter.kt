package com.elementary.tasks.core.data.adapter.note

import com.github.naz013.domain.note.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteEdit
import com.github.naz013.domain.font.FontParams

class UiNoteEditAdapter(
  private val uiNoteImagesAdapter: UiNoteImagesAdapter
) {

  fun convert(noteWithImages: NoteWithImages): UiNoteEdit {
    val textSize = if (noteWithImages.getFontSize() == -1) {
      FontParams.DEFAULT_FONT_SIZE
    } else {
      noteWithImages.getFontSize()
    }
    return UiNoteEdit(
      id = noteWithImages.getKey(),
      typeface = noteWithImages.getStyle(),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary(),
      colorPosition = noteWithImages.getColor(),
      colorPalette = noteWithImages.getPalette(),
      opacity = noteWithImages.getOpacity(),
      fontSize = textSize,
      isArchived = noteWithImages.note?.archived ?: false
    )
  }
}
