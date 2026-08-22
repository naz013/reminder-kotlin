package com.github.naz013.feature.routine.execution

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.logic.routine.RoutineDurationCalculator
import com.github.naz013.logic.routine.usecase.RecordRoutineExecutionUseCase
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.ui.common.preferences.AppPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Drives the focus runner: a per-step countdown that survives backgrounding by tracking an
 * absolute wall-clock deadline ([stepDeadlineAtMillis]) rather than decrementing a counter each
 * tick, so the displayed time stays correct even if the process is merely backgrounded for a
 * while (see the design doc's backgrounding note). Auto-advancing when a timed step's countdown
 * reaches zero counts that step as completed - the allotted time ran out and the runner moved on,
 * matching typical habit/Pomodoro-timer UX.
 */
internal class RoutineExecutionViewModel(
  private val id: String,
  private val dispatcherProvider: DispatcherProvider,
  private val routineRepository: RoutineRepository,
  private val recordRoutineExecutionUseCase: RecordRoutineExecutionUseCase,
  private val routineDurationCalculator: RoutineDurationCalculator,
  private val appPreferences: AppPreferences,
) : ViewModel() {

  private val _state = MutableStateFlow<RoutineExecutionState>(RoutineExecutionState.Loading)
  val state = _state.stateInWhileSubscribed(RoutineExecutionState.Loading)
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()
  val stepTransitionEvent: LiveData<Event<Unit>> field = mutableLiveEventOf()

  private var routine: Routine? = null
  private var steps: List<RoutineStep> = emptyList()
  private var stepIndex = 0
  private val completedStepIds = mutableSetOf<String>()

  private var stepDeadlineAtMillis = 0L
  private var pausedRemainingMillis: Long? = null
  private var hasAutoAdvancedForStep = false
  private var sessionStartedAtMillis = 0L
  private var pauseStartedAtMillis: Long? = null
  private var totalPausedMillis = 0L
  private var hasRecorded = false

  private val toneGenerator: ToneGenerator? by lazy {
    runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, TONE_VOLUME) }.getOrNull()
  }

  init {
    load()
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val loaded = routineRepository.getById(id)
      if (loaded == null || loaded.sortedSteps.isEmpty()) {
        Logger.w(TAG, "Routine not found or has no steps, id: $id")
        withContext(dispatcherProvider.main()) { navigationEvent.emit(NavigationEvent.Back) }
        return@launch
      }
      routine = loaded
      steps = loaded.sortedSteps
      sessionStartedAtMillis = System.currentTimeMillis()
      startStep(0)
      startTicker()
    }
  }

  private fun startTicker() {
    viewModelScope.launch(dispatcherProvider.main()) {
      while (isActive) {
        delay(TICK_MS)
        tick()
      }
    }
  }

  private fun startStep(index: Int) {
    stepIndex = index
    hasAutoAdvancedForStep = false
    pausedRemainingMillis = null
    val step = steps[index]
    if (step.durationSeconds > 0) {
      stepDeadlineAtMillis = System.currentTimeMillis() + step.durationSeconds * 1000L
    }
    publishRunningState()
  }

  private fun currentRemainingMillis(): Long {
    val step = steps.getOrNull(stepIndex)
    if (step == null || step.durationSeconds <= 0) return 0L
    return pausedRemainingMillis ?: (stepDeadlineAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
  }

  private fun publishRunningState() {
    val loadedRoutine = routine ?: return
    val step = steps.getOrNull(stepIndex) ?: return
    val remainingSeconds = (currentRemainingMillis() / 1000L).toInt()
    _state.update {
      RoutineExecutionState.Running(
        routineTitle = loadedRoutine.title,
        stepIndex = stepIndex,
        stepCount = steps.size,
        stepTitle = step.title,
        scheduledTimeLabel = step.scheduledTime,
        isTimed = step.durationSeconds > 0,
        remainingSeconds = remainingSeconds,
        timeLabel = formatCountdown(remainingSeconds),
        progress = if (step.durationSeconds > 0) remainingSeconds / step.durationSeconds.toFloat() else 0f,
        isPaused = pausedRemainingMillis != null,
        isFirstStep = stepIndex == 0,
      )
    }
  }

  private fun tick() {
    val running = _state.value as? RoutineExecutionState.Running ?: return
    if (running.isPaused || !running.isTimed) return
    publishRunningState()
    val remainingMillis = stepDeadlineAtMillis - System.currentTimeMillis()
    if (remainingMillis <= 0 && !hasAutoAdvancedForStep) {
      hasAutoAdvancedForStep = true
      if (routine?.autoAdvance == true) {
        advance(markCompleted = true)
      }
    }
  }

  fun onPlayPauseClick() {
    val step = steps.getOrNull(stepIndex) ?: return
    if (step.durationSeconds <= 0) return
    val remaining = pausedRemainingMillis
    if (remaining != null) {
      stepDeadlineAtMillis = System.currentTimeMillis() + remaining
      pausedRemainingMillis = null
      pauseStartedAtMillis?.let { totalPausedMillis += System.currentTimeMillis() - it }
      pauseStartedAtMillis = null
    } else {
      pausedRemainingMillis = currentRemainingMillis()
      pauseStartedAtMillis = System.currentTimeMillis()
    }
    publishRunningState()
  }

  fun onAddMinuteClick() {
    val step = steps.getOrNull(stepIndex) ?: return
    if (step.durationSeconds <= 0) return
    val remaining = pausedRemainingMillis
    if (remaining != null) {
      pausedRemainingMillis = remaining + ADD_MINUTE_MILLIS
    } else {
      stepDeadlineAtMillis += ADD_MINUTE_MILLIS
    }
    publishRunningState()
  }

  fun onSkipClick() {
    advance(markCompleted = false)
  }

  fun onCompleteStepClick() {
    advance(markCompleted = true)
  }

  fun onPreviousStepClick() {
    if (stepIndex == 0) return
    startStep(stepIndex - 1)
  }

  private fun advance(markCompleted: Boolean) {
    val step = steps.getOrNull(stepIndex) ?: return
    if (markCompleted) completedStepIds += step.id
    notifyStepTransition()
    if (stepIndex >= steps.lastIndex) {
      finishNaturally()
    } else {
      startStep(stepIndex + 1)
    }
  }

  private fun notifyStepTransition() {
    if (appPreferences.hapticsEnabled) {
      stepTransitionEvent.emit(Unit)
    }
    if (routine?.soundAlertsEnabled == true) {
      runCatching { toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, TONE_DURATION_MS) }
    }
  }

  private fun elapsedActiveSeconds(): Int {
    val activeMillis = (System.currentTimeMillis() - sessionStartedAtMillis - totalPausedMillis).coerceAtLeast(0L)
    return (activeMillis / 1000L).toInt()
  }

  private fun finishNaturally() {
    if (hasRecorded) return
    hasRecorded = true
    val totalSeconds = elapsedActiveSeconds()
    val totalStepsCount = steps.size
    viewModelScope.launch(dispatcherProvider.io()) {
      recordRoutineExecutionUseCase(
        routineId = id,
        completedStepIds = completedStepIds.toList(),
        totalTimeSpentSeconds = totalSeconds,
        totalStepsCount = totalStepsCount,
      )
      withContext(dispatcherProvider.main()) {
        _state.update {
          RoutineExecutionState.Finished(
            totalTimeLabel = routineDurationCalculator.formatDuration(totalSeconds),
            completedCount = completedStepIds.size,
            totalCount = totalStepsCount,
          )
        }
      }
    }
  }

  fun onBackClick() {
    // routine == null covers the race where back is pressed before load() finishes - there is
    // no session to record yet (sessionStartedAtMillis is still unset), so just navigate away.
    if (_state.value is RoutineExecutionState.Finished || hasRecorded || routine == null) {
      navigationEvent.emit(NavigationEvent.Back)
      return
    }
    hasRecorded = true
    val totalSeconds = elapsedActiveSeconds()
    val totalStepsCount = steps.size
    viewModelScope.launch(dispatcherProvider.io()) {
      recordRoutineExecutionUseCase(
        routineId = id,
        completedStepIds = completedStepIds.toList(),
        totalTimeSpentSeconds = totalSeconds,
        totalStepsCount = totalStepsCount,
      )
      withContext(dispatcherProvider.main()) { navigationEvent.emit(NavigationEvent.Back) }
    }
  }

  override fun onCleared() {
    super.onCleared()
    toneGenerator?.release()
  }

  sealed interface NavigationEvent {
    data object Back : NavigationEvent
  }

  companion object {
    private const val TAG = "RoutineExecutionViewModel"
    private const val TICK_MS = 1000L
    private const val ADD_MINUTE_MILLIS = 60_000L
    private const val TONE_VOLUME = 80
    private const val TONE_DURATION_MS = 150
  }
}

private fun formatCountdown(seconds: Int): String {
  val clamped = seconds.coerceAtLeast(0)
  return "%02d:%02d".format(clamped / 60, clamped % 60)
}
