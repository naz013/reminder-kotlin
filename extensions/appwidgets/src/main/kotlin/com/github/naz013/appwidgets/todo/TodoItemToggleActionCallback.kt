package com.github.naz013.appwidgets.todo

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetInteractedEvent
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.repository.ReminderV2Repository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class TodoItemToggleActionCallback : ActionCallback, KoinComponent {

  private val reminderV2Repository by inject<ReminderV2Repository>()
  private val saveReminderUseCase by inject<SaveReminderUseCase>()
  private val analyticsEventSender by inject<AnalyticsEventSender>()

  override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
    val reminderId = parameters[REMINDER_ID_KEY]
    val itemId = parameters[ITEM_ID_KEY]
    if (reminderId.isNullOrEmpty() || itemId.isNullOrEmpty()) {
      Logger.w(TAG, "Missing reminder or item id")
      return
    }
    val reminder = reminderV2Repository.getById(reminderId) ?: return
    val items = reminder.shoppingItems.map {
      if (it.uuId == itemId) it.copy(isChecked = !it.isChecked) else it
    }
    saveReminderUseCase(reminder.copy(shoppingItems = items))
    TodoGlanceAppWidget().updateAll(context)
    analyticsEventSender.send(WidgetInteractedEvent(Widget.TODO))
  }

  companion object {
    private const val TAG = "TodoItemToggle"
    val REMINDER_ID_KEY = ActionParameters.Key<String>("arg_todo_reminder_id")
    val ITEM_ID_KEY = ActionParameters.Key<String>("arg_todo_item_id")
  }
}
