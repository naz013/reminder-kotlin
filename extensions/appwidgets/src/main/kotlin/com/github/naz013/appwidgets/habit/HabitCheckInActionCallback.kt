package com.github.naz013.appwidgets.habit

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetInteractedEvent
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class HabitCheckInActionCallback : ActionCallback, KoinComponent {

  private val habitCheckInHandler by inject<HabitCheckInHandler>()
  private val analyticsEventSender by inject<AnalyticsEventSender>()

  override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
    val habitId = parameters[HABIT_ID_KEY]
    if (habitId.isNullOrEmpty()) {
      Logger.w(TAG, "Missing habit id")
      return
    }
    // The check-in use cases refresh the widget themselves.
    if (habitCheckInHandler.checkIn(habitId)) {
      analyticsEventSender.send(WidgetInteractedEvent(Widget.HABITS))
    }
  }

  companion object {
    private const val TAG = "HabitCheckInCallback"
    val HABIT_ID_KEY = ActionParameters.Key<String>("arg_habit_id")
  }
}
