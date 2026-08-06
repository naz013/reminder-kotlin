package com.github.naz013.appwidgets.birthdays

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

internal class BirthdaysWidgetConfigViewModel(
  private val prefsProvider: BirthdaysWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  appWidgetPreferences: AppWidgetPreferences,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val composeResourceProvider: ComposeResourceProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(BirthdaysWidgetConfigState())
  val state = _state.asStateFlow()

  init {
    _state.update {
      it.copy(
        palette = composeResourceProvider.getBackgroundColors(),
        hapticFeedbackEnabled = appWidgetPreferences.isHapticFeedbackEnabled,
      )
    }

    onBackgroundColorSelected(prefsProvider.getWidgetBackground())
  }

  fun onBackgroundColorSelected(index: Int) {
    _state.update {
      it.copy(
        backgroundColorIndex = index,
        backgroundColor = it.palette[index],
        foregroundColor = composeResourceProvider.bestForegroundColor(it.palette[index]),
      )
    }
  }

  fun onSaveClick() {
    prefsProvider.setWidgetBackground(state.value.backgroundColorIndex)

    analyticsEventSender.send(WidgetUsedEvent(Widget.BIRTHDAYS))

    appWidgetUpdater.updateBirthdaysWidget(prefsProvider.widgetId)
  }
}
