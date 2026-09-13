package com.github.naz013.feature.pomodoro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.pomodoro.compose.PomodoroScreen
import com.github.naz013.feature.pomodoro.compose.PomodoroScreenState
import com.github.naz013.feature.pomodoro.compose.PomodoroTimerViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.pomodoroEntries(backStack: MutableList<NavKey>) {
  entry<PomodoroNavKey.Timer> { key -> TimerEntry(key, backStack) }
}

@Composable
private fun TimerEntry(key: PomodoroNavKey.Timer, backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<PomodoroTimerViewModel> { parametersOf(key.linkedReminderId) }
  val state by viewModel.state.collectAsState(PomodoroScreenState())

  PomodoroScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onStartClick = viewModel::onStartClick,
    onPauseResumeClick = viewModel::onPauseResumeClick,
    onSkipToBreakClick = viewModel::onSkipToBreakClick,
    onStopClick = viewModel::onStopClick,
    onLinkReminderClick = viewModel::onLinkReminderClick,
    onReminderPickerDismiss = viewModel::onReminderPickerDismiss,
    onReminderPicked = viewModel::onReminderPicked,
  )
}
