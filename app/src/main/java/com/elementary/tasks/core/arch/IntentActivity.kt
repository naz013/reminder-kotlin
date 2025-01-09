package com.elementary.tasks.core.arch

import android.content.ContentResolver
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.note.OldNote
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DataDestination
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.activity.LightThemedActivity
import com.github.naz013.ui.common.activity.toast
import org.koin.android.ext.android.inject

class IntentActivity : LightThemedActivity() {

  private val noteToOldNoteConverter by inject<NoteToOldNoteConverter>()
  private val navigator by inject<Navigator>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val data = intent.data ?: return
    val scheme = data.scheme

    Logger.i(TAG, "Incoming intent with data: $data, scheme: $scheme")

    if (ContentResolver.SCHEME_CONTENT == scheme) {
      val any = MemoryUtil.readFromUri(this, data)
      if (any != null) {
        Logger.i(TAG, "Parsed object: $any")
        when (any) {
          is Place -> {
            if (any.isValid()) {
              Logger.i(TAG, "Place is valid")
              navigator.navigate(DataDestination(any))
            } else {
              Logger.i(TAG, "Place is NOT invalid, reason: ${any.getInvalidReason()}")
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          is NoteWithImages -> {
            if (any.isValid()) {
              Logger.i(TAG, "Note is valid")
              navigator.navigate(DataDestination(any))
            } else {
              Logger.i(TAG, "Note is NOT valid")
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          is OldNote -> {
            val noteWithImages = noteToOldNoteConverter.toNote(any)
            if (noteWithImages != null) {
              Logger.i(TAG, "OLD Note is valid")
              navigator.navigate(DataDestination(noteWithImages))
            } else {
              Logger.i(TAG, "OLD Note is Null")
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          is Birthday -> {
            if (any.isValid()) {
              Logger.i(TAG, "Birthday is valid")
              navigator.navigate(DataDestination(any))
            } else {
              Logger.i(TAG, "Birthday is NOT valid, reason: ${any.getInvalidReason()}")
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          is Reminder -> {
            if (any.isValid()) {
              Logger.i(TAG, "Reminder is valid")
              navigator.navigate(DataDestination(any))
            } else {
              Logger.i(TAG, "Reminder is NOT valid, reason: ${any.getInvalidReason()}")
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          is ReminderGroup -> {
            if (any.isValid()) {
              Logger.i(TAG, "Group is valid")
              navigator.navigate(DataDestination(any))
            } else {
              Logger.i(TAG, "Group is NOT valid, reason: ${any.getInvalidReason()}")
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          else -> {
            Logger.i(TAG, "Parsed object is not supported: ${any.javaClass.simpleName}")
            toast(getString(R.string.unsupported_file_format))
            finish()
          }
        }
      } else {
        Logger.i(TAG, "Parsed object is NULL")
        toast(getString(R.string.unsupported_file_format))
        finish()
      }
    } else {
      Logger.i(TAG, "Unsupported scheme: $scheme")
      toast(getString(R.string.unsupported_file_format))
      finish()
    }
  }

  private fun NoteWithImages.isValid(): Boolean {
    val nt = note
    return nt != null && nt.key.isNotEmpty()
  }

  private fun ReminderGroup.isValid(): Boolean {
    return groupTitle.isNotBlank() && groupUuId.isNotEmpty()
  }

  private fun ReminderGroup.getInvalidReason(): String {
    return when {
      groupUuId.isBlank() -> "UUID is blank"
      groupTitle.isBlank() -> "Title is blank"
      else -> ""
    }
  }

  private fun Place.isValid(): Boolean {
    return latitude != 0.0 && longitude != 0.0 && name.isNotBlank()
  }

  private fun Place.getInvalidReason(): String {
    return when {
      latitude == 0.0 -> "Latitude is 0"
      longitude == 0.0 -> "Longitude is 0"
      name.isBlank() -> "Name is blank"
      else -> ""
    }
  }

  private fun Reminder.isValid(): Boolean {
    return type > 0 && groupUuId.isNotBlank()
  }

  private fun Reminder.getInvalidReason(): String {
    return when {
      type <= 0 -> "Type is not supported"
      groupUuId.isBlank() -> "Group UUID is blank"
      else -> ""
    }
  }

  private fun Birthday.isValid(): Boolean {
    return name.isNotBlank() && date.isNotBlank() && uuId.isNotBlank() && day > 0
  }

  private fun Birthday.getInvalidReason(): String {
    return when {
      name.isBlank() -> "Name is blank"
      date.isBlank() -> "Date is blank"
      uuId.isBlank() -> "Key is blank"
      day == 0 -> "Day is 0"
      else -> ""
    }
  }

  companion object {
    private const val TAG = "IntentActivity"
  }
}
