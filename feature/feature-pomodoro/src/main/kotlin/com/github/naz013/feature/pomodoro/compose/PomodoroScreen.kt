package com.github.naz013.feature.pomodoro.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.pomodoro.R
import com.github.naz013.feature.pomodoro.engine.PomodoroMode
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.TooltipIconButton
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground
import com.github.naz013.ui.common.compose.foundation.component.GradientHeroCard
import com.github.naz013.ui.common.compose.foundation.component.GradientScreenHeader

private val TIMER_RING_SIZE = 220.dp
private val TIMER_RING_STROKE = 12.dp
private const val BREATH_MAX_SCALE = 1.035f
private const val BREATH_DURATION_MS = 2600
private const val TABULAR_FIGURES = "tnum"

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
  AnimatedGradientBackground(
    modifier = modifier,
    colors = pomodoroGradientColors(state.mode),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
    ) {
      GradientScreenHeader(
        onBackClick = onBackClick,
        contentDescription = stringResource(R.string.cd_back),
      )

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
          .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        Text(
          text = stringResource(R.string.pomodoro_focus_timer),
          style = MaterialTheme.typography.headlineSmallEmphasized,
          color = MaterialTheme.colorScheme.tertiary,
        )

        GradientHeroCard(verticalArrangement = Arrangement.spacedBy(20.dp)) {
          ModeChip(mode = state.mode, modifier = Modifier.align(Alignment.CenterHorizontally))

          PomodoroTimerRing(state = state, modifier = Modifier.align(Alignment.CenterHorizontally))

          LinkedReminderRow(
            summary = state.linkedReminderSummary,
            onClick = onLinkReminderClick,
            modifier = Modifier.align(Alignment.CenterHorizontally),
          )

          Text(
            text = stringResource(R.string.pomodoro_sessions_today, state.completedWorkSessionsToday),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally),
          )
        }

        TimerControls(
          state = state,
          onStartClick = onStartClick,
          onPauseResumeClick = onPauseResumeClick,
          onSkipToBreakClick = onSkipToBreakClick,
          onStopClick = onStopClick,
        )
      }
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

/**
 * Tints the animated background toward this screen's current mode — a warm primary/tertiary
 * wash while focusing, cooling toward tertiary/secondary on a break — so the drifting gradient
 * itself reflects timer state instead of staying a static, mode-agnostic backdrop.
 */
@Composable
private fun pomodoroGradientColors(mode: PomodoroMode): List<Color> {
  val primaryContainer = MaterialTheme.colorScheme.primaryContainer
  val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
  val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer

  val target = when (mode) {
    PomodoroMode.Idle -> listOf(primaryContainer, tertiaryContainer, secondaryContainer)
    PomodoroMode.Work -> listOf(primaryContainer, primaryContainer, tertiaryContainer)
    PomodoroMode.Break, PomodoroMode.LongBreak -> listOf(tertiaryContainer, secondaryContainer, tertiaryContainer)
  }

  val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
  val first by animateColorAsState(target[0], effectsSpec, label = "pomodoroGradientColor0")
  val second by animateColorAsState(target[1], effectsSpec, label = "pomodoroGradientColor1")
  val third by animateColorAsState(target[2], effectsSpec, label = "pomodoroGradientColor2")
  return listOf(first, second, third)
}

@Composable
private fun ModeChip(mode: PomodoroMode, modifier: Modifier = Modifier) {
  val labelRes = when (mode) {
    PomodoroMode.Idle -> R.string.pomodoro_ready
    PomodoroMode.Work -> R.string.pomodoro_work
    PomodoroMode.Break -> R.string.pomodoro_break
    PomodoroMode.LongBreak -> R.string.pomodoro_long_break
  }
  val icon = when (mode) {
    PomodoroMode.Idle, PomodoroMode.Work -> AppIcons.Builder.Timer
    PomodoroMode.Break, PomodoroMode.LongBreak -> AppIcons.Fluent.DrinkCoffee
  }
  AssistChip(
    onClick = {},
    label = { Text(stringResource(labelRes)) },
    leadingIcon = {
      Icon(
        painter = icon,
        contentDescription = null,
        modifier = Modifier.size(AssistChipDefaults.IconSize),
      )
    },
    modifier = modifier,
  )
}

/**
 * The hero moment of this screen: a large progress ring that eases toward the latest [state]
 * with a spatial spring, tints itself by mode, and breathes gently while a session is actively
 * running (paused/idle states hold still so the motion stays meaningful, not just decorative).
 */
@Composable
private fun PomodoroTimerRing(state: PomodoroScreenState, modifier: Modifier = Modifier) {
  val animatedProgress by animateFloatAsState(
    targetValue = state.progress,
    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
    label = "pomodoroProgress",
  )

  val ringColor by animateColorAsState(
    targetValue = when (state.mode) {
      PomodoroMode.Break, PomodoroMode.LongBreak -> MaterialTheme.colorScheme.tertiary
      PomodoroMode.Idle, PomodoroMode.Work -> MaterialTheme.colorScheme.primary
    },
    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
    label = "pomodoroRingColor",
  )

  val infiniteTransition = rememberInfiniteTransition(label = "pomodoroBreath")
  val breathScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = BREATH_MAX_SCALE,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = BREATH_DURATION_MS, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pomodoroBreathScale",
  )
  val ringScale = if (state.isRunning && !state.isPaused) breathScale else 1f

  Box(
    modifier = modifier.scale(ringScale),
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator(
      progress = { animatedProgress },
      modifier = Modifier.size(TIMER_RING_SIZE),
      strokeWidth = TIMER_RING_STROKE,
      color = ringColor,
      trackColor = ringColor.copy(alpha = 0.16f),
    )
    Text(
      text = state.timeLabel,
      style = MaterialTheme.typography.displayMediumEmphasized.copy(fontFeatureSettings = TABULAR_FIGURES),
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun LinkedReminderRow(summary: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
  TextButton(onClick = onClick, modifier = modifier) {
    Icon(
      painter = AppIcons.Fluent.Link,
      contentDescription = null,
      modifier = Modifier.size(AssistChipDefaults.IconSize),
    )
    Text(
      text = summary ?: stringResource(R.string.pomodoro_link_reminder),
      modifier = Modifier.padding(start = 8.dp),
    )
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
      Button(onClick = onStartClick) {
        Icon(painter = AppIcons.Fluent.Play, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(text = stringResource(R.string.pomodoro_start), modifier = Modifier.padding(start = 8.dp))
      }
    } else {
      Button(onClick = onPauseResumeClick) {
        Icon(
          painter = if (state.isPaused) AppIcons.Fluent.Play else AppIcons.Fluent.Pause,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = stringResource(if (state.isPaused) R.string.pomodoro_resume else R.string.pomodoro_pause),
          modifier = Modifier.padding(start = 8.dp),
        )
      }
      if (state.mode == PomodoroMode.Work) {
        TooltipIconButton(contentDescription = stringResource(R.string.pomodoro_skip_break)) {
          OutlinedIconButton(onClick = onSkipToBreakClick) {
            Icon(painter = AppIcons.Fluent.Next, contentDescription = stringResource(R.string.pomodoro_skip_break))
          }
        }
      }
      TooltipIconButton(contentDescription = stringResource(R.string.pomodoro_stop)) {
        OutlinedIconButton(onClick = onStopClick) {
          Icon(painter = AppIcons.Fluent.RecordingStop, contentDescription = stringResource(R.string.pomodoro_stop))
        }
      }
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
