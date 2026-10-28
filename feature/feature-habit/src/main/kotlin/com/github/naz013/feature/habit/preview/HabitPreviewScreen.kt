package com.github.naz013.feature.habit.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

private val MAX_CONTENT_WIDTH = 600.dp
private val ICON_SIZE = 24.dp
private val CHECK_BUTTON_SIZE = 56.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitPreviewScreen(
  modifier: Modifier = Modifier,
  state: HabitPreviewState,
  onBackClick: () -> Unit,
  onEditClick: () -> Unit,
  onCheckInClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onDeleteConfirmed: () -> Unit,
  onDeleteDismissed: () -> Unit,
  adsContent: @Composable () -> Unit = {},
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBackClick,
          )
        },
        actions = {
          if (state.content is HabitPreviewContent.Ready) {
            MenuIconButton(
              icon = AppIcons.Fluent.Edit,
              contentDescription = stringResource(R.string.edit),
              onClick = onEditClick,
            )
            MenuIconButton(
              icon = AppIcons.Fluent.Delete,
              contentDescription = stringResource(R.string.delete),
              onClick = onDeleteClick,
            )
          }
        },
        colors = TopAppbarColor,
      )
    },
  ) { padding ->
    when (val content = state.content) {
      is HabitPreviewContent.Loading -> {
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is HabitPreviewContent.Ready -> {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Column(
            modifier = Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            HeaderCard(content = content, onCheckInClick = onCheckInClick)
            StatsRow(stats = content.stats)
            ActivityCard(content = content)
            adsContent()
          }
        }
      }
    }
  }

  if (state.isDeleteConfirmationVisible) {
    AlertDialog(
      onDismissRequest = onDeleteDismissed,
      title = { Text(stringResource(R.string.delete_habit_title)) },
      text = { Text(stringResource(R.string.delete_habit_message)) },
      confirmButton = {
        TextButton(onClick = onDeleteConfirmed, modifier = Modifier.testTag("habit_delete_confirm")) {
          Text(stringResource(R.string.delete))
        }
      },
      dismissButton = {
        TextButton(onClick = onDeleteDismissed) {
          Text(stringResource(R.string.cancel))
        }
      },
    )
  }
}

@Composable
private fun HeaderCard(
  modifier: Modifier = Modifier,
  content: HabitPreviewContent.Ready,
  onCheckInClick: () -> Unit,
) {
  Card(
    modifier = modifier.fillMaxWidth().testTag("habit_preview_header"),
    colors = CardDefaults.cardColors(containerColor = content.backgroundColor),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      content.iconRes?.let {
        Icon(
          painter = painterResource(it),
          contentDescription = null,
          tint = content.contentColor,
          modifier = Modifier.size(ICON_SIZE),
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = content.name,
          color = content.contentColor,
          style = MaterialTheme.typography.titleLarge,
        )
        Text(
          text = content.frequencyLabel,
          color = content.contentColor,
          style = MaterialTheme.typography.bodyMedium,
        )
        content.reminderLabel?.let {
          Text(text = it, color = content.contentColor, style = MaterialTheme.typography.bodySmall)
        }
        content.weekProgressLabel?.let {
          Text(text = it, color = content.contentColor, style = MaterialTheme.typography.bodySmall)
        }
        content.description?.let {
          Text(
            text = it,
            color = content.contentColor,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
          )
        }
      }
      Text(
        text = content.todayProgressLabel,
        color = content.contentColor,
        style = MaterialTheme.typography.titleLarge,
      )
      IconButton(
        onClick = onCheckInClick,
        modifier = Modifier.size(CHECK_BUTTON_SIZE).testTag("habit_preview_check_in"),
        colors = IconButtonDefaults.iconButtonColors(
          containerColor = content.checkButtonContainerColor,
          contentColor = content.checkButtonContentColor,
        ),
      ) {
        Icon(
          painter = AppIcons.Fluent.CheckmarkCircle,
          contentDescription = stringResource(R.string.habit_check_in),
        )
      }
    }
  }
}

@Composable
private fun StatsRow(
  modifier: Modifier = Modifier,
  stats: List<UiHabitStat>,
) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    stats.forEach { stat ->
      Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(text = stat.value, style = MaterialTheme.typography.headlineSmall)
          Text(
            text = stat.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
private fun ActivityCard(
  modifier: Modifier = Modifier,
  content: HabitPreviewContent.Ready,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Text(
        text = stringResource(R.string.habit_activity_title),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 12.dp),
      )
      HabitActivityBar(
        weeks = content.activityWeeks,
        activeColor = content.backgroundColor,
        summary = content.activitySummary,
        lessLabel = stringResource(R.string.habit_legend_less),
        moreLabel = stringResource(R.string.habit_legend_more),
      )
    }
  }
}
