package com.elementary.tasks.core.arch

import android.content.ContentResolver
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.note.OldNote
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.elementary.tasks.core.os.IntentDataHolder
import com.github.naz013.feature.common.android.intentForClass
import com.github.naz013.feature.common.android.toast
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.groups.create.CreateGroupActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.places.create.CreatePlaceActivity
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.github.naz013.logging.Logger
import org.koin.android.ext.android.inject

class IntentActivity : ThemedActivity() {

  private val noteToOldNoteConverter by inject<NoteToOldNoteConverter>()
  private val intentDataHolder by inject<IntentDataHolder>()
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val data = intent.data ?: return
    val scheme = data.scheme

    Logger.d("onCreate: $data, $scheme")

    if (ContentResolver.SCHEME_CONTENT == scheme) {
      val any = MemoryUtil.readFromUri(this, data)
      Logger.d("getPlace: $any")
      if (any != null) {
        when (any) {
          is Place -> {
            if (any.isValid()) {
              startActivity(
                intentForClass(CreatePlaceActivity::class.java)
                  .putExtra(Constants.INTENT_ITEM, any)
              )
            } else {
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          is OldNote -> {
            val noteWithImages = noteToOldNoteConverter.toNote(any)
            if (noteWithImages != null) {
              startActivity(
                intentForClass(CreateNoteActivity::class.java)
                  .putExtra(Constants.INTENT_ITEM, noteWithImages)
              )
            } else {
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          is Birthday -> {
            if (any.isValid()) {
              intentDataHolder.putData(Constants.INTENT_ITEM, any)
              startActivity(intentForClass(AddBirthdayActivity::class.java))
            } else {
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          is Reminder -> {
            if (any.isValid()) {
              reminderBuilderLauncher.openNotLogged(this) {
                putExtra(Constants.INTENT_ITEM, any)
              }
            } else {
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          is ReminderGroup -> {
            if (any.isValid()) {
              startActivity(
                intentForClass(CreateGroupActivity::class.java)
                  .putExtra(Constants.INTENT_ITEM, any)
              )
            } else {
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          else -> {
            toast(getString(R.string.unsupported_file_format))
            finish()
          }
        }
      } else {
        toast(getString(R.string.unsupported_file_format))
        finish()
      }
    } else {
      toast(getString(R.string.unsupported_file_format))
      finish()
    }
  }
}

private fun ReminderGroup.isValid(): Boolean {
  return groupTitle.isNotBlank() && groupUuId.isNotEmpty()
}

private fun Place.isValid(): Boolean {
  return latitude != 0.0 && longitude != 0.0 && name.isNotBlank()
}

fun NoteWithImages.isValid(): Boolean {
  val nt = note
  return nt != null && nt.key.isNotEmpty()
}

private fun Reminder.isValid(): Boolean {
  return summary.isNotBlank() && type > 0 && groupUuId.isNotBlank()
}

private fun Birthday.isValid(): Boolean {
  return name.isNotBlank() && date.isNotBlank() && key.isNotBlank() && day > 0
}
