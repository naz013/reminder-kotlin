package com.github.naz013.appwidgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.github.naz013.appwidgets.birthdays.BirthdaysWidget
import com.github.naz013.appwidgets.calendar.CalendarWidget
import com.github.naz013.appwidgets.events.EventsWidget
import com.github.naz013.appwidgets.googletasks.TasksWidget
import com.github.naz013.appwidgets.notes.NotesWidget
import com.github.naz013.appwidgets.singlenote.SingleNoteWidget

internal class AppWidgetUpdaterImpl(
  private val context: Context
) : AppWidgetUpdater {

  override fun updateAllWidgets() {
    val intent = Intent(context, EventsWidget::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val ids = AppWidgetManager.getInstance(context)
      .getAppWidgetIds(ComponentName(context, EventsWidget::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
    updateCalendarWidget()
    updateScheduleWidget()
    updateBirthdaysWidget()
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
}
