package com.elementary.tasks.core.app_widgets.buttons

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity

class CombinedButtonsWidget : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    super.onUpdate(context, appWidgetManager, appWidgetIds)
    for (widgetId in appWidgetIds) {
      updateWidget(context, appWidgetManager, CombinedWidgetPrefsProvider(context, widgetId))
    }
  }

  companion object {

    fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      prefsProvider: CombinedWidgetPrefsProvider
    ) {
      val color = prefsProvider.getWidgetBackground()

      val rv = RemoteViews(context.packageName, R.layout.widget_combined_buttons)
      rv.setInt(android.R.id.background, "setBackgroundResource", WidgetUtils.newWidgetBg(color))

      if (WidgetUtils.isDarkBg(color)) {
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_alarm_24px, R.color.pureWhite,
          R.id.btn_add_reminder, CreateReminderActivity::class.java
        )
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_note_24px, R.color.pureWhite,
          R.id.btn_add_note, CreateNoteActivity::class.java
        )
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_cake_24px, R.color.pureWhite,
          R.id.btn_add_birthday, AddBirthdayActivity::class.java
        )
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_mic_black_24px, R.color.pureWhite,
          R.id.btn_voice, VoiceWidgetDialog::class.java
        )
      } else {
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_alarm_24px, R.color.pureBlack,
          R.id.btn_add_reminder, CreateReminderActivity::class.java
        )
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_note_24px, R.color.pureBlack,
          R.id.btn_add_note, CreateNoteActivity::class.java
        )
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_cake_24px, R.color.pureBlack,
          R.id.btn_add_birthday, AddBirthdayActivity::class.java
        )
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_mic_black_24px, R.color.pureBlack,
          R.id.btn_voice, VoiceWidgetDialog::class.java
        )
      }

      appWidgetManager.updateAppWidget(prefsProvider.widgetId, rv)
    }
  }
}
