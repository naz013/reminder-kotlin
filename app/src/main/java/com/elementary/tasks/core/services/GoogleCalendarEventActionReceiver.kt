package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import com.github.naz013.logic.notificationaction.calendarevent.GoogleCalendarEventActionProcessor
import org.koin.core.component.inject

class GoogleCalendarEventActionReceiver : BaseBroadcast() {
  private val googleCalendarEventActionProcessor by inject<GoogleCalendarEventActionProcessor>()

  override fun onReceive(
    context: Context,
    intent: Intent?,
  ) {
    if (intent != null) {
      val action = intent.action
      val id = intent.getStringExtra(IntentKeys.INTENT_ID) ?: ""
      Logger.d(TAG, "onReceive: $action, id=$id")
      if (action == ACTION_HIDE && id.isNotEmpty()) {
        googleCalendarEventActionProcessor.cancel(id)
      }
    }
  }

  companion object {
    private const val TAG = "GoogleCalendarEventActionReceiver"
    const val ACTION_HIDE = Actions.GoogleCalendarEvent.ACTION_HIDE_SIMPLE
  }
}
