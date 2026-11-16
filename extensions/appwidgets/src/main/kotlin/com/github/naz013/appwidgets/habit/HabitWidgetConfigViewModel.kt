package com.github.naz013.appwidgets.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.appwidgets.compose.ComposeResourceProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HabitWidgetConfigViewModel(
  private val appWidgetUpdater: AppWidgetUpdater,
  private val dispatcherProvider: DispatcherProvider,
  private val prefsProvider: HabitWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  appWidgetPreferences: AppWidgetPreferences,
  private val composeResourceProvider: ComposeResourceProvider
) : ViewModel() {

  private val _state = MutableStateFlow(HabitWidgetConfigState())
  val state = _state.asStateFlow()

  private val _events = Channel<HabitWidgetConfigEvent>(Channel.BUFFERED)
  val events = _events.receiveAsFlow()

  init {
    _state.update {
      it.copy(
        palette = composeResourceProvider.getBackgroundColors(),
        hapticFeedbackEnabled = appWidgetPreferences.isHapticFeedbackEnabled
      )
    }
    onBackgroundColorSelected(prefsProvider.getBackground())
  }

  fun onBackgroundColorSelected(index: Int) {
    _state.update {
      it.copy(
        backgroundIndex = index,
        backgroundColor = it.palette[index],
        foregroundColor = composeResourceProvider.bestForegroundColor(it.palette[index])
      )
    }
  }

  fun onSaveClick() {
    viewModelScope.launch(dispatcherProvider.io()) {
      prefsProvider.setBackground(state.value.backgroundIndex)
      analyticsEventSender.send(WidgetUsedEvent(Widget.HABITS))
      appWidgetUpdater.updateHabitsWidget()
      _events.trySend(HabitWidgetConfigEvent.Saved)
    }
  }
}
