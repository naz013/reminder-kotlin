package com.github.naz013.appwidgets.notes

import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.appwidgets.compose.ComposeResourceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class NotesWidgetConfigViewModel(
  private val prefsProvider: NotesWidgetPrefsProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val analyticsEventSender: AnalyticsEventSender,
  appWidgetPreferences: AppWidgetPreferences,
  private val composeResourceProvider: ComposeResourceProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(NotesWidgetConfigState())
  val state = _state.asStateFlow()

  init {
    _state.update {
      it.copy(
        palette = composeResourceProvider.getBackgroundColors(),
        hapticFeedbackEnabled = appWidgetPreferences.isHapticFeedbackEnabled,
      )
    }
    onBackgroundColorSelected(prefsProvider.getBackground())
  }

  fun onBackgroundColorSelected(index: Int) {
    _state.update {
      it.copy(
        backgroundIndex = index,
        backgroundColor = it.palette[index],
        foregroundColor = composeResourceProvider.bestForegroundColor(it.palette[index]),
      )
    }
  }

  fun onSaveClick() {
    prefsProvider.setBackground(state.value.backgroundIndex)
    analyticsEventSender.send(WidgetUsedEvent(Widget.NOTES))
    appWidgetUpdater.updateNotesWidget(prefsProvider.widgetId)
  }
}
