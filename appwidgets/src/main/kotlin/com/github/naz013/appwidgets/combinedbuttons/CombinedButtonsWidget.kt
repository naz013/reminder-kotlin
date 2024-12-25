package com.github.naz013.appwidgets.combinedbuttons

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetUtils

internal class CombinedButtonsWidget : AppWidgetProvider() {

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
          intent = createIntent(context, Direction.ADD_REMINDER)
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_note,
          color = R.color.pureWhite,
          viewId = R.id.btn_add_note,
          intent = createIntent(context, Direction.ADD_NOTE)
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_food_cake,
          color = R.color.pureWhite,
          viewId = R.id.btn_add_birthday,
          intent = createIntent(context, Direction.ADD_BIRTHDAY)
        )
      } else {
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_clock_alarm,
          color = R.color.pureBlack,
          viewId = R.id.btn_add_reminder,
          intent = createIntent(context, Direction.ADD_REMINDER)
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_note,
          color = R.color.pureBlack,
          viewId = R.id.btn_add_note,
          intent = createIntent(context, Direction.ADD_NOTE)
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_food_cake,
          color = R.color.pureBlack,
          viewId = R.id.btn_add_birthday,
          intent = createIntent(context, Direction.ADD_BIRTHDAY)
        )
      }

      appWidgetManager.updateAppWidget(prefsProvider.widgetId, rv)
    }

    private fun createIntent(context: Context, direction: Direction): Intent {
      return AppWidgetActionActivity.createIntent(context).apply {
        putExtra(AppWidgetActionActivity.DIRECTION, direction)
      }
    }
  }
}
