package com.elementary.tasks.settings.calendar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.settings.calendar.work.ScanGoogleCalendarEventsWorker
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GoogleCalendarReceiver : BroadcastReceiver(), KoinComponent {

  private val prefs by inject<Prefs>()

  override fun onReceive(context: Context?, intent: Intent?) {
    Logger.i(TAG, "Received intent: ${intent?.action}")
    if (!prefs.scanGoogleCalendarEvents) {
      Logger.w(TAG, "Google Calendar scanning is disabled in preferences.")
      return
    }
    if (prefs.googleCalendarReminderId <= 0) {
      Logger.w(TAG, "No Google Calendar reminder ID set in preferences.")
      return
    }
    context?.let {
      ScanGoogleCalendarEventsWorker.schedule(it)
    }
  }

  companion object {
    private const val TAG = "GoogleCalendarReceiver"
  }
}
