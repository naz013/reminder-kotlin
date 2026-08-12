package com.github.naz013.appwidgets

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.github.naz013.appwidgets.birthdays.BirthdaysGlanceAppWidget
import com.github.naz013.appwidgets.calendar.CalendarGlanceAppWidget
import com.github.naz013.appwidgets.combinedbuttons.CombinedButtonsGlanceAppWidget
import com.github.naz013.appwidgets.events.EventsGlanceAppWidget
import com.github.naz013.appwidgets.googletasks.GoogleTasksGlanceAppWidget
import com.github.naz013.appwidgets.notes.NotesGlanceAppWidget
import com.github.naz013.appwidgets.singlenote.SingleNoteGlanceAppWidget
import com.github.naz013.feature.common.coroutine.invokeSuspend
import com.github.naz013.logging.Logger

internal class AppWidgetUpdaterImpl(
  private val context: Context,
  private val appWidgetPreviewUpdater: AppWidgetPreviewUpdater
) : AppWidgetUpdater {

  override fun updateAllWidgets() {
    invokeSuspend { updateEventsWidget() }
    updateCalendarWidget()
    updateScheduleWidget()
    updateBirthdaysWidget()
  }

  override suspend fun updateEventsWidget(widgetId: Int) {
    updateGlanceWidget(EventsGlanceAppWidget(), widgetId)
    appWidgetPreviewUpdater.updateEventsWidgetPreview()
  }

  override suspend fun updateCombinedButtonsWidget(widgetId: Int) {
    updateGlanceWidget(CombinedButtonsGlanceAppWidget(), widgetId)
    appWidgetPreviewUpdater.updateCombinedButtonsWidgetPreview()
  }

  private suspend fun updateGlanceWidget(widget: GlanceAppWidget, widgetId: Int) {
    val manager = GlanceAppWidgetManager(context)
    var glanceId: GlanceId? = null
    if (widgetId != WidgetId.NO_ID) {
      try {
        glanceId = manager.getGlanceIdBy(widgetId)
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to get glance ID for widget with id $widgetId", e)
      }
    }
    Logger.i(TAG, "Updating ${widget.javaClass.simpleName} with id $widgetId, glanceId: $glanceId")
    if (glanceId == null) {
      // Update all widgets
      val glanceIds = manager.getGlanceIds(widget.javaClass)
      glanceIds.forEach { gId ->
        widget.update(context, gId)
      }
    } else {
      try {
        widget.update(context, glanceId)
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to update ${widget.javaClass.simpleName} with id $widgetId", e)
      }
    }
  }

  override fun updateNotesWidget(widgetId: Int) {
    invokeSuspend {
      updateGlanceWidget(NotesGlanceAppWidget(), widgetId)
      appWidgetPreviewUpdater.updateNotesWidgetPreview()
    }
    updateNoteWidgets()
  }

  private fun updateNoteWidgets() {
    invokeSuspend {
      updateGlanceWidget(SingleNoteGlanceAppWidget(), WidgetId.NO_ID)
      appWidgetPreviewUpdater.updateSingleNoteWidgetPreview()
    }
  }

  override suspend fun updateSingleNoteWidget(widgetId: Int) {
    updateGlanceWidget(SingleNoteGlanceAppWidget(), widgetId)
    appWidgetPreviewUpdater.updateSingleNoteWidgetPreview()
  }

  override fun updateCalendarWidget(widgetId: Int) {
    invokeSuspend {
      updateGlanceWidget(CalendarGlanceAppWidget(), widgetId)
      appWidgetPreviewUpdater.updateCalendarWidgetPreview()
    }
  }

  override fun updateScheduleWidget(widgetId: Int) {
    invokeSuspend {
      updateGlanceWidget(GoogleTasksGlanceAppWidget(), widgetId)
      appWidgetPreviewUpdater.updateScheduleWidgetPreview()
    }
  }

  override fun updateBirthdaysWidget(widgetId: Int) {
    invokeSuspend {
      updateGlanceWidget(BirthdaysGlanceAppWidget(), widgetId)
      appWidgetPreviewUpdater.updateBirthdaysWidgetPreview()
    }
  }

  companion object {
    private const val TAG = "AppWidgetUpdater"
  }
}
