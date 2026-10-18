package com.github.naz013.appwidgets.todo

import com.github.naz013.appwidgets.events.data.UiShopListWidget
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.ui.common.icon.DrawableCatalog

internal class TodoAppWidgetViewModel(
  private val prefsProvider: TodoWidgetPrefsProvider,
  private val reminderV2Repository: ReminderV2Repository
) {

  suspend fun getState(): TodoAppWidgetState {
    val widgetId = prefsProvider.widgetId
    val reminder = prefsProvider.getReminderId()
      ?.let { reminderV2Repository.getById(it) }
      ?.takeIf { it.isActive && !it.isRemoved }
      ?: return TodoAppWidgetState(widgetId = widgetId)
    return TodoAppWidgetState(
      widgetId = widgetId,
      reminderId = reminder.uuId,
      title = reminder.summary,
      items = reminder.toWidgetItems()
    )
  }

  private fun ReminderV2.toWidgetItems(): List<UiShopListWidget> {
    return shoppingItems.filterNot { it.isDeleted }.map {
      UiShopListWidget(
        iconRes = if (it.isChecked) {
          DrawableCatalog.Fluent.CheckboxChecked
        } else {
          DrawableCatalog.Fluent.CheckboxUnchecked
        },
        text = it.summary,
        id = it.uuId
      )
    }
  }
}
