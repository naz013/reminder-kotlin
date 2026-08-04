package com.github.naz013.appwidgets.events

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.appwidgets.WidgetUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class EventsWidgetConfigViewModel(
  private val context: Context,
  private val prefsProvider: EventsWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val appWidgetUpdater: AppWidgetUpdater,
  appWidgetPreferences: AppWidgetPreferences,
) : ViewModel() {

  private val _state = MutableStateFlow(EventsWidgetConfigState())
  val state = _state.asStateFlow()

  private val _saved = Channel<Unit>(Channel.CONFLATED)
  val saved = _saved.receiveAsFlow()

  init {
    _state.update {
      it.copy(
        headerBackgroundIndex = prefsProvider.getHeaderBackground(),
        itemBackgroundIndex = prefsProvider.getItemBackground(),
        textSize = prefsProvider.getTextSize().takeIf { it != 0f }?.toInt() ?: 14,
        hapticFeedbackEnabled = appWidgetPreferences.isHapticFeedbackEnabled,
      )
    }
    val palette = (0..13).map { WidgetUtils.getComposeColor(it) } +
      WidgetUtils.getDynamicPreviewColor(context)
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
    _state.update { it.copy(isTextSizeDialogVisible = true) }
  }

  fun onTextSizeChanged(size: Int) {
    _state.update { it.copy(textSize = size) }
  }

  fun onTextSizeDialogDismiss() {
    _state.update { it.copy(isTextSizeDialogVisible = false) }
  }

  fun onTextSizeDialogConfirm() {
    _state.update { it.copy(isTextSizeDialogVisible = false) }
    viewModelScope.launch {
      prefsProvider.setHeaderBackground(state.value.headerBackgroundIndex)
      prefsProvider.setItemBackground(state.value.itemBackgroundIndex)
      prefsProvider.setTextSize(state.value.textSize.toFloat())

      analyticsEventSender.send(WidgetUsedEvent(Widget.EVENTS))
      appWidgetUpdater.updateEventsWidget(prefsProvider.widgetId)

      _saved.trySend(Unit)
    }
  }
}
