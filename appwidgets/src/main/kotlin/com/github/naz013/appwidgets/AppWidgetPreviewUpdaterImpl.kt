package com.github.naz013.appwidgets

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.github.naz013.appwidgets.birthdays.BirthdaysGlanceAppWidgetReceiver
import com.github.naz013.appwidgets.calendar.CalendarGlanceAppWidgetReceiver
import com.github.naz013.appwidgets.combinedbuttons.CombinedButtonsGlanceAppWidgetReceiver
import com.github.naz013.appwidgets.events.EventsGlanceAppWidgetReceiver
import com.github.naz013.appwidgets.googletasks.GoogleTasksGlanceAppWidgetReceiver
import com.github.naz013.appwidgets.notes.NotesGlanceAppWidgetReceiver
import com.github.naz013.appwidgets.singlenote.SingleNoteGlanceAppWidgetReceiver
import com.github.naz013.common.system.Module
import com.github.naz013.logging.Logger
import kotlin.reflect.KClass

internal class AppWidgetPreviewUpdaterImpl(
  private val context: Context
) : AppWidgetPreviewUpdater {

  override suspend fun updateEventsWidgetPreview() {
    updatePreview(EventsGlanceAppWidgetReceiver::class, "Events")
  }

  override suspend fun updateCombinedButtonsWidgetPreview() {
    updatePreview(CombinedButtonsGlanceAppWidgetReceiver::class, "CombinedButtons")
  }

  override suspend fun updateNotesWidgetPreview() {
    updatePreview(NotesGlanceAppWidgetReceiver::class, "Notes")
  }

  override suspend fun updateSingleNoteWidgetPreview() {
    updatePreview(SingleNoteGlanceAppWidgetReceiver::class, "SingleNote")
  }

  override suspend fun updateCalendarWidgetPreview() {
    updatePreview(CalendarGlanceAppWidgetReceiver::class, "Calendar")
  }

  override suspend fun updateScheduleWidgetPreview() {
    updatePreview(GoogleTasksGlanceAppWidgetReceiver::class, "GoogleTasks")
  }

  override suspend fun updateBirthdaysWidgetPreview() {
    updatePreview(BirthdaysGlanceAppWidgetReceiver::class, "Birthdays")
  }

  private suspend fun updatePreview(
    receiver: KClass<out GlanceAppWidgetReceiver>,
    name: String
  ) {
    if (!Module.is15) {
      return
    }
    try {
      val result = GlanceAppWidgetManager(context).setWidgetPreviews(receiver)
      Logger.d(TAG, "Updated $name widget preview, result = $result")
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to update $name widget preview", e)
    }
  }

  companion object {
    private const val TAG = "AppWidgetPreviewUpdater"
  }
}
