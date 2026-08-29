package com.github.naz013.ui.note

import androidx.compose.ui.graphics.Color
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.ui.common.isAlmostTransparent
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider

class UiNoteListItemAdapter(
  private val themeProvider: ThemeProvider,
  private val uiNoteImagesAdapter: UiNoteImagesAdapter,
) {
  fun convert(noteWithImages: NoteWithImages): UiNoteListItem {
    val backgroundColorInt =
      themeProvider.getNoteLightColor(
        noteWithImages.getColor(),
        noteWithImages.getOpacity(),
      )
    val isDarkBg =
      (noteWithImages.getOpacity().isAlmostTransparent() && themeProvider.isDark) ||
        backgroundColorInt.isColorDark()

    val fontSize =
      if (noteWithImages.getFontSize() == -1) {
        FontParams.DEFAULT_FONT_SIZE
      } else {
        noteWithImages.getFontSize()
      }

    return UiNoteListItem(
      id = noteWithImages.getKey(),
      content = noteWithImages.note?.content ?: NoteDocument(),
      backgroundColor = Color(backgroundColorInt),
      textColor = if (isDarkBg) Color.White else Color.Black,
      fontStyle = noteWithImages.getStyle(),
      fontSize = fontSize.toFloat(),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      isPinned = noteWithImages.isPinned(),
    )
  }
}
