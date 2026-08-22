package com.github.naz013.feature.routine.execution

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.routine.CircularStepTimer

private val COMPLETE_BUTTON_HEIGHT = 56.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineExecutionScreen(
  state: RoutineExecutionState,
  onBackClick: () -> Unit,
  onPlayPauseClick: () -> Unit,
  onAddMinuteClick: () -> Unit,
  onSkipClick: () -> Unit,
  onPreviousStepClick: () -> Unit,
  onCompleteStepClick: () -> Unit,
  adsContent: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { if (state is RoutineExecutionState.Running) Text(state.routineTitle) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        colors = TopAppbarColor,
      )
    },
    bottomBar = {
      if (state is RoutineExecutionState.Running) {
        Surface(shadowElevation = 4.dp) {
          Button(
            onClick = onCompleteStepClick,
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
              .height(COMPLETE_BUTTON_HEIGHT),
          ) {
            Text(
              text = stringResource(R.string.complete_step),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    },
  ) { padding ->
    when (state) {
      is RoutineExecutionState.Loading -> {
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is RoutineExecutionState.Running -> {
        RunningContent(
          state = state,
          onPlayPauseClick = onPlayPauseClick,
          onAddMinuteClick = onAddMinuteClick,
          onSkipClick = onSkipClick,
          onPreviousStepClick = onPreviousStepClick,
          adsContent = adsContent,
          modifier = Modifier.fillMaxSize().padding(padding),
        )
      }

      is RoutineExecutionState.Finished -> {
        FinishedContent(
          state = state,
          onDoneClick = onBackClick,
          modifier = Modifier.fillMaxSize().padding(padding),
        )
      }
    }
  }
}

@Composable
private fun RunningContent(
  state: RoutineExecutionState.Running,
  onPlayPauseClick: () -> Unit,
  onAddMinuteClick: () -> Unit,
  onSkipClick: () -> Unit,
  onPreviousStepClick: () -> Unit,
  adsContent: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.verticalScroll(rememberScrollState()).padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = stringResource(R.string.step_of_count, state.stepIndex + 1, state.stepCount),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = state.stepTitle,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(top = 4.dp),
        )
        state.scheduledTimeLabel?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
          )
        }
        if (state.isTimed) {
          Spacer(modifier = Modifier.height(24.dp))
          CircularStepTimer(progress = state.progress, timeLabel = state.timeLabel)
        }
      }
    }
    Spacer(modifier = Modifier.height(16.dp))

    adsContent()

    Spacer(modifier = Modifier.height(16.dp))

    if (state.isTimed) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        MenuIconButton(
          icon = Icons.Filled.SkipPrevious,
          contentDescription = stringResource(R.string.previous_step),
          enabled = !state.isFirstStep,
          onClick = onPreviousStepClick,
        )
        MenuIconButton(
          icon = if (state.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
          contentDescription = stringResource(if (state.isPaused) R.string.resume else R.string.pause),
          onClick = onPlayPauseClick,
        )
        MenuIconButton(
          icon = Icons.Filled.SkipNext,
          contentDescription = stringResource(R.string.skip_step),
          onClick = onSkipClick,
        )
      }
      TextButton(onClick = onAddMinuteClick, modifier = Modifier.padding(top = 8.dp)) {
        Text(stringResource(R.string.add_one_minute))
      }
    } else if (!state.isFirstStep) {
      MenuIconButton(
        icon = Icons.Filled.SkipPrevious,
        contentDescription = stringResource(R.string.previous_step),
        onClick = onPreviousStepClick,
      )
    }
  }
}

@Composable
private fun FinishedContent(
  state: RoutineExecutionState.Finished,
  onDoneClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      Icons.Filled.CheckCircle,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.primary,
    )
    Text(
      text = stringResource(R.string.routine_finished_title),
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(top = 16.dp),
    )
    Text(
      text = stringResource(R.string.routine_execution_steps_completed, state.completedCount, state.totalCount),
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.padding(top = 8.dp),
    )
    Text(
      text = stringResource(R.string.routine_execution_total_time, state.totalTimeLabel),
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.padding(top = 4.dp),
    )
    Button(onClick = onDoneClick, modifier = Modifier.padding(top = 24.dp).fillMaxWidth()) {
      Text(stringResource(R.string.ok))
    }
  }
}
