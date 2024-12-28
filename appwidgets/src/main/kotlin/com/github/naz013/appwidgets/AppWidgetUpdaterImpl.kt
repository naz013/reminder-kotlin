package com.github.naz013.appwidgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.github.naz013.appwidgets.birthdays.BirthdaysWidget
import com.github.naz013.appwidgets.calendar.CalendarWidget
import com.github.naz013.appwidgets.events.EventsGlanceAppWidget
import com.github.naz013.appwidgets.googletasks.TasksWidget
import com.github.naz013.appwidgets.notes.NotesWidget
import com.github.naz013.appwidgets.singlenote.SingleNoteWidget
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
    val manager = GlanceAppWidgetManager(context)
    val widget = EventsGlanceAppWidget()
    var glanceId: GlanceId? = null
    if (widgetId != WidgetId.NO_ID) {
      try {
        glanceId = manager.getGlanceIdBy(widgetId)
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to get glance ID for widget with id $widgetId", e)
      }
    }
    Logger.i(TAG, "Updating events widget with id $widgetId, glanceId: $glanceId")
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
        Logger.e(TAG, "Failed to update events widget with id $widgetId", e)
      }
    }
    appWidgetPreviewUpdater.updateEventsWidgetPreview()
  }

  override fun updateNotesWidget() {
    val intent = Intent(context, NotesWidget::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val ids = AppWidgetManager.getInstance(context)
      .getAppWidgetIds(ComponentName(context, NotesWidget::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)

    updateNoteWidgets()
  }

  private fun updateNoteWidgets() {
    val intent = Intent(context, SingleNoteWidget::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val ids = AppWidgetManager.getInstance(context)
      .getAppWidgetIds(ComponentName(context, SingleNoteWidget::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
  }

  override fun updateCalendarWidget() {
    val intent = Intent(context, CalendarWidget::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val ids = AppWidgetManager.getInstance(context)
      .getAppWidgetIds(ComponentName(context, CalendarWidget::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
  }

  override fun updateScheduleWidget() {
    val intent = Intent(context, TasksWidget::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val ids = AppWidgetManager.getInstance(context)
      .getAppWidgetIds(ComponentName(context, TasksWidget::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
  }

  override fun updateBirthdaysWidget() {
    val intent = Intent(context, BirthdaysWidget::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val ids = AppWidgetManager.getInstance(context)
      .getAppWidgetIds(ComponentName(context, BirthdaysWidget::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
  }

  companion object {
    private const val TAG = "AppWidgetUpdater"
  }
}
