package com.github.naz013.feature.routine.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.tag.TagChipRow

private const val CHECK_ANIMATION_MS = 150

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutinePreviewScreen(
  state: RoutinePreviewState,
  onBackClick: () -> Unit,
  onEditClick: () -> Unit,
  onPinToggleClick: () -> Unit,
  onResetStepsClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onStepCheckToggle: (stepId: String) -> Unit,
  onStartClick: () -> Unit,
  adsContent: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    floatingActionButton = {
      if (state is RoutinePreviewState.Ready) {
        ExtendedFloatingActionButton(
          onClick = onStartClick,
          icon = { Icon(AppIcons.Fluent.Play, contentDescription = null) },
          text = { Text(stringResource(R.string.start_routine)) },
        )
      }
    },
    topBar = {
      TopAppBar(
        title = { },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        actions = {
          if (state is RoutinePreviewState.Ready) {
            MenuIconButton(
              icon = AppIcons.Fluent.Edit,
              contentDescription = stringResource(R.string.edit),
              onClick = onEditClick,
            )
            OverflowMenu(
              isPinned = state.isPinned,
              onPinToggleClick = onPinToggleClick,
              onResetStepsClick = onResetStepsClick,
              onDeleteClick = onDeleteClick,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    when (state) {
      is RoutinePreviewState.Loading -> {
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is RoutinePreviewState.Ready -> {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
          item { RoutineBanner(state) }
          item { adsContent() }
          items(state.steps, key = { it.id }) { step ->
            RoutineStepChecklistRow(step = step, onCheckToggle = { onStepCheckToggle(step.id) })
          }
        }
      }
    }
  }
}

@Composable
private fun RoutineBanner(state: RoutinePreviewState.Ready) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(16.dp),
    colors = CardDefaults.cardColors(containerColor = state.backgroundColor),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        state.iconRes?.let {
          Icon(
            painter = painterResource(it),
            contentDescription = null,
            tint = state.contentColor,
            modifier = Modifier.padding(end = 8.dp),
          )
        }
        Text(
          text = state.title,
          style = MaterialTheme.typography.headlineSmall,
          color = state.contentColor,
        )
      }
      if (!state.description.isNullOrEmpty()) {
        Text(
          text = state.description,
          style = MaterialTheme.typography.bodyMedium,
          color = state.contentColor,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Text(text = state.durationLabel, color = state.contentColor, style = MaterialTheme.typography.labelLarge)
        Text(text = state.stepCountLabel, color = state.contentColor, style = MaterialTheme.typography.labelLarge)
        Text(text = state.recurrenceLabel, color = state.contentColor, style = MaterialTheme.typography.labelLarge)
      }
      if (state.tags.isNotEmpty()) {
        TagChipRow(tags = state.tags, modifier = Modifier.padding(top = 12.dp))
      }
    }
  }
}

@Composable
private fun RoutineStepChecklistRow(
  step: RoutinePreviewStepUiState,
  onCheckToggle: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(onClick = onCheckToggle, modifier = Modifier.size(40.dp)) {
      AnimatedVisibility(
        visible = step.isCompleted,
        enter = scaleIn(tween(CHECK_ANIMATION_MS)) + fadeIn(tween(CHECK_ANIMATION_MS)),
        exit = scaleOut(tween(CHECK_ANIMATION_MS)) + fadeOut(tween(CHECK_ANIMATION_MS)),
      ) {
        Icon(
          painter = AppIcons.Fluent.CheckboxChecked,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
      }
      AnimatedVisibility(
        visible = !step.isCompleted,
        enter = scaleIn(tween(CHECK_ANIMATION_MS)) + fadeIn(tween(CHECK_ANIMATION_MS)),
        exit = scaleOut(tween(CHECK_ANIMATION_MS)) + fadeOut(tween(CHECK_ANIMATION_MS)),
      ) {
        Icon(
          painter = AppIcons.Fluent.CheckboxUnchecked,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = step.title,
        style = MaterialTheme.typography.bodyLarge,
        textDecoration = if (step.isCompleted) TextDecoration.LineThrough else null,
        color = if (step.isCompleted) {
          MaterialTheme.colorScheme.onSurfaceVariant
        } else {
          MaterialTheme.colorScheme.onSurface
        },
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        step.scheduledTime?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Text(
          text = step.durationLabel,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun OverflowMenu(
  isPinned: Boolean,
  onPinToggleClick: () -> Unit,
  onResetStepsClick: () -> Unit,
  onDeleteClick: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    MenuIconButton(
      icon = AppIcons.Fluent.MoreVertical,
      contentDescription = stringResource(R.string.more_options),
      onClick = { expanded = true },
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = listOf(
        PopupMenuItem(
          id = OverflowAction.TOGGLE_PIN.ordinal,
          title = stringResource(if (isPinned) R.string.unpin else R.string.pin),
          iconRes = if (isPinned) DrawableCatalog.Fluent.PinOff else DrawableCatalog.Fluent.Pin,
        ),
        PopupMenuItem(
          id = OverflowAction.RESET_STEPS.ordinal,
          title = stringResource(R.string.reset_steps),
          iconRes = DrawableCatalog.Fluent.ArrowCounterclockwise,
        ),
        PopupMenuItem(
          id = OverflowAction.DELETE.ordinal,
          title = stringResource(R.string.delete),
          iconRes = DrawableCatalog.Fluent.Delete,
        ),
      ),
      onItemClick = { id ->
        expanded = false
        when (OverflowAction.entries[id]) {
          OverflowAction.TOGGLE_PIN -> onPinToggleClick()
          OverflowAction.RESET_STEPS -> onResetStepsClick()
          OverflowAction.DELETE -> onDeleteClick()
        }
      },
    )
  }
}

private enum class OverflowAction {
  TOGGLE_PIN,
  RESET_STEPS,
  DELETE,
}
