package com.github.naz013.ui.note

import androidx.compose.ui.graphics.Color
import com.github.naz013.domain.font.FontParams
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
        noteWithImages.getPalette(),
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
    val titleFontSize =
      if (noteWithImages.getTitleFontSize() == -1) {
        FontParams.DEFAULT_TITLE_FONT_SIZE
      } else {
        noteWithImages.getTitleFontSize()
      }

    return UiNoteListItem(
      id = noteWithImages.getKey(),
      title = noteWithImages.getTitle(),
      text = noteWithImages.getSummary(),
      backgroundColor = Color(backgroundColorInt),
      textColor = if (isDarkBg) Color.White else Color.Black,
      fontStyle = noteWithImages.getStyle(),
      fontSize = fontSize.toFloat(),
      titleFontStyle = noteWithImages.getTitleFontStyle(),
      titleFontSize = titleFontSize.toFloat(),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      isPinned = noteWithImages.isPinned(),
    )
  }
}
