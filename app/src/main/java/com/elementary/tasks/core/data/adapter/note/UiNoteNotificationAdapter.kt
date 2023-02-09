package com.elementary.tasks.core.data.adapter.note

import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteNotification
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.isAlmostTransparent
import com.elementary.tasks.core.utils.isColorDark
import com.elementary.tasks.core.utils.params.Prefs

class UiNoteNotificationAdapter(
  private val themeProvider: ThemeProvider,
  private val contextProvider: ContextProvider
) {

  fun convert(noteWithImages: NoteWithImages): UiNoteNotification {
    val backgroundColor = themeProvider.getNoteLightColor(
      noteWithImages.getColor(),
      noteWithImages.getOpacity(),
      noteWithImages.getPalette()
    )

    val textColor = if ((noteWithImages.getOpacity().isAlmostTransparent() &&
        themeProvider.isDark) || backgroundColor.isColorDark()) {
      ContextCompat.getColor(contextProvider.context, R.color.pureWhite)
    } else {
      ContextCompat.getColor(contextProvider.context, R.color.pureBlack)
    }

    val image = noteWithImages.images.firstOrNull()?.let {
      runCatching { BitmapFactory.decodeFile(it.filePath) }.getOrNull()
    }

    return UiNoteNotification(
      id = noteWithImages.getKey(),
      backgroundColor = backgroundColor,
      textColor = textColor,
      image = image,
      text = noteWithImages.getSummary(),
      uniqueId = noteWithImages.note?.uniqueId ?: 1133
    )
  }
}
