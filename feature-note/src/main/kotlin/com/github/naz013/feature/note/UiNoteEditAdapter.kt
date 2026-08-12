package com.github.naz013.feature.note

import com.github.naz013.domain.font.FontParams
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
    val titleTextSize =
      if (noteWithImages.getTitleFontSize() == -1) {
        FontParams.DEFAULT_TITLE_FONT_SIZE
      } else {
        noteWithImages.getTitleFontSize()
      }
    return UiNoteEdit(
      id = noteWithImages.getKey(),
      typeface = noteWithImages.getStyle(),
      title = noteWithImages.getTitle(),
      titleTypeface = noteWithImages.getTitleFontStyle(),
      titleFontSize = titleTextSize,
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary(),
      colorPosition = noteWithImages.getColor(),
      colorPalette = noteWithImages.getPalette(),
      opacity = noteWithImages.getOpacity(),
      fontSize = textSize,
      isArchived = noteWithImages.note?.archived ?: false,
    )
  }
}
