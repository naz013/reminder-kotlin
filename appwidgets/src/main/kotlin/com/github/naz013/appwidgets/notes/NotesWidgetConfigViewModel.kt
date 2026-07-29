package com.github.naz013.appwidgets.notes

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class NotesWidgetConfigViewModel(
  private val context: Context,
  private val prefsProvider: NotesWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {

  private val _state = MutableStateFlow(
    NotesWidgetConfigState(backgroundIndex = prefsProvider.getHeaderBackground())
  )
  val state = _state.asStateFlow()

  fun onBackgroundColorSelected(index: Int) {
    _state.update { it.copy(backgroundIndex = index) }
  }

  fun onSaveClick() {
    prefsProvider.setHeaderBackground(state.value.backgroundIndex)
    analyticsEventSender.send(WidgetUsedEvent(Widget.NOTES))
    NotesWidget.updateWidget(context, AppWidgetManager.getInstance(context), prefsProvider)
  }
}
