package com.elementary.tasks.core.app_widgets.notes

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import androidx.core.content.ContextCompat

import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.preview.NotePreviewActivity
import java.lang.RuntimeException

class NotesWidget : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    val sp = context.getSharedPreferences(
      NotesWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
    for (i in appWidgetIds) {
      updateWidget(context, appWidgetManager, sp, i)
    }
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    super.onDeleted(context, appWidgetIds)
    val editor = context.getSharedPreferences(
      NotesWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit()
    for (widgetID in appWidgetIds) {
      editor.remove(NotesWidgetConfigActivity.WIDGET_HEADER_BG_COLOR + widgetID)
    }
    editor.apply()
  }

  companion object {

    fun updateWidget(context: Context, appWidgetManager: AppWidgetManager,
                     sp: SharedPreferences, widgetID: Int) {
      val rv = RemoteViews(context.packageName, R.layout.widget_note)
      val headerBgColor = sp.getInt(NotesWidgetConfigActivity.WIDGET_HEADER_BG_COLOR + widgetID, 0)

      rv.setInt(R.id.headerBg, "setBackgroundResource", WidgetUtils.newWidgetBg(headerBgColor))

      if (WidgetUtils.isDarkBg(headerBgColor)) {
        WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_settings_24px, R.color.pureWhite,
          R.id.btn_settings, NotesWidgetConfigActivity::class.java) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
          return@initButton it
        }
        WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_add_24px, R.color.pureWhite,
          R.id.btn_add_note, CreateNoteActivity::class.java)
        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureWhite))
      } else {
        WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_settings_24px, R.color.pureBlack,
          R.id.btn_settings, NotesWidgetConfigActivity::class.java) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
          return@initButton it
        }
        WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_add_24px, R.color.pureBlack,
          R.id.btn_add_note, CreateNoteActivity::class.java)
        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureBlack))
      }

      val startActivityIntent = Intent(context, NotePreviewActivity::class.java)
      val startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent,
        PendingIntent.FLAG_UPDATE_CURRENT)
      rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent)

      val adapter = Intent(context, NotesService::class.java)
      adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
      rv.setRemoteAdapter(android.R.id.list, adapter)
      try {
        appWidgetManager.updateAppWidget(widgetID, rv)
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, android.R.id.list)
      } catch (e: RuntimeException) {

      }
    }
  }
}
