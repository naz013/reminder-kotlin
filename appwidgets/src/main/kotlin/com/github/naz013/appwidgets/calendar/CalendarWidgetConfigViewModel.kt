package com.github.naz013.appwidgets.calendar

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import java.util.GregorianCalendar

internal class CalendarWidgetConfigViewModel(
  private val context: Context,
  private val prefsProvider: CalendarWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {

  private val _state = MutableStateFlow(
    CalendarWidgetConfigState(
      headerBackgroundIndex = prefsProvider.getHeaderBackground(),
      backgroundIndex = prefsProvider.getBackground(),
    )
  )
  val state = _state.asStateFlow()

  fun onHeaderColorSelected(index: Int) {
    _state.update { it.copy(headerBackgroundIndex = index) }
  }

  fun onBackgroundColorSelected(index: Int) {
    _state.update { it.copy(backgroundIndex = index) }
  }

  fun onSaveClick() {
    val calendar = GregorianCalendar().apply { timeInMillis = System.currentTimeMillis() }

    prefsProvider.setBackground(state.value.backgroundIndex)
    prefsProvider.setHeaderBackground(state.value.headerBackgroundIndex)
    prefsProvider.setMonth(calendar.get(Calendar.MONTH))
    prefsProvider.setYear(calendar.get(Calendar.YEAR))

    analyticsEventSender.send(WidgetUsedEvent(Widget.CALENDAR))
    CalendarWidget.updateWidget(context, AppWidgetManager.getInstance(context), prefsProvider)
  }
}
