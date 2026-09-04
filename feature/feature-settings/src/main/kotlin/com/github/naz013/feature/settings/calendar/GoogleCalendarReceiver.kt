package com.github.naz013.feature.settings.calendar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.naz013.feature.settings.calendar.work.ScanGoogleCalendarEventsTask
import com.github.naz013.logging.Logger
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GoogleCalendarReceiver :
  BroadcastReceiver(),
  KoinComponent {
  private val prefs by inject<CalendarSettingsPreferences>()
  private val workScheduler by inject<WorkScheduler>()

  override fun onReceive(
    context: Context?,
    intent: Intent?,
  ) {
    Logger.i(TAG, "Received intent: ${intent?.action}")
    if (!prefs.scanGoogleCalendarEvents) {
      Logger.w(TAG, "Google Calendar scanning is disabled in preferences.")
      return
    }
    if (prefs.selectedGoogleCalendarIds.isEmpty()) {
      Logger.w(TAG, "No Google Calendars selected in preferences.")
      return
    }
    workScheduler.enqueue(
      WorkRequest(taskKey = ScanGoogleCalendarEventsTask.TASK_KEY, tag = ScanGoogleCalendarEventsTask.TASK_KEY),
    )
  }

  companion object {
    private const val TAG = "GoogleCalendarReceiver"
  }
}
