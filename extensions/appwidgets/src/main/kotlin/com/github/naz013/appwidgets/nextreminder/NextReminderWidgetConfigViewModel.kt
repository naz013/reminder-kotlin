package com.github.naz013.appwidgets.nextreminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.appwidgets.compose.ComposeResourceProvider
import com.github.naz013.logging.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class NextReminderWidgetConfigViewModel(
  private val composeResourceProvider: ComposeResourceProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val prefsProvider: NextReminderWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  appWidgetPreferences: AppWidgetPreferences,
) : ViewModel() {

  private val _state = MutableStateFlow(NextReminderWidgetConfigState())
  val state = _state.asStateFlow()

  private val _saved = Channel<Unit>(Channel.CONFLATED)
  val saved = _saved.receiveAsFlow()

  init {
    _state.update {
      it.copy(
        hapticFeedbackEnabled = appWidgetPreferences.isHapticFeedbackEnabled,
        palette = composeResourceProvider.getBackgroundColors(),
      )
    }
    onBackgroundColorSelected(prefsProvider.getWidgetBackground())
  }

  fun onBackgroundColorSelected(index: Int) {
    _state.update {
      it.copy(
        backgroundIndex = index,
        backgroundColor = it.palette[index],
        contentColor = composeResourceProvider.bestForegroundColor(it.palette[index]),
      )
    }
  }

  fun onSaveClick() {
    Logger.d(TAG, "Saving widget config for widget ID = ${prefsProvider.widgetId}")
    prefsProvider.setWidgetBackground(state.value.backgroundIndex)
    analyticsEventSender.send(WidgetUsedEvent(Widget.NEXT_REMINDER))
    viewModelScope.launch {
      appWidgetUpdater.updateNextReminderWidget(prefsProvider.widgetId)
      _saved.trySend(Unit)
    }
  }

  companion object {
    private const val TAG = "NextReminderWidgetConfigViewModel"
  }
}
