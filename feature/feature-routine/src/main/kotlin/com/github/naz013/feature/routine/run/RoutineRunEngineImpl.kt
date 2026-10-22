package com.github.naz013.feature.routine.run

import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.logic.routine.usecase.RecordRoutineExecutionUseCase
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.ui.common.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Tracks an absolute wall-clock deadline ([stepDeadlineAtMillis]) rather than decrementing a counter,
 * so the countdown stays correct while the process is backgrounded. Auto-advancing when a timed step
 * reaches zero counts that step as completed. All commands and ticks run on the main dispatcher.
 */
internal class RoutineRunEngineImpl(
  private val dispatcherProvider: DispatcherProvider,
  private val routineRepository: RoutineRepository,
  private val recordRoutineExecutionUseCase: RecordRoutineExecutionUseCase,
  private val appPreferences: AppPreferences,
  private val foregroundNotifier: RoutineRunForegroundNotifier,
  private val tonePlayer: StepTonePlayer = ToneGeneratorStepTonePlayer(),
  private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : RoutineRunEngine {

  private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.main())
  private var runJob: Job? = null

  private val _state = MutableStateFlow<RoutineRunState>(RoutineRunState.Idle)
  override val state: StateFlow<RoutineRunState> = _state.asStateFlow()

  private val _stepTransitions = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  override val stepTransitions: Flow<Unit> = _stepTransitions.asSharedFlow()

  private var routineId = ""
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
  private var latestAttachToken = 0L

  override fun start(routineId: String): Long {
    val token = ++latestAttachToken
    val current = _state.value
    val isSameActiveRun = (current is RoutineRunState.Loading && current.routineId == routineId) ||
      (current is RoutineRunState.Running && current.routineId == routineId)
    if (isSameActiveRun) return token
    if (current !is RoutineRunState.Idle) stop()
    Logger.d(TAG, "Start run, routineId=$routineId")
    this.routineId = routineId
    _state.value = RoutineRunState.Loading(routineId)
    runJob = scope.launch {
      val loaded = withContext(dispatcherProvider.io()) { routineRepository.getById(routineId) }
      if (loaded == null || loaded.sortedSteps.isEmpty()) {
        Logger.w(TAG, "Routine not found or has no steps, id: $routineId")
        _state.value = RoutineRunState.NotFound(routineId)
        return@launch
      }
      routine = loaded
      steps = loaded.sortedSteps
      sessionStartedAtMillis = currentTimeMillis()
      startStep(0)
      runCatching { foregroundNotifier.start() }
        .onFailure { Logger.w(TAG, "Foreground notifier failed to start: ${it.message}") }
      while (isActive) {
        delay(TICK_MS)
        tick()
      }
    }
    return token
  }

  override fun release(token: Long) {
    if (token == latestAttachToken) stop()
  }

  override fun pause() {
    if (!isCurrentStepTimed() || pausedRemainingMillis != null) return
    pausedRemainingMillis = currentRemainingMillis()
    pauseStartedAtMillis = currentTimeMillis()
    publishRunningState()
  }

  override fun resume() {
    val remaining = pausedRemainingMillis ?: return
    if (!isCurrentStepTimed()) return
    stepDeadlineAtMillis = currentTimeMillis() + remaining
    pausedRemainingMillis = null
    accountPausedTime()
    publishRunningState()
  }

  override fun addMinute() {
    if (!isCurrentStepTimed()) return
    val remaining = pausedRemainingMillis
    if (remaining != null) {
      pausedRemainingMillis = remaining + ADD_MINUTE_MILLIS
    } else {
      stepDeadlineAtMillis += ADD_MINUTE_MILLIS
    }
    publishRunningState()
  }

  override fun skip() {
    advance(markCompleted = false)
  }

  override fun completeStep() {
    advance(markCompleted = true)
  }

  override fun previous() {
    if (_state.value !is RoutineRunState.Running || stepIndex == 0) return
    startStep(stepIndex - 1)
  }

  override fun stop() {
    val current = _state.value
    if (current is RoutineRunState.Idle) return
    Logger.d(TAG, "Stop run, routineId=$routineId")
    runJob?.cancel()
    runJob = null
    if (current is RoutineRunState.Running) {
      accountPausedTime()
      record()
    }
    resetRun()
    _state.value = RoutineRunState.Idle
    stopForegroundNotifier()
  }

  private fun stopForegroundNotifier() {
    runCatching { foregroundNotifier.stop() }
      .onFailure { Logger.w(TAG, "Foreground notifier failed to stop: ${it.message}") }
  }

  private fun isCurrentStepTimed(): Boolean {
    if (_state.value !is RoutineRunState.Running) return false
    return (steps.getOrNull(stepIndex)?.durationSeconds ?: 0) > 0
  }

  private fun accountPausedTime() {
    pauseStartedAtMillis?.let { totalPausedMillis += currentTimeMillis() - it }
    pauseStartedAtMillis = null
  }

  private fun startStep(index: Int) {
    accountPausedTime()
    stepIndex = index
    hasAutoAdvancedForStep = false
    pausedRemainingMillis = null
    val step = steps[index]
    if (step.durationSeconds > 0) {
      stepDeadlineAtMillis = currentTimeMillis() + step.durationSeconds * 1000L
    }
    publishRunningState()
  }

  private fun currentRemainingMillis(): Long {
    val step = steps.getOrNull(stepIndex)
    if (step == null || step.durationSeconds <= 0) return 0L
    return pausedRemainingMillis ?: (stepDeadlineAtMillis - currentTimeMillis()).coerceAtLeast(0L)
  }

  private fun publishRunningState() {
    val loadedRoutine = routine ?: return
    val step = steps.getOrNull(stepIndex) ?: return
    _state.value = RoutineRunState.Running(
      routineId = routineId,
      routineTitle = loadedRoutine.title,
      stepIndex = stepIndex,
      stepCount = steps.size,
      stepTitle = step.title,
      scheduledTimeLabel = step.scheduledTime,
      isTimed = step.durationSeconds > 0,
      remainingSeconds = (currentRemainingMillis() / 1000L).toInt(),
      stepTotalSeconds = step.durationSeconds,
      isPaused = pausedRemainingMillis != null,
      stepEndsAtMillis = stepEndsAtMillis(step),
      completedCount = completedStepIds.size,
    )
  }

  private fun stepEndsAtMillis(step: RoutineStep): Long? {
    return if (step.durationSeconds > 0 && pausedRemainingMillis == null) stepDeadlineAtMillis else null
  }

  private fun tick() {
    val running = _state.value as? RoutineRunState.Running ?: return
    if (running.isPaused || !running.isTimed) return
    publishRunningState()
    val remainingMillis = stepDeadlineAtMillis - currentTimeMillis()
    if (remainingMillis <= 0 && !hasAutoAdvancedForStep) {
      hasAutoAdvancedForStep = true
      if (routine?.autoAdvance == true) {
        advance(markCompleted = true)
      }
    }
  }

  private fun advance(markCompleted: Boolean) {
    if (_state.value !is RoutineRunState.Running) return
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
      _stepTransitions.tryEmit(Unit)
    }
    if (routine?.soundAlertsEnabled == true) {
      tonePlayer.play()
    }
  }

  private fun elapsedActiveSeconds(): Int {
    val activeMillis = (currentTimeMillis() - sessionStartedAtMillis - totalPausedMillis).coerceAtLeast(0L)
    return (activeMillis / 1000L).toInt()
  }

  private fun finishNaturally() {
    accountPausedTime()
    val totalSeconds = elapsedActiveSeconds()
    val completedCount = completedStepIds.size
    val totalCount = steps.size
    record()
    runJob?.cancel()
    runJob = null
    _state.value = RoutineRunState.Finished(
      routineId = routineId,
      totalTimeSeconds = totalSeconds,
      completedCount = completedCount,
      totalCount = totalCount,
    )
    tonePlayer.release()
    stopForegroundNotifier()
  }

  private fun record() {
    if (hasRecorded) return
    hasRecorded = true
    val id = routineId
    val completed = completedStepIds.toList()
    val totalSeconds = elapsedActiveSeconds()
    val totalStepsCount = steps.size
    scope.launch(dispatcherProvider.io()) {
      recordRoutineExecutionUseCase(
        routineId = id,
        completedStepIds = completed,
        totalTimeSpentSeconds = totalSeconds,
        totalStepsCount = totalStepsCount,
      )
    }
  }

  private fun resetRun() {
    routine = null
    steps = emptyList()
    stepIndex = 0
    completedStepIds.clear()
    stepDeadlineAtMillis = 0L
    pausedRemainingMillis = null
    pauseStartedAtMillis = null
    totalPausedMillis = 0L
    hasAutoAdvancedForStep = false
    hasRecorded = false
    tonePlayer.release()
  }

  private companion object {
    private const val TAG = "RoutineRunEngine"
    private const val TICK_MS = 1000L
    private const val ADD_MINUTE_MILLIS = 60_000L
  }
}
