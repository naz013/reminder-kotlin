package com.elementary.tasks.core.data.adapter.note

import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.elementary.tasks.core.utils.isAlmostTransparent
import com.elementary.tasks.core.utils.isColorDark
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.ViewUtils

class UiNoteListAdapter(
  private val dateTimeManager: DateTimeManager,
  private val themeProvider: ThemeProvider,
  private val contextProvider: ContextProvider,
  private val prefs: Prefs,
  private val uiNoteImagesAdapter: UiNoteImagesAdapter
) {

  fun convert(noteWithImages: NoteWithImages): UiNoteList {
    val backgroundColor = themeProvider.getNoteLightColor(
      noteWithImages.getColor(),
      noteWithImages.getOpacity(),
      noteWithImages.getPalette()
    )

    val isDarkIcon = if (noteWithImages.getOpacity().isAlmostTransparent()) {
      themeProvider.isDark
    } else {
      backgroundColor.isColorDark()
    }

    val textColor = if ((noteWithImages.getOpacity().isAlmostTransparent() &&
        themeProvider.isDark) || backgroundColor.isColorDark()) {
      ContextCompat.getColor(contextProvider.context, R.color.pureWhite)
    } else {
      ContextCompat.getColor(contextProvider.context, R.color.pureBlack)
    }

    return UiNoteList(
      id = noteWithImages.getKey(),
      backgroundColor = backgroundColor,
      moreIcon = ViewUtils.tintIcon(
        contextProvider.context,
        R.drawable.ic_twotone_more_vert_24px,
        isDarkIcon
      ),
      textColor = textColor,
      typeface = AssetsUtil.getTypeface(contextProvider.context, noteWithImages.getStyle()),
      fontSize = (prefs.noteTextSize + 12).toFloat(),
      formattedDateTime = dateTimeManager.fromGmtToLocal(noteWithImages.getGmtTime())?.let {
        dateTimeManager.getFullDateTime(it)
      } ?: "",
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary(),
      colorPosition = noteWithImages.getColor(),
      colorPalette = noteWithImages.getPalette(),
      uniqueId = noteWithImages.note?.uniqueId ?: 1133
    )
  }
}
