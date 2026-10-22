package com.github.naz013.feature.routine.execution

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.feature.routine.run.RoutineRunEngine
import com.github.naz013.feature.routine.run.RoutineRunState
import com.github.naz013.logic.routine.RoutineDurationCalculator
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Renders [RoutineRunEngine] state for the focus screen and forwards user actions to it. The run
 * itself lives in the engine so it survives this screen and the app being backgrounded.
 */
internal class RoutineExecutionViewModel(
  private val id: String,
  private val engine: RoutineRunEngine,
  private val routineDurationCalculator: RoutineDurationCalculator,
) : ViewModel() {

  val state = engine.state
    .filter { it !is RoutineRunState.Idle }
    .map(::toUiState)
    .stateInWhileSubscribed(RoutineExecutionState.Loading)
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()
  val stepTransitionEvent: LiveData<Event<Unit>> field = mutableLiveEventOf()

  private val attachToken: Long = engine.start(id)

  private var hasShownRunning = false
  private var isExiting = false

  init {
    viewModelScope.launch {
      engine.state.collect(::onEngineState)
    }
    viewModelScope.launch {
      engine.stepTransitions.collect { stepTransitionEvent.emit(Unit) }
    }
  }

  private fun onEngineState(runState: RoutineRunState) {
    when (runState) {
      is RoutineRunState.Running -> hasShownRunning = true
      is RoutineRunState.Finished -> hasShownRunning = false
      is RoutineRunState.NotFound -> if (runState.routineId == id) {
        engine.stop()
        exit()
      }
      is RoutineRunState.Idle -> if (hasShownRunning) exit()
      else -> Unit
    }
  }

  private fun exit() {
    if (isExiting) return
    isExiting = true
    navigationEvent.emit(NavigationEvent.Back)
  }

  private fun toUiState(runState: RoutineRunState): RoutineExecutionState {
    return when (runState) {
      is RoutineRunState.Running -> RoutineExecutionState.Running(
        routineTitle = runState.routineTitle,
        stepIndex = runState.stepIndex,
        stepCount = runState.stepCount,
        stepTitle = runState.stepTitle,
        scheduledTimeLabel = runState.scheduledTimeLabel,
        isTimed = runState.isTimed,
        remainingSeconds = runState.remainingSeconds,
        timeLabel = formatCountdown(runState.remainingSeconds),
        progress = if (runState.isTimed) runState.remainingSeconds / runState.stepTotalSeconds.toFloat() else 0f,
        isPaused = runState.isPaused,
        isFirstStep = runState.stepIndex == 0,
      )

      is RoutineRunState.Finished -> RoutineExecutionState.Finished(
        totalTimeLabel = routineDurationCalculator.formatDuration(runState.totalTimeSeconds),
        completedCount = runState.completedCount,
        totalCount = runState.totalCount,
      )

      else -> RoutineExecutionState.Loading
    }
  }

  fun onPlayPauseClick() {
    val running = engine.state.value as? RoutineRunState.Running ?: return
    if (running.isPaused) engine.resume() else engine.pause()
  }

  fun onAddMinuteClick() {
    engine.addMinute()
  }

  fun onSkipClick() {
    engine.skip()
  }

  fun onCompleteStepClick() {
    engine.completeStep()
  }

  fun onPreviousStepClick() {
    engine.previous()
  }

  fun onBackClick() {
    isExiting = true
    engine.stop()
    navigationEvent.emit(NavigationEvent.Back)
  }

  override fun onCleared() {
    super.onCleared()
    engine.release(attachToken)
  }

  sealed interface NavigationEvent {
    data object Back : NavigationEvent
  }
}

private fun formatCountdown(seconds: Int): String {
  val clamped = seconds.coerceAtLeast(0)
  return "%02d:%02d".format(clamped / 60, clamped % 60)
}
