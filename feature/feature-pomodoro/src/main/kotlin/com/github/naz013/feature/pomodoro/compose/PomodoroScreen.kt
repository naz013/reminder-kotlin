package com.github.naz013.feature.pomodoro.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.pomodoro.R
import com.github.naz013.feature.pomodoro.engine.PomodoroMode
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PomodoroScreen(
  modifier: Modifier = Modifier,
  state: PomodoroScreenState,
  onBackClick: () -> Unit,
  onStartClick: () -> Unit,
  onPauseResumeClick: () -> Unit,
  onSkipToBreakClick: () -> Unit,
  onStopClick: () -> Unit,
  onLinkReminderClick: () -> Unit,
  onReminderPickerDismiss: () -> Unit,
  onReminderPicked: (UiPickableReminder) -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.pomodoro_focus_timer)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBackClick,
          )
        },
      )
    },
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
      ModeChip(state.mode)

      Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
          progress = { state.progress },
          modifier = Modifier.size(220.dp),
          strokeWidth = 10.dp,
        )
        Text(text = state.timeLabel, style = MaterialTheme.typography.displayMedium)
      }

      LinkedReminderRow(
        summary = state.linkedReminderSummary,
        onClick = onLinkReminderClick,
      )

      Text(
        text = stringResource(R.string.pomodoro_sessions_today, state.completedWorkSessionsToday),
        style = MaterialTheme.typography.bodyMedium,
      )

      TimerControls(
        state = state,
        onStartClick = onStartClick,
        onPauseResumeClick = onPauseResumeClick,
        onSkipToBreakClick = onSkipToBreakClick,
        onStopClick = onStopClick,
      )
    }
  }

  if (state.isReminderPickerVisible) {
    ReminderPickerSheet(
      reminders = state.availableReminders,
      onDismiss = onReminderPickerDismiss,
      onReminderPicked = onReminderPicked,
    )
  }
}

@Composable
private fun ModeChip(mode: PomodoroMode) {
  val labelRes = when (mode) {
    PomodoroMode.Idle -> R.string.pomodoro_ready
    PomodoroMode.Work -> R.string.pomodoro_work
    PomodoroMode.Break -> R.string.pomodoro_break
    PomodoroMode.LongBreak -> R.string.pomodoro_long_break
  }
  AssistChip(onClick = {}, label = { Text(stringResource(labelRes)) })
}

@Composable
private fun LinkedReminderRow(summary: String?, onClick: () -> Unit) {
  TextButton(onClick = onClick) {
    Text(text = summary ?: stringResource(R.string.pomodoro_link_reminder))
  }
}

@Composable
private fun TimerControls(
  state: PomodoroScreenState,
  onStartClick: () -> Unit,
  onPauseResumeClick: () -> Unit,
  onSkipToBreakClick: () -> Unit,
  onStopClick: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
  ) {
    if (!state.isRunning) {
      Button(onClick = onStartClick) { Text(stringResource(R.string.pomodoro_start)) }
    } else {
      Button(onClick = onPauseResumeClick) {
        Text(
          stringResource(
            if (state.isPaused) R.string.pomodoro_resume else R.string.pomodoro_pause,
          ),
        )
      }
      if (state.mode == PomodoroMode.Work) {
        OutlinedButton(onClick = onSkipToBreakClick) { Text(stringResource(R.string.pomodoro_skip_break)) }
      }
      OutlinedButton(onClick = onStopClick) { Text(stringResource(R.string.pomodoro_stop)) }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderPickerSheet(
  reminders: List<UiPickableReminder>,
  onDismiss: () -> Unit,
  onReminderPicked: (UiPickableReminder) -> Unit,
) {
  ModalBottomSheet(onDismissRequest = onDismiss) {
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
      items(reminders, key = { it.id }) { reminder ->
        ListItem(
          headlineContent = { Text(reminder.summary) },
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onReminderPicked(reminder) },
        )
      }
    }
  }
}
