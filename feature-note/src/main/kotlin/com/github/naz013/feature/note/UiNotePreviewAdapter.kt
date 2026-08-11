package com.github.naz013.feature.note

import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.ui.note.NoteFontProvider
import com.github.naz013.ui.note.UiNoteImagesAdapter

class UiNotePreviewAdapter(
  private val contextProvider: ContextProvider,
  private val uiNoteImagesAdapter: UiNoteImagesAdapter,
  private val noteFontProvider: NoteFontProvider,
) {
  fun convert(noteWithImages: NoteWithImages): UiNotePreview {
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

    return UiNotePreview(
      id = noteWithImages.getKey(),
      typeface = noteFontProvider.getTypeface(contextProvider.themedContext, noteWithImages.getStyle()),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary(),
      title = noteWithImages.getTitle(),
      uniqueId = noteWithImages.note?.uniqueId ?: 1133,
      textSize = textSize.toFloat(),
      titleTypeface =
        noteFontProvider.getTypeface(
          contextProvider.themedContext,
          noteWithImages.getTitleFontStyle(),
        ),
      titleTextSize = titleTextSize.toFloat(),
      isArchived = noteWithImages.note?.archived ?: false,
    )
  }
}
