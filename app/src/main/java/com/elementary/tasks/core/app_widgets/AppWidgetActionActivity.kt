package com.elementary.tasks.core.app_widgets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.birthdays.preview.BirthdayPreviewActivity
import com.elementary.tasks.core.arch.ThemedActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.readSerializable
import com.elementary.tasks.google_tasks.TasksConstants
import com.elementary.tasks.google_tasks.preview.GoogleTaskPreviewActivity
import com.elementary.tasks.google_tasks.task.GoogleTaskActivity
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity
import timber.log.Timber

class AppWidgetActionActivity : ThemedActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val direction = intent.readSerializable(DIRECTION, Direction::class.java)
    Timber.d("direction -> $direction")
    if (direction == null) {
      finish()
      return
    }
    val data = intent.readSerializable(DATA, WidgetIntentProtocol::class.java)
    Timber.d("data -> $data")
    when (direction) {
      Direction.REMINDER -> {
        val id = data?.extra?.get(Constants.INTENT_ID) as? String ?: return
        PinLoginActivity.openLogged(
          this,
          Intent(this, ReminderPreviewActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(Constants.INTENT_ID, id)
        )
      }
      Direction.BIRTHDAY -> {
        val id = data?.extra?.get(Constants.INTENT_ID) as? String ?: return
        PinLoginActivity.openLogged(
          this,
          Intent(this, BirthdayPreviewActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(Constants.INTENT_ID, id)
        )
      }
      Direction.NOTE -> {
        val id = data?.extra?.get(Constants.INTENT_ID) as? String ?: return
        PinLoginActivity.openLogged(
          this,
          Intent(this, NotePreviewActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(Constants.INTENT_ID, id)
        )
      }
      Direction.GOOGLE_TASK -> {
        val id = data?.extra?.get(Constants.INTENT_ID) as? String ?: return
        val action = data.extra[TasksConstants.INTENT_ACTION] as? String ?: return
        if (action == TasksConstants.CREATE) {
          PinLoginActivity.openLogged(
            this,
            Intent(this, GoogleTaskActivity::class.java)
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              .putExtra(Constants.INTENT_ID, id)
              .putExtra(TasksConstants.INTENT_ACTION, action)
          )
        } else {
          PinLoginActivity.openLogged(
            this,
            Intent(this, GoogleTaskPreviewActivity::class.java)
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              .putExtra(Constants.INTENT_ID, id)
              .putExtra(TasksConstants.INTENT_ACTION, action)
          )
        }
      }
    }
    finish()
  }

  companion object {
    const val DIRECTION = "arg_direction"
    const val DATA = "arg_data"

    fun createIntent(context: Context): Intent {
      return Intent(context, AppWidgetActionActivity::class.java)
    }
  }
}
