package com.github.naz013.appwidgets.calendar

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
import java.util.Calendar
import java.util.GregorianCalendar

internal class CalendarWidgetConfigViewModel(
  private val prefsProvider: CalendarWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  appWidgetPreferences: AppWidgetPreferences,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val composeResourceProvider: ComposeResourceProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(CalendarWidgetConfigState())
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
        foregroundColor = composeResourceProvider.bestForegroundColor(it.palette[index])
      )
    }
  }

  fun onSaveClick() {
    val calendar = GregorianCalendar().apply { timeInMillis = System.currentTimeMillis() }

    prefsProvider.setBackground(state.value.backgroundIndex)
    prefsProvider.setMonth(calendar.get(Calendar.MONTH))
    prefsProvider.setYear(calendar.get(Calendar.YEAR))

    analyticsEventSender.send(WidgetUsedEvent(Widget.CALENDAR))
    appWidgetUpdater.updateCalendarWidget(prefsProvider.widgetId)
  }
}
