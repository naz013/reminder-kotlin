package com.github.naz013.appwidgets.combinedbuttons

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class CombinedWidgetConfigViewModel(
  private val context: Context,
  private val prefsProvider: CombinedWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {

  private val _state = MutableStateFlow(
    CombinedWidgetConfigState(backgroundIndex = prefsProvider.getWidgetBackground())
  )
  val state = _state.asStateFlow()

  fun onBackgroundColorSelected(index: Int) {
    _state.update { it.copy(backgroundIndex = index) }
  }

  fun onSaveClick() {
    prefsProvider.setWidgetBackground(state.value.backgroundIndex)
    analyticsEventSender.send(WidgetUsedEvent(Widget.COMBINED))
    CombinedButtonsWidget.updateWidget(context, AppWidgetManager.getInstance(context), prefsProvider)
  }
}
