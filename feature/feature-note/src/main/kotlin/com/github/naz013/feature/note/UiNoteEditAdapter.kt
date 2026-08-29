package com.github.naz013.feature.note

import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.ui.note.UiNoteImagesAdapter

internal class UiNoteEditAdapter(
  private val uiNoteImagesAdapter: UiNoteImagesAdapter,
) {
  fun convert(noteWithImages: NoteWithImages): UiNoteEdit {
    val textSize =
      if (noteWithImages.getFontSize() == -1) {
        FontParams.DEFAULT_FONT_SIZE
      } else {
        noteWithImages.getFontSize()
      }
    return UiNoteEdit(
      id = noteWithImages.getKey(),
      typeface = noteWithImages.getStyle(),
      document = noteWithImages.note?.content ?: NoteDocument(),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      colorIndex = noteWithImages.getColor(),
      opacity = noteWithImages.getOpacity(),
      fontSize = textSize,
      isArchived = noteWithImages.note?.archived ?: false,
    )
  }
}
