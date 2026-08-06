package com.elementary.tasks.core.arch

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderV2

/**
 * Decides what [IntentActivity] should do with an incoming import intent: whether its
 * URI scheme is one it can read, and whether the object parsed from that URI is valid
 * enough to hand off to navigation.
 */
class ImportIntentResolver {

  fun isSupportedScheme(scheme: String?): Boolean = scheme == SCHEME_CONTENT || scheme == SCHEME_FILE

  fun resolve(data: Any?): ImportResult {
    if (data == null) return ImportResult.Unsupported
    return when (data) {
      is Place -> data.toResult()
      is NoteWithImages -> data.toResult()
      is Birthday -> data.toResult()
      is ReminderV2 -> ImportResult.Valid(data)
      is GroupV2 -> ImportResult.Valid(data)
      else -> ImportResult.Unsupported
    }
  }

  private fun Place.toResult(): ImportResult {
    val reason = when {
      latitude == 0.0 -> "Latitude is 0"
      longitude == 0.0 -> "Longitude is 0"
      name.isBlank() -> "Name is blank"
      else -> null
    }
    return if (reason == null) ImportResult.Valid(this) else ImportResult.Invalid(reason)
  }

  private fun NoteWithImages.toResult(): ImportResult {
    val nt = note
    return if (nt != null && nt.key.isNotEmpty()) {
      ImportResult.Valid(this)
    } else {
      ImportResult.Invalid("Note is not valid")
    }
  }

  private fun Birthday.toResult(): ImportResult {
    val reason = when {
      name.isBlank() -> "Name is blank"
      date.isBlank() -> "Date is blank"
      uuId.isBlank() -> "Key is blank"
      day == 0 -> "Day is 0"
      else -> null
    }
    return if (reason == null) ImportResult.Valid(this) else ImportResult.Invalid(reason)
  }

  companion object {
    const val SCHEME_CONTENT = "content"
    const val SCHEME_FILE = "file"
  }
}

sealed class ImportResult {
  data class Valid(val data: Any) : ImportResult()
  data class Invalid(val reason: String) : ImportResult()
  data object Unsupported : ImportResult()
}
