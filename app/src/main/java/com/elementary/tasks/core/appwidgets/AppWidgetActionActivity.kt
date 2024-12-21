package com.elementary.tasks.core.appwidgets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.birthdays.preview.BirthdayPreviewActivity
import com.elementary.tasks.core.arch.ThemedActivity
import com.github.naz013.feature.common.android.intentForClass
import com.github.naz013.feature.common.android.readSerializable
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.googletasks.TasksConstants
import com.elementary.tasks.googletasks.preview.GoogleTaskPreviewActivity
import com.elementary.tasks.googletasks.task.GoogleTaskActivity
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity
import com.github.naz013.logging.Logger

class AppWidgetActionActivity : ThemedActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val direction = intent.readSerializable(DIRECTION, Direction::class.java)
    Logger.d("direction -> $direction")
    if (direction == null) {
      finish()
      return
    }
    val data = intent.readSerializable(DATA, WidgetIntentProtocol::class.java)
    Logger.d("data -> $data")
    when (direction) {
      Direction.REMINDER -> {
        val id = data?.extra?.get(Constants.INTENT_ID) as? String ?: return
        PinLoginActivity.openLogged(this, ReminderPreviewActivity::class.java) {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          putExtra(Constants.INTENT_ID, id)
        }
      }
      Direction.BIRTHDAY -> {
        val id = data?.extra?.get(Constants.INTENT_ID) as? String ?: return
        PinLoginActivity.openLogged(this, BirthdayPreviewActivity::class.java) {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          putExtra(Constants.INTENT_ID, id)
        }
      }
      Direction.NOTE -> {
        val id = data?.extra?.get(Constants.INTENT_ID) as? String ?: return
        PinLoginActivity.openLogged(this, NotePreviewActivity::class.java) {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          putExtra(Constants.INTENT_ID, id)
        }
      }
      Direction.GOOGLE_TASK -> {
        val id = data?.extra?.get(Constants.INTENT_ID) as? String ?: return
        val action = data.extra[TasksConstants.INTENT_ACTION] as? String ?: return
        if (action == TasksConstants.CREATE) {
          PinLoginActivity.openLogged(this, GoogleTaskActivity::class.java) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Constants.INTENT_ID, id)
            putExtra(TasksConstants.INTENT_ACTION, action)
          }
        } else {
          PinLoginActivity.openLogged(this, GoogleTaskPreviewActivity::class.java) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Constants.INTENT_ID, id)
            putExtra(TasksConstants.INTENT_ACTION, action)
          }
        }
      }
    }
    finish()
  }

  companion object {

    const val DIRECTION = "arg_direction"
    const val DATA = "arg_data"

    fun createIntent(context: Context): Intent {
      return context.intentForClass(AppWidgetActionActivity::class.java)
    }
  }
}
