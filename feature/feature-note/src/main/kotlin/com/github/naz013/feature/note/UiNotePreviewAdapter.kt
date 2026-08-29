package com.github.naz013.feature.note

import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.ui.note.UiNoteImagesAdapter

internal class UiNotePreviewAdapter(
  private val uiNoteImagesAdapter: UiNoteImagesAdapter,
) {
  fun convert(noteWithImages: NoteWithImages): UiNotePreview {
    val textSize =
      if (noteWithImages.getFontSize() == -1) {
        FontParams.DEFAULT_FONT_SIZE
      } else {
        noteWithImages.getFontSize()
      }

    return UiNotePreview(
      id = noteWithImages.getKey(),
      document = noteWithImages.note?.content ?: NoteDocument(),
      fontStyle = noteWithImages.getStyle(),
      fontSize = textSize.toFloat(),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      uniqueId = noteWithImages.note?.uniqueId ?: 1133,
      isArchived = noteWithImages.note?.archived ?: false,
      isPinned = noteWithImages.isPinned(),
    )
  }
}
