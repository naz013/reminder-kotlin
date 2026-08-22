package com.github.naz013.feature.googletask.tasklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.common.compose.foundation.component.ColorPickerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditGoogleTaskListScreen(
  modifier: Modifier = Modifier,
  state: EditGoogleTaskListState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onDeleteMenuClick: () -> Unit,
  onNameChange: (String) -> Unit,
  onColorSelected: (Int) -> Unit,
  onDefaultToggle: () -> Unit,
  onDeleteConfirmed: () -> Unit,
  onDeleteDismiss: () -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(state.screenTitleRes)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            enabled = !state.isLoading,
            onClick = onBackClick,
          )
        },
        actions = {
          if (state.canDelete) {
            MenuIconButton(
              icon = painterResource(R.drawable.ic_fluent_delete),
              contentDescription = stringResource(R.string.delete),
              enabled = !state.isLoading,
              onClick = onDeleteMenuClick,
            )
          }
          MenuTextButton(
            text = stringResource(R.string.save),
            enabled = !state.isLoading,
            onClick = onSaveClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
          .padding(padding)
          .verticalScroll(rememberScrollState())
          .padding(16.dp),
    ) {
      OutlinedTextField(
        value = state.name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.name)) },
        isError = state.nameError,
        supportingText = {
          if (state.nameError) Text(stringResource(R.string.must_be_not_empty))
        },
        enabled = !state.isLoading,
        modifier =
          Modifier
            .fillMaxWidth(),
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
          Modifier
            .fillMaxWidth(),
      ) {
        Text(
          text = stringResource(R.string.make_default),
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.weight(1f),
        )
        Switch(
          checked = state.isDefault,
          onCheckedChange = { onDefaultToggle() },
          enabled = !state.isLoading && !state.isDefaultLocked,
        )
      }

      ColorPickerCard(
        colors = state.sliderColors,
        selectedIndex = state.colorIndex,
        onColorSelected = onColorSelected,
        enabled = !state.isLoading,
        hapticFeedbackEnabled = state.hapticFeedbackEnabled,
        modifier = Modifier.padding(top = 16.dp),
      )

      adsContent()
    }
  }

  if (state.showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = onDeleteDismiss,
      text = { Text(stringResource(R.string.delete_this_list)) },
      confirmButton = {
        TextButton(onClick = onDeleteConfirmed) { Text(stringResource(R.string.yes)) }
      },
      dismissButton = {
        TextButton(onClick = onDeleteDismiss) { Text(stringResource(R.string.no)) }
      },
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun EditGoogleTaskListScreenPreview() {
  AppTheme {
    EditGoogleTaskListScreen(
      state =
        EditGoogleTaskListState(
          name = "Groceries",
          sliderColors =
            listOf(
              Color(0xFFF44336),
              Color(0xFFE91E63),
              Color(0xFF9C27B0),
              Color(0xFF673AB7),
              Color(0xFF3F51B5),
              Color(0xFF2196F3),
              Color(0xFF4CAF50),
              Color(0xFFFFEB3B),
              Color(0xFFFF9800),
            ),
          colorIndex = 6,
          isDefault = true,
          canDelete = true,
        ),
      onBackClick = {},
      onSaveClick = {},
      onDeleteMenuClick = {},
      onNameChange = {},
      onColorSelected = {},
      onDefaultToggle = {},
      onDeleteConfirmed = {},
      onDeleteDismiss = {},
      adsContent = {},
    )
  }
}
