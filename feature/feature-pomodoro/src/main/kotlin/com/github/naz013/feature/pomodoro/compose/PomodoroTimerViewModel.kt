package com.github.naz013.feature.pomodoro.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.pomodoro.engine.PomodoroEngineState
import com.github.naz013.feature.pomodoro.engine.PomodoroMode
import com.github.naz013.feature.pomodoro.engine.PomodoroTimerEngine
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class PomodoroTimerViewModel(
  initialLinkedReminderId: String?,
  private val pomodoroTimerEngine: PomodoroTimerEngine,
  private val reminderV2Repository: ReminderV2Repository,
  private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

  private val linkedReminderSummary = MutableStateFlow<String?>(null)
  private val reminderPickerVisible = MutableStateFlow(false)
  private val availableReminders = MutableStateFlow<List<UiPickableReminder>>(emptyList())

  val state: StateFlow<PomodoroScreenState> = combine(
    pomodoroTimerEngine.state,
    linkedReminderSummary,
    reminderPickerVisible,
    availableReminders,
  ) { engineState, summary, pickerVisible, reminders ->
    engineState.toScreenState(summary, pickerVisible, reminders)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PomodoroScreenState())

  init {
    if (initialLinkedReminderId != null) {
      loadLinkedReminderSummary(initialLinkedReminderId)
      pomodoroTimerEngine.start(initialLinkedReminderId)
    }
  }

  fun onStartClick() {
    if (pomodoroTimerEngine.state.value.mode == PomodoroMode.Idle) {
      pomodoroTimerEngine.start(pomodoroTimerEngine.state.value.linkedReminderId)
    }
  }

  fun onPauseResumeClick() {
    val current = pomodoroTimerEngine.state.value
    if (current.isPaused) pomodoroTimerEngine.resume() else pomodoroTimerEngine.pause()
  }

  fun onSkipToBreakClick() {
    pomodoroTimerEngine.skipToBreak()
  }

  fun onStopClick() {
    pomodoroTimerEngine.stop(discard = false)
    linkedReminderSummary.value = null
  }

  fun onLinkReminderClick() {
    reminderPickerVisible.value = true
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminders = reminderV2Repository.getAll(active = true, removed = false)
        .map { UiPickableReminder(id = it.uuId, summary = it.summary) }
      availableReminders.value = reminders
    }
  }

  fun onReminderPickerDismiss() {
    reminderPickerVisible.value = false
  }

  fun onReminderPicked(reminder: UiPickableReminder) {
    reminderPickerVisible.value = false
    linkedReminderSummary.value = reminder.summary
    // The engine only reads the linked id when a new work session starts; if one is already
    // running the link takes effect for the *next* session, which the UI communicates via the
    // now-updated linkedReminderSummary.
    if (pomodoroTimerEngine.state.value.mode == PomodoroMode.Idle) {
      pomodoroTimerEngine.start(reminder.id)
    }
  }

  private fun loadLinkedReminderSummary(reminderId: String) {
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderV2Repository.getById(reminderId)
      linkedReminderSummary.value = reminder?.summary
    }
  }

  private fun PomodoroEngineState.toScreenState(
    summary: String?,
    pickerVisible: Boolean,
    reminders: List<UiPickableReminder>,
  ): PomodoroScreenState = PomodoroScreenState(
    mode = mode,
    isRunning = isRunning,
    isPaused = isPaused,
    remainingSeconds = remainingSeconds,
    totalSeconds = totalSeconds,
    timeLabel = formatTime(remainingSeconds),
    progress = if (totalSeconds > 0) remainingSeconds / totalSeconds.toFloat() else 0f,
    completedWorkSessionsToday = completedWorkSessionsToday,
    linkedReminderId = linkedReminderId,
    linkedReminderSummary = summary,
    isReminderPickerVisible = pickerVisible,
    availableReminders = reminders,
  )

  private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "%02d:%02d".format(minutes, seconds)
  }

  companion object {
    private const val STOP_TIMEOUT_MS = 5000L
    private const val SECONDS_PER_MINUTE = 60
  }
}
