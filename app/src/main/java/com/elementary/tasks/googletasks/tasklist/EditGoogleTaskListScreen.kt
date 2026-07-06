package com.elementary.tasks.googletasks.tasklist

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.ColorSlider

/**
 * Body content only - the title/back-arrow/menu chrome is the native Toolbar owned by
 * [com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment].
 */
@Composable
fun EditGoogleTaskListScreen(
  state: EditGoogleTaskListState,
  onNameChange: (String) -> Unit,
  onColorSelected: (Int) -> Unit,
  onDefaultToggle: () -> Unit,
  onDeleteConfirmed: () -> Unit,
  onDeleteDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
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
          .fillMaxWidth()
          .padding(top = 8.dp),
    )

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
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

    Card(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
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
          selectedIndex = state.colorIndex,
          onColorSelected = onColorSelected,
          enabled = !state.isLoading,
          modifier =
            Modifier
              .fillMaxWidth()
              .height(36.dp)
              .padding(top = 8.dp),
        )
      }
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
