package com.elementary.tasks.core.arch

import android.content.ContentResolver
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.groups.create.CreateGroupActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.places.create.CreatePlaceActivity
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.github.naz013.common.intent.IntentKeys
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
import com.github.naz013.ui.common.context.intentForClass
import org.koin.android.ext.android.inject

class IntentActivity : LightThemedActivity() {

  private val noteToOldNoteConverter by inject<NoteToOldNoteConverter>()
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()
  private val navigator by inject<Navigator>()

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
                  .putExtra(IntentKeys.INTENT_ITEM, any)
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
                  .putExtra(IntentKeys.INTENT_ITEM, noteWithImages)
              )
            } else {
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          is Birthday -> {
            if (any.isValid()) {
              navigator.navigate(DataDestination(any))
            } else {
              toast(getString(R.string.unsupported_file_format))
            }
            finish()
          }

          is Reminder -> {
            if (any.isValid()) {
              reminderBuilderLauncher.openNotLogged(this) {
                putExtra(IntentKeys.INTENT_ITEM, any)
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
                  .putExtra(IntentKeys.INTENT_ITEM, any)
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
