package com.github.naz013.appwidgets.notes

import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.appwidgets.WidgetUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class NotesWidgetConfigViewModel(
  private val appWidgetUpdater: AppWidgetUpdater,
  private val prefsProvider: NotesWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  appWidgetPreferences: AppWidgetPreferences,
) : ViewModel() {

  private val _state = MutableStateFlow(NotesWidgetConfigState())
  val state = _state.asStateFlow()

  init {
    _state.update {
      it.copy(
        backgroundIndex = prefsProvider.getHeaderBackground(),
        hapticFeedbackEnabled = appWidgetPreferences.isHapticFeedbackEnabled,
      )
    }
    val palette = (0..13).map { WidgetUtils.getComposeColor(it) }
    _state.update {
      it.copy(
        palette = palette,
        headerColor = palette[it.backgroundIndex],
        contentColor = WidgetUtils.getContrastColor(it.backgroundIndex),
      )
    }
  }

  fun onBackgroundColorSelected(index: Int) {
    _state.update {
      it.copy(
        backgroundIndex = index,
        headerColor = it.palette[index],
        contentColor = WidgetUtils.getContrastColor(index),
      )
    }
  }

  fun onSaveClick() {
    prefsProvider.setHeaderBackground(state.value.backgroundIndex)
    analyticsEventSender.send(WidgetUsedEvent(Widget.NOTES))
    appWidgetUpdater.updateNotesWidget(prefsProvider.widgetId)
  }
}
