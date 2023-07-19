package com.elementary.tasks.core.app_widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.elementary.tasks.core.app_widgets.birthdays.BirthdaysWidget
import com.elementary.tasks.core.app_widgets.calendar.CalendarWidget
import com.elementary.tasks.core.app_widgets.events.EventsWidget
import com.elementary.tasks.core.app_widgets.google_tasks.TasksWidget
import com.elementary.tasks.core.app_widgets.notes.NotesWidget
import com.elementary.tasks.core.app_widgets.singlenote.SingleNoteWidget

class UpdatesHelper(
  private val context: Context
) {

  fun updateWidgets() {
    val intent = Intent(context, EventsWidget::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val ids = AppWidgetManager.getInstance(context)
      .getAppWidgetIds(ComponentName(context, EventsWidget::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
    updateCalendarWidget()
    updateTasksWidget()
    updateBirthdaysWidget()
  }

  fun updateNotesWidget() {
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

  fun updateCalendarWidget() {
    val intent = Intent(context, CalendarWidget::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val ids = AppWidgetManager.getInstance(context)
      .getAppWidgetIds(ComponentName(context, CalendarWidget::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
  }

  fun updateTasksWidget() {
    val intent = Intent(context, TasksWidget::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val ids = AppWidgetManager.getInstance(context)
      .getAppWidgetIds(ComponentName(context, TasksWidget::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
  }

  fun updateBirthdaysWidget() {
    val intent = Intent(context, BirthdaysWidget::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val ids = AppWidgetManager.getInstance(context)
      .getAppWidgetIds(ComponentName(context, BirthdaysWidget::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
  }
}
