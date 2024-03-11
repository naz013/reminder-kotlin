package com.elementary.tasks.core.appwidgets.buttons

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.appwidgets.WidgetUtils
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.reminder.ReminderBuilderLauncher

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
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_clock_alarm,
          color = R.color.pureWhite,
          viewId = R.id.btn_add_reminder,
          cls = ReminderBuilderLauncher.PENDING_INTENT_CLASS
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_note,
          color = R.color.pureWhite,
          viewId = R.id.btn_add_note,
          cls = CreateNoteActivity::class.java
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_food_cake,
          color = R.color.pureWhite,
          viewId = R.id.btn_add_birthday,
          cls = AddBirthdayActivity::class.java
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_builder_mic_on,
          color = R.color.pureWhite,
          viewId = R.id.btn_voice,
          cls = VoiceWidgetDialog::class.java
        )
      } else {
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_clock_alarm,
          color = R.color.pureBlack,
          viewId = R.id.btn_add_reminder,
          cls = ReminderBuilderLauncher.PENDING_INTENT_CLASS
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_note,
          color = R.color.pureBlack,
          viewId = R.id.btn_add_note,
          cls = CreateNoteActivity::class.java
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_food_cake,
          color = R.color.pureBlack,
          viewId = R.id.btn_add_birthday,
          cls = AddBirthdayActivity::class.java
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_builder_mic_on,
          color = R.color.pureBlack,
          viewId = R.id.btn_voice,
          cls = VoiceWidgetDialog::class.java
        )
      }

      appWidgetManager.updateAppWidget(prefsProvider.widgetId, rv)
    }
  }
}
