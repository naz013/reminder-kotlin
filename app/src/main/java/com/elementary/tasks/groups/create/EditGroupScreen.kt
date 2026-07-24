package com.elementary.tasks.groups.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.common.compose.foundation.component.ColorSlider
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupScreen(
  state: EditGroupState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onDeleteMenuClick: () -> Unit,
  onNameChange: (String) -> Unit,
  onColorSelected: (Int) -> Unit,
  onDefaultCheckChanged: (Boolean) -> Unit,
  onWorkflowRulesClick: () -> Unit,
  onDeleteConfirmed: () -> Unit,
  onCopyKeepClick: () -> Unit,
  onCopyReplaceClick: () -> Unit,
  onDialogDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(if (state.hasId) R.string.change_group else R.string.create_group)) },
        navigationIcon = {
          MenuIconButton(
            icon = painterResource(R.drawable.ic_builder_arrow_left),
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
          .padding(padding)
          .verticalScroll(rememberScrollState())
          .padding(16.dp),
    ) {
      OutlinedTextField(
        value = state.title,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.title)) },
        isError = state.titleError,
        supportingText = {
          if (state.titleError) Text(stringResource(R.string.must_be_not_empty))
        },
        singleLine = true,
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      ) {
        Text(
          text = stringResource(R.string.make_default),
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.weight(1f),
        )
        Switch(
          checked = state.isDefault,
          onCheckedChange = onDefaultCheckChanged,
          enabled = !state.isLoading && state.defaultCheckEnabled,
        )
      }

      Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = stringResource(R.string.color),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          ColorSlider(
            colors = state.sliderColors,
            selectedIndex = state.colorPosition,
            onColorSelected = onColorSelected,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(40.dp).padding(top = 8.dp),
          )
        }
      }

      if (state.hasId) {
        SettingsItem(
          title = stringResource(R.string.workflow_rules),
          icon = painterResource(R.drawable.ic_fluent_arrow_repeat_all),
          modifier = Modifier.padding(top = 16.dp),
          onClick = onWorkflowRulesClick,
        )
      }
    }
  }

  when (state.dialog) {
    EditGroupDialog.CopyConflict -> {
      AlertDialog(
        onDismissRequest = onDialogDismiss,
        text = { Text(stringResource(R.string.same_group_message)) },
        confirmButton = {
          TextButton(onClick = onCopyKeepClick) { Text(stringResource(R.string.keep)) }
        },
        dismissButton = {
          TextButton(onClick = onCopyReplaceClick) { Text(stringResource(R.string.replace)) }
        },
      )
    }

    EditGroupDialog.DeleteConfirm -> {
      AlertDialog(
        onDismissRequest = onDialogDismiss,
        text = { Text(stringResource(R.string.are_you_sure)) },
        confirmButton = {
          TextButton(onClick = onDeleteConfirmed) { Text(stringResource(R.string.yes)) }
        },
        dismissButton = {
          TextButton(onClick = onDialogDismiss) { Text(stringResource(R.string.no)) }
        },
      )
    }

    null -> Unit
  }
}

@Preview(showBackground = true)
@Composable
private fun EditGroupScreenPreview() {
  AppTheme {
    EditGroupScreen(
      state =
        EditGroupState(
          title = "Work",
          colorPosition = 5,
          sliderColors = listOf(Color.Red, Color.Magenta, Color.Blue, Color.Cyan, Color.Green, Color.Yellow),
          canDelete = true,
        ),
      onBackClick = {},
      onSaveClick = {},
      onDeleteMenuClick = {},
      onNameChange = {},
      onColorSelected = {},
      onDefaultCheckChanged = {},
      onWorkflowRulesClick = {},
      onDeleteConfirmed = {},
      onCopyKeepClick = {},
      onCopyReplaceClick = {},
      onDialogDismiss = {},
    )
  }
}
