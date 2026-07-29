package com.github.naz013.appwidgets.googletasks

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class TasksWidgetConfigViewModel(
  private val context: Context,
  private val prefsProvider: GoogleTasksWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  googleTasksAuthManager: GoogleTasksAuthManager,
) : ViewModel() {

  private val _state = MutableStateFlow(
    TasksWidgetConfigState(
      headerBackgroundIndex = prefsProvider.getHeaderBackground(),
      itemBackgroundIndex = prefsProvider.getItemBackground(),
      isAuthorized = googleTasksAuthManager.isAuthorized(),
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

    analyticsEventSender.send(WidgetUsedEvent(Widget.GOOGLE_TASKS))
    TasksWidget.updateWidget(context, AppWidgetManager.getInstance(context), prefsProvider)
  }
}
