package com.github.naz013.appwidgets.nextreminder

import com.github.naz013.appwidgets.events.UiReminderWidgetListAdapter
import com.github.naz013.appwidgets.events.data.DateSorted
import com.github.naz013.appwidgets.events.data.UiReminderWidgetList
import com.github.naz013.appwidgets.events.data.UiReminderWidgetShopList
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderV2Repository

internal class NextReminderAppWidgetViewModel(
  private val prefsProvider: NextReminderWidgetPrefsProvider,
  private val reminderV2Repository: ReminderV2Repository,
  private val uiReminderWidgetListAdapter: UiReminderWidgetListAdapter
) {

  suspend fun getState(): NextReminderAppWidgetState {
    Logger.d(TAG, "Get state: ${prefsProvider.widgetId}")
    return if (prefsProvider.widgetId <= -2) {
      getPreviewState()
    } else {
      val next = getNextReminder()
      NextReminderAppWidgetState(
        widgetId = prefsProvider.widgetId,
        backgroundColor = prefsProvider.getWidgetBackground(),
        uuId = next?.uuId,
        title = next?.title(),
        dateTime = next?.dateTime()
      )
    }
  }

  private suspend fun getNextReminder(): DateSorted? {
    return reminderV2Repository.getAll(active = true, removed = false)
      .map { uiReminderWidgetListAdapter.createV2(it) }
      .minByOrNull { it.millis }
  }

  private fun getPreviewState(): NextReminderAppWidgetState {
    return NextReminderAppWidgetState(
      widgetId = prefsProvider.widgetId,
      backgroundColor = prefsProvider.getWidgetBackground(),
      uuId = null,
      title = "Call to Person",
      dateTime = "Today, 18:00"
    )
  }

  companion object {
    private const val TAG = "NextReminderAppWidgetViewModel"
  }
}

internal fun DateSorted.title(): String = when (this) {
  is UiReminderWidgetList -> text
  is UiReminderWidgetShopList -> text
  else -> ""
}

internal fun DateSorted.dateTime(): String? = when (this) {
  is UiReminderWidgetList -> dateTime
  is UiReminderWidgetShopList -> dateTime
  else -> null
}
