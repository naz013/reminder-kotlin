package com.elementary.tasks.core.notes

import android.graphics.Bitmap
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.feature.note.UiNoteNotification
import com.github.naz013.ui.note.NoteNotifier

class AppNoteNotifier(
  private val notifier: Notifier,
) : NoteNotifier {
  override fun showNoteNotification(text: String, image: Bitmap?, uniqueId: Int) {
    notifier.showNoteNotification(
      UiNoteNotification(
        id = "",
        text = text,
        backgroundColor = 0,
        textColor = 0,
        image = image,
        uniqueId = uniqueId,
      ),
    )
  }
}
