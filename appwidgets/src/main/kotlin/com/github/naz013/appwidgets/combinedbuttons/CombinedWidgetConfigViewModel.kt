package com.github.naz013.appwidgets.combinedbuttons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.appwidgets.WidgetUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CombinedWidgetConfigViewModel(
  private val appWidgetUpdater: AppWidgetUpdater,
  private val prefsProvider: CombinedWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  appWidgetPreferences: AppWidgetPreferences,
) : ViewModel() {

  private val _state = MutableStateFlow(CombinedWidgetConfigState())
  val state = _state.asStateFlow()

  init {
    _state.update {
      it.copy(
        backgroundIndex = prefsProvider.getWidgetBackground(),
        hapticFeedbackEnabled = appWidgetPreferences.isHapticFeedbackEnabled,
      )
    }
    val palette = (0..13).map { WidgetUtils.getComposeColor(it) }
    _state.update {
      it.copy(
        palette = palette,
        backgroundColor = palette[it.backgroundIndex],
        contentColor = WidgetUtils.getContrastColor(it.backgroundIndex),
      )
    }
  }

  fun onBackgroundColorSelected(index: Int) {
    _state.update {
      it.copy(
        backgroundIndex = index,
        backgroundColor = it.palette[index],
        contentColor = WidgetUtils.getContrastColor(index),
      )
    }
  }

  fun onSaveClick() {
    prefsProvider.setWidgetBackground(state.value.backgroundIndex)
    analyticsEventSender.send(WidgetUsedEvent(Widget.COMBINED))
    viewModelScope.launch {
      appWidgetUpdater.updateCombinedButtonsWidget(prefsProvider.widgetId)
    }
  }
}
