package com.github.naz013.feature.googletask.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PreviewGoogleTaskScreen(
  modifier: Modifier = Modifier,
  state: PreviewGoogleTaskState,
  onBackClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onDeleteConfirmed: () -> Unit,
  onDeleteDismiss: () -> Unit,
  onCompleteClick: () -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.details)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        actions = {
          MenuIconButton(
            icon = painterResource(R.drawable.ic_fluent_edit),
            contentDescription = stringResource(R.string.edit),
            onClick = onEditClick,
          )
          MenuIconButton(
            icon = painterResource(R.drawable.ic_fluent_delete),
            contentDescription = stringResource(R.string.delete),
            onClick = onDeleteClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
    floatingActionButton = {
      val task = state.task
      if (task != null && !task.isCompleted) {
        ExtendedFloatingActionButton(
          onClick = onCompleteClick,
          icon = { Icon(painterResource(R.drawable.ic_fluent_checkmark), contentDescription = null) },
          text = { Text(stringResource(R.string.complete)) },
        )
      }
    },
  ) { padding ->
    val task = state.task
    if (task == null) {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
      return@Scaffold
    }

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(padding)
          .verticalScroll(rememberScrollState()),
    ) {
      DetailRow(
        icon = R.drawable.ic_fluent_text,
        text = task.text,
        iconTint = MaterialTheme.colorScheme.primary,
        textStyle = MaterialTheme.typography.titleLarge,
        textColor = MaterialTheme.colorScheme.primary,
        topPadding = 24.dp,
      )
      task.notes?.let {
        DetailRow(icon = R.drawable.ic_fluent_note, text = it)
      }
      DetailRow(
        icon = R.drawable.ic_fluent_list,
        text = task.taskListName,
        iconTint = Color(task.taskListColor),
        textColor = Color(task.taskListColor),
      )
      task.dueDate?.let {
        DetailRow(icon = R.drawable.ic_builder_by_monthday, text = it)
      }
      task.createdDate?.let {
        DetailRow(icon = R.drawable.ic_builder_google_calendar_add, text = it)
      }
      task.completedDate?.let {
        DetailRow(icon = R.drawable.ic_fluent_calendar_checkmark, text = it)
      }
      DetailRow(
        icon = R.drawable.ic_fluent_flag,
        text = stringResource(if (task.isCompleted) R.string.completed else R.string.not_completed),
      )

      adsContent()
    }
  }

  if (state.showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = onDeleteDismiss,
      text = { Text(stringResource(R.string.are_you_sure)) },
      confirmButton = {
        TextButton(onClick = onDeleteConfirmed) { Text(stringResource(R.string.yes)) }
      },
      dismissButton = {
        TextButton(onClick = onDeleteDismiss) { Text(stringResource(R.string.no)) }
      },
    )
  }
}

@Composable
private fun DetailRow(
  icon: Int,
  text: String,
  modifier: Modifier = Modifier,
  iconTint: Color = MaterialTheme.colorScheme.onBackground,
  textColor: Color = MaterialTheme.colorScheme.onBackground,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  topPadding: Dp = 12.dp,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = topPadding),
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(32.dp),
    )
    Text(
      text = text,
      style = textStyle,
      color = textColor,
      modifier =
        Modifier
          .weight(1f)
          .padding(start = 16.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun PreviewGoogleTaskScreenPreview() {
  AppTheme {
    PreviewGoogleTaskScreen(
      state =
        PreviewGoogleTaskState(
          task =
            GoogleTaskPreviewState(
              id = "1",
              text = "Buy milk",
              notes = "2 liters, whole",
              dueDate = "Tomorrow",
              createdDate = "Today",
              completedDate = null,
              isCompleted = false,
              taskListName = "Groceries",
              taskListColor = Color(0xFF4CAF50).toArgb(),
            ),
        ),
      onBackClick = {},
      onEditClick = {},
      onDeleteClick = {},
      onDeleteConfirmed = {},
      onDeleteDismiss = {},
      onCompleteClick = {},
      adsContent = {},
    )
  }
}
