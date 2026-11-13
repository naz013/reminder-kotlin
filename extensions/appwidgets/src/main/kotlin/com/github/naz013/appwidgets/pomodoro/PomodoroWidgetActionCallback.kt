package com.github.naz013.appwidgets.pomodoro

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

internal class PomodoroWidgetActionCallback : ActionCallback, KoinComponent {

  private val actionHandler by inject<PomodoroWidgetActionHandler>()
  private val analyticsEventSender by inject<AnalyticsEventSender>()

  override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
    val action = parameters[ACTION_KEY]?.let { name -> PomodoroWidgetAction.entries.firstOrNull { it.name == name } }
    if (action == null) {
      Logger.w(TAG, "Missing pomodoro action")
      return
    }
    // The engine refreshes the widget itself on every state change.
    if (actionHandler.handle(action)) {
      analyticsEventSender.send(WidgetInteractedEvent(Widget.POMODORO))
    }
  }

  companion object {
    private const val TAG = "PomodoroWidgetCallback"
    val ACTION_KEY = ActionParameters.Key<String>("arg_pomodoro_action")
  }
}
