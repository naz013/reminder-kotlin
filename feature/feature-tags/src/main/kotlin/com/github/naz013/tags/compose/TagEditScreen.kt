package com.github.naz013.tags.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.tags.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.ColorPickerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TagEditScreen(
  modifier: Modifier = Modifier,
  state: TagEditState,
  // True when shown as a two-pane detail pane rather than pushed full-screen - only changes the
  // leading icon (close vs. back), onBackClick pops the entry either way.
  renderAsDetailPane: Boolean = false,
  onBackClick: () -> Unit,
  onNameChange: (String) -> Unit,
  onColorSelected: (Int) -> Unit,
  onSaveClick: () -> Unit,
  onDeleteClick: () -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(if (state.id == null) R.string.new_tag else R.string.tags)) },
        navigationIcon = {
          MenuIconButton(
            icon = if (renderAsDetailPane) AppIcons.Fluent.Dismiss else AppIcons.Builder.ArrowLeft,
            contentDescription = if (renderAsDetailPane) stringResource(R.string.acc_close) else null,
            onClick = onBackClick
          )
        },
        actions = {
          if (state.canDelete) {
            MenuIconButton(
              icon = AppIcons.Fluent.Delete,
              contentDescription = stringResource(R.string.delete),
              onClick = onDeleteClick
            )
          }
          MenuIconButton(
            icon = AppIcons.Fluent.Checkmark,
            contentDescription = null,
            onClick = onSaveClick
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .padding(16.dp)
    ) {
      OutlinedTextField(
        value = state.name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.tag_name)) },
        isError = state.nameError,
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )

      ColorPickerCard(
        colors = state.sliderColors,
        selectedIndex = state.colorPosition,
        onColorSelected = onColorSelected,
        hapticFeedbackEnabled = state.hapticFeedbackEnabled,
        modifier = Modifier.padding(top = 16.dp),
      )

      adsContent()
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun TagEditScreenPreview() {
  AppTheme {
    TagEditScreen(
      state = TagEditState(id = "1", name = "Work", colorPosition = 5, canDelete = true),
      onBackClick = {},
      onNameChange = {},
      onColorSelected = {},
      onSaveClick = {},
      onDeleteClick = {},
      adsContent = {},
    )
  }
}
