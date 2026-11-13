package com.elementary.tasks.reminder.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.reminder.snooze.SnoozePickerScreen
import com.github.naz013.ui.common.compose.ComposeActivity
import com.github.naz013.ui.common.context.buildIntent

class SnoozePickerActivity : ComposeActivity() {

  @Composable
  override fun ActivityContent() {
    SnoozePickerScreen(
      id = intent.getStringExtra(IntentKeys.INTENT_ID) ?: "",
      onFinish = { snoozed ->
        setResult(if (snoozed) Activity.RESULT_OK else Activity.RESULT_CANCELED)
        finish()
      },
    )
  }

  companion object {
    fun getLaunchIntent(
      context: Context,
      id: String,
    ): Intent =
      context.buildIntent(SnoozePickerActivity::class.java) {
        putExtra(IntentKeys.INTENT_ID, id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      }
  }
}
