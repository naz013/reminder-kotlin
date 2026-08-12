package com.github.naz013.ui.note

import android.graphics.Bitmap

/**
 * Seam over app's `Notifier` (implements `NotificationApi`, but "show note in notification" isn't
 * part of that shared interface), which `ui-note`/`feature-note` can't depend on. Implemented in
 * `app` by delegating to `Notifier.showNoteNotification` - see `AppNoteNotifier`.
 */
interface NoteNotifier {
  fun showNoteNotification(text: String, image: Bitmap?, uniqueId: Int)
}
