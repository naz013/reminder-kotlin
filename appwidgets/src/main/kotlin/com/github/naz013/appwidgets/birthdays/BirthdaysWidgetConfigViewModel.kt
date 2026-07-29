package com.github.naz013.appwidgets.birthdays

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class BirthdaysWidgetConfigViewModel(
  private val context: Context,
  private val prefsProvider: BirthdaysWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {

  private val _state = MutableStateFlow(
    BirthdaysWidgetConfigState(
      headerBackgroundIndex = prefsProvider.getHeaderBackground(),
      itemBackgroundIndex = prefsProvider.getItemBackground(),
    )
  )
  val state = _state.asStateFlow()

  fun onHeaderColorSelected(index: Int) {
    _state.update { it.copy(headerBackgroundIndex = index) }
  }

  fun onItemColorSelected(index: Int) {
    _state.update { it.copy(itemBackgroundIndex = index) }
  }

  fun onSaveClick() {
    prefsProvider.setHeaderBackground(state.value.headerBackgroundIndex)
    prefsProvider.setItemBackground(state.value.itemBackgroundIndex)

    analyticsEventSender.send(WidgetUsedEvent(Widget.BIRTHDAYS))
    BirthdaysWidget.updateWidget(context, AppWidgetManager.getInstance(context), prefsProvider)
  }
}
