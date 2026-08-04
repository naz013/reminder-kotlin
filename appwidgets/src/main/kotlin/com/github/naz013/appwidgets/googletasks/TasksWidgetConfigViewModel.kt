package com.github.naz013.appwidgets.googletasks

import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class TasksWidgetConfigViewModel(
  private val appWidgetUpdater: AppWidgetUpdater,
  private val prefsProvider: GoogleTasksWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  googleTasksAuthManager: GoogleTasksAuthManager,
  appWidgetPreferences: AppWidgetPreferences,
) : ViewModel() {

  private val _state = MutableStateFlow(TasksWidgetConfigState())
  val state = _state.asStateFlow()

  init {
    _state.update {
      it.copy(
        headerBackgroundIndex = prefsProvider.getHeaderBackground(),
        itemBackgroundIndex = prefsProvider.getItemBackground(),
        isAuthorized = googleTasksAuthManager.isAuthorized(),
        hapticFeedbackEnabled = appWidgetPreferences.isHapticFeedbackEnabled,
      )
    }
    val palette = (0..13).map { WidgetUtils.getComposeColor(it) }
    _state.update {
      it.copy(
        palette = palette,
        headerColor = palette[it.headerBackgroundIndex],
        headerContentColor = WidgetUtils.getContrastColor(it.headerBackgroundIndex),
        itemColor = palette[it.itemBackgroundIndex],
        itemContentColor = WidgetUtils.getContrastColor(it.itemBackgroundIndex),
      )
    }
  }

  fun onHeaderColorSelected(index: Int) {
    _state.update {
      it.copy(
        headerBackgroundIndex = index,
        headerColor = it.palette[index],
        headerContentColor = WidgetUtils.getContrastColor(index),
      )
    }
  }

  fun onItemColorSelected(index: Int) {
    _state.update {
      it.copy(
        itemBackgroundIndex = index,
        itemColor = it.palette[index],
        itemContentColor = WidgetUtils.getContrastColor(index),
      )
    }
  }

  fun onSaveClick() {
    prefsProvider.setHeaderBackground(state.value.headerBackgroundIndex)
    prefsProvider.setItemBackground(state.value.itemBackgroundIndex)

    analyticsEventSender.send(WidgetUsedEvent(Widget.GOOGLE_TASKS))
    appWidgetUpdater.updateScheduleWidget(prefsProvider.widgetId)
  }
}
