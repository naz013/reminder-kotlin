package com.github.naz013.appwidgets.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TodoWidgetConfigViewModel(
  private val appWidgetUpdater: AppWidgetUpdater,
  private val dispatcherProvider: DispatcherProvider,
  private val prefsProvider: TodoWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val reminderV2Repository: ReminderV2Repository
) : ViewModel() {

  private val _state = MutableStateFlow(TodoWidgetConfigState())
  val state = _state.asStateFlow()

  private val _events = Channel<TodoWidgetConfigEvent>(Channel.BUFFERED)
  val events = _events.receiveAsFlow()

  init {
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminders = reminderV2Repository.getAll(active = true, removed = false)
        .filter { it.action is ReminderAction.Shopping }
        .map { reminder ->
          TodoReminderItem(
            id = reminder.uuId,
            summary = reminder.summary,
            itemsCount = reminder.shoppingItems.count { !it.isDeleted }
          )
        }
        .filter { it.itemsCount > 0 }
      val storedId = prefsProvider.getReminderId()?.takeIf { id -> reminders.any { it.id == id } }
      _state.update { it.copy(reminders = reminders, selectedReminderId = storedId) }
    }
  }

  fun onReminderSelected(id: String) {
    _state.update { it.copy(selectedReminderId = id) }
  }

  fun onSaveClick() {
    val reminderId = state.value.selectedReminderId
    if (reminderId == null) {
      _events.trySend(TodoWidgetConfigEvent.ReminderNotSelected)
      return
    }
    viewModelScope.launch(dispatcherProvider.io()) {
      prefsProvider.setReminderId(reminderId)
      analyticsEventSender.send(WidgetUsedEvent(Widget.TODO))
      appWidgetUpdater.updateAllWidgets()
      _events.trySend(TodoWidgetConfigEvent.Saved)
    }
  }
}
