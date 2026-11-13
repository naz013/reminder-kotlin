package com.github.naz013.appwidgets.routine

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.routine.Routine
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.ui.routine.RoutineColors
import com.github.naz013.ui.routine.RoutineIconSet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class RoutineWidgetConfigViewModel(
  private val appWidgetUpdater: AppWidgetUpdater,
  private val dispatcherProvider: DispatcherProvider,
  private val prefsProvider: RoutineWidgetPrefsProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val routineRepository: RoutineRepository
) : ViewModel() {

  private val _state = MutableStateFlow(
    RoutineWidgetConfigState(selectedRoutineId = prefsProvider.getRoutineId())
  )
  val state = _state.asStateFlow()

  private val _events = Channel<RoutineWidgetConfigEvent>(Channel.BUFFERED)
  val events = _events.receiveAsFlow()

  init {
    viewModelScope.launch {
      val options = withContext(dispatcherProvider.io()) {
        routineRepository.getAll()
          .sortedWith(compareByDescending<Routine> { it.isPinned }.thenBy { it.title })
          .map {
            UiRoutineWidgetOption(
              id = it.id,
              name = it.title,
              iconRes = it.icon?.let { index -> RoutineIconSet.ALL.getOrNull(index) },
              color = RoutineColors.ALL.getOrElse(it.color) { RoutineColors.ALL.first() }.toArgb()
            )
          }
      }
      _state.update { it.copy(routines = options) }
    }
  }

  fun onRoutineSelected(id: String?) {
    _state.update { it.copy(selectedRoutineId = id) }
  }

  fun onSaveClick() {
    viewModelScope.launch(dispatcherProvider.io()) {
      prefsProvider.setRoutineId(state.value.selectedRoutineId)
      analyticsEventSender.send(WidgetUsedEvent(Widget.ROUTINE))
      appWidgetUpdater.updateRoutineWidget(prefsProvider.widgetId)
      _events.trySend(RoutineWidgetConfigEvent.Saved)
    }
  }
}
