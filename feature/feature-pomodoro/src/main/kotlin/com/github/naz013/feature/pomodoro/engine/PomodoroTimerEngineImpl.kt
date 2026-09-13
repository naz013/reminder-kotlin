package com.github.naz013.feature.pomodoro.engine

import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.pomodoro.PomodoroForegroundNotifier
import com.github.naz013.feature.pomodoro.PomodoroPreferences
import com.github.naz013.feature.pomodoro.usecase.RecordPomodoroSessionUseCase
import com.github.naz013.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime

internal class PomodoroTimerEngineImpl(
  private val dispatcherProvider: DispatcherProvider,
  private val pomodoroPreferences: PomodoroPreferences,
  private val pomodoroForegroundNotifier: PomodoroForegroundNotifier,
  private val recordPomodoroSessionUseCase: RecordPomodoroSessionUseCase,
) : PomodoroTimerEngine {

  private val engineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())
  private var tickerStarted = false

  private val _state = MutableStateFlow(PomodoroEngineState())
  override val state: StateFlow<PomodoroEngineState> = _state.asStateFlow()

  private var deadlineAtMillis = 0L
  private var pausedRemainingMillis: Long? = null
  private var workStartedAt: LocalDateTime? = null
  private var plannedWorkSeconds = 0
  private var completedWorkSessionsToday = 0

  override fun start(linkedReminderId: String?) {
    if (_state.value.isRunning) return
    Logger.d(TAG, "Start work session, linkedReminderId=$linkedReminderId")
    beginWork(linkedReminderId)
    ensureTicker()
  }

  override fun pause() {
    val current = _state.value
    if (!current.isRunning || current.isPaused) return
    pausedRemainingMillis = remainingMillis()
    publish(isPaused = true)
  }

  override fun resume() {
    val current = _state.value
    if (!current.isRunning || !current.isPaused) return
    val remaining = pausedRemainingMillis ?: return
    deadlineAtMillis = System.currentTimeMillis() + remaining
    pausedRemainingMillis = null
    publish(isPaused = false)
  }

  override fun skipToBreak() {
    val current = _state.value
    if (current.mode != PomodoroMode.Work) return
    finishWork(wasCompleted = false)
    beginBreak()
  }

  override fun stop(discard: Boolean) {
    Logger.d(TAG, "Stop session, discard=$discard")
    val current = _state.value
    if (current.mode == PomodoroMode.Work && !discard) {
      finishWork(wasCompleted = false)
    }
    pausedRemainingMillis = null
    workStartedAt = null
    _state.value = PomodoroEngineState(completedWorkSessionsToday = completedWorkSessionsToday)
    pomodoroForegroundNotifier.stop()
  }

  private fun beginWork(linkedReminderId: String?) {
    workStartedAt = LocalDateTime.now()
    plannedWorkSeconds = pomodoroPreferences.workDurationMinutes * SECONDS_PER_MINUTE
    deadlineAtMillis = System.currentTimeMillis() + plannedWorkSeconds * 1000L
    pausedRemainingMillis = null
    _state.value = PomodoroEngineState(
      mode = PomodoroMode.Work,
      isRunning = true,
      isPaused = false,
      remainingSeconds = plannedWorkSeconds,
      totalSeconds = plannedWorkSeconds,
      linkedReminderId = linkedReminderId,
      completedWorkSessionsToday = completedWorkSessionsToday,
    )
    pomodoroForegroundNotifier.start()
  }

  private fun beginBreak() {
    val isLongBreak = completedWorkSessionsToday > 0 &&
      completedWorkSessionsToday % pomodoroPreferences.sessionsUntilLongBreak == 0
    val minutes = if (isLongBreak) {
      pomodoroPreferences.longBreakDurationMinutes
    } else {
      pomodoroPreferences.breakDurationMinutes
    }
    val totalSeconds = minutes * SECONDS_PER_MINUTE
    deadlineAtMillis = System.currentTimeMillis() + totalSeconds * 1000L
    pausedRemainingMillis = null
    _state.update {
      it.copy(
        mode = if (isLongBreak) PomodoroMode.LongBreak else PomodoroMode.Break,
        isRunning = true,
        isPaused = false,
        remainingSeconds = totalSeconds,
        totalSeconds = totalSeconds,
        completedWorkSessionsToday = completedWorkSessionsToday,
      )
    }
    if (pomodoroPreferences.autoStartNext) {
      pomodoroForegroundNotifier.start()
    } else {
      pomodoroForegroundNotifier.stop()
    }
  }

  private fun finishWork(wasCompleted: Boolean) {
    val startedAt = workStartedAt ?: return
    val actualSeconds = (plannedWorkSeconds - (remainingMillis() / 1000L).toInt())
      .let { if (wasCompleted) plannedWorkSeconds else it.coerceAtLeast(0) }
    if (wasCompleted) completedWorkSessionsToday += 1
    val reminderId = _state.value.linkedReminderId
    engineScope.launch(dispatcherProvider.io()) {
      recordPomodoroSessionUseCase(
        linkedReminderId = reminderId,
        startedAt = startedAt,
        plannedFocusSeconds = plannedWorkSeconds,
        actualFocusSeconds = actualSeconds,
        wasCompleted = wasCompleted,
      )
    }
    workStartedAt = null
  }

  private fun remainingMillis(): Long =
    pausedRemainingMillis ?: (deadlineAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)

  private fun publish(isPaused: Boolean) {
    _state.update { it.copy(isPaused = isPaused, remainingSeconds = (remainingMillis() / 1000L).toInt()) }
  }

  private fun ensureTicker() {
    if (tickerStarted) return
    tickerStarted = true
    engineScope.launch {
      while (isActive) {
        delay(TICK_MS)
        tick()
      }
    }
  }

  private fun tick() {
    val current = _state.value
    if (!current.isRunning || current.isPaused) return
    val remainingSeconds = (remainingMillis() / 1000L).toInt()
    _state.update { it.copy(remainingSeconds = remainingSeconds) }
    pomodoroForegroundNotifier.update(current.mode, remainingSeconds, isPaused = false)
    if (remainingMillis() <= 0L) {
      onDeadlineReached(current.mode)
    }
  }

  private fun onDeadlineReached(mode: PomodoroMode) {
    when (mode) {
      PomodoroMode.Work -> {
        finishWork(wasCompleted = true)
        beginBreak()
      }
      PomodoroMode.Break, PomodoroMode.LongBreak -> {
        if (pomodoroPreferences.autoStartNext) {
          beginWork(_state.value.linkedReminderId)
        } else {
          stop(discard = true)
        }
      }
      PomodoroMode.Idle -> Unit
    }
  }

  companion object {
    private const val TAG = "PomodoroTimerEngine"
    private const val TICK_MS = 1000L
    private const val SECONDS_PER_MINUTE = 60
  }
}
