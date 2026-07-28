package com.elementary.tasks.core.arch

import android.content.ContentResolver
import android.os.Bundle
import androidx.compose.runtime.Composable
import com.elementary.tasks.R
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.files.AndroidDataConverter
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DataDestination
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.compose.ComposeActivity
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class IntentActivity : ComposeActivity() {

  private val navigator by inject<Navigator>()
  private val androidDataConverter by inject<AndroidDataConverter>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val data = intent.data ?: return
    val scheme = data.scheme

    Logger.i(TAG, "Incoming intent with data: $data, scheme: $scheme")

    if (ContentResolver.SCHEME_CONTENT == scheme) {
      val any = runBlocking { androidDataConverter.toData(data) }
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

          is ReminderV2 -> {
            Logger.i(TAG, "Reminder is valid")
            navigator.navigate(DataDestination(any))
            finish()
          }

          is GroupV2 -> {
            Logger.i(TAG, "Group is valid")
            navigator.navigate(DataDestination(any))
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

  @Composable
  override fun ActivityContent() {

  }

  private fun NoteWithImages.isValid(): Boolean {
    val nt = note
    return nt != null && nt.key.isNotEmpty()
  }

  private fun Place.isValid(): Boolean = latitude != 0.0 && longitude != 0.0 && name.isNotBlank()

  private fun Place.getInvalidReason(): String =
    when {
      latitude == 0.0 -> "Latitude is 0"
      longitude == 0.0 -> "Longitude is 0"
      name.isBlank() -> "Name is blank"
      else -> ""
    }

  private fun Birthday.isValid(): Boolean = name.isNotBlank() && date.isNotBlank() && uuId.isNotBlank() && day > 0

  private fun Birthday.getInvalidReason(): String =
    when {
      name.isBlank() -> "Name is blank"
      date.isBlank() -> "Date is blank"
      uuId.isBlank() -> "Key is blank"
      day == 0 -> "Day is 0"
      else -> ""
    }

  companion object {
    private const val TAG = "IntentActivity"
  }
}
