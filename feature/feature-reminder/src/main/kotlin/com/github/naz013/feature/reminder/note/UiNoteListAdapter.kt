package com.github.naz013.feature.reminder.note

import androidx.core.content.ContextCompat
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.note.UiNoteList
import com.github.naz013.common.ContextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.ui.common.isAlmostTransparent
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.note.NoteFontProvider
import com.github.naz013.ui.note.UiNoteImagesAdapter

internal class UiNoteListAdapter(
  private val dateTimeManager: DateTimeManager,
  private val themeProvider: ThemeProvider,
  private val contextProvider: ContextProvider,
  private val uiNoteImagesAdapter: UiNoteImagesAdapter,
  private val noteFontProvider: NoteFontProvider,
) {
  fun convert(noteWithImages: NoteWithImages): UiNoteList {
    val backgroundColor =
      themeProvider.getNoteLightColor(
        noteWithImages.getColor(),
        noteWithImages.getOpacity(),
      )

    val isDarkIcon =
      if (noteWithImages.getOpacity().isAlmostTransparent()) {
        themeProvider.isDark
      } else {
        backgroundColor.isColorDark()
      }

    val isDarkBg =
      (noteWithImages.getOpacity().isAlmostTransparent() && themeProvider.isDark) ||
        backgroundColor.isColorDark()
    val textColor =
      if (isDarkBg) {
        ContextCompat.getColor(contextProvider.themedContext, R.color.pureWhite)
      } else {
        ContextCompat.getColor(contextProvider.themedContext, R.color.pureBlack)
      }

    val textSize =
      if (noteWithImages.getFontSize() == -1) {
        FontParams.DEFAULT_FONT_SIZE
      } else {
        noteWithImages.getFontSize()
      }

    return UiNoteList(
      id = noteWithImages.getKey(),
      backgroundColor = backgroundColor,
      moreIcon =
        ViewUtils.tintIcon(
          contextProvider.themedContext,
          R.drawable.ic_fluent_more_vertical,
          isDarkIcon,
        ),
      textColor = textColor,
      typeface = noteFontProvider.getTypeface(contextProvider.themedContext, noteWithImages.getStyle()),
      fontSize = textSize.toFloat(),
      formattedDateTime =
        dateTimeManager.fromGmtToLocal(noteWithImages.getGmtTime())?.let {
          dateTimeManager.getFullDateTime(it)
        } ?: "",
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary(),
      colorPosition = noteWithImages.getColor(),
      uniqueId = noteWithImages.note?.uniqueId ?: 1133,
    )
  }
}
