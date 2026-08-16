package com.github.naz013.feature.reminder.settings.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.preset.UiPresetList
import com.github.naz013.feature.reminder.build.preset.PresetListItem
import com.github.naz013.ui.common.compose.AppTheme

@Composable
fun ManagePresetsScreen(
  presets: List<UiPresetList>,
  onDeleteClick: (UiPresetList) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
  ) {
    if (presets.isEmpty()) {
      EmptyState(
        modifier =
          Modifier
            .fillMaxSize()
            .weight(1f),
      )
    } else {
      LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(presets, key = { it.id }) { preset ->
          PresetListItem(
            preset = preset,
            onClick = {},
            onDeleteClick = { onDeleteClick(preset) },
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_builder_preset),
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    )
    Text(
      text = stringResource(R.string.recur_no_presets),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun ManagePresetsScreenPreview() {
  AppTheme {
    ManagePresetsScreen(
      presets =
        listOf(
          UiPresetList(name = "Daily standup", id = "1", description = "RRULE:FREQ=DAILY"),
          UiPresetList(name = "Weekly review", id = "2", description = "RRULE:FREQ=WEEKLY"),
        ),
      onDeleteClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun ManagePresetsScreenPreview_Empty() {
  AppTheme {
    ManagePresetsScreen(
      presets = emptyList(),
      onDeleteClick = {},
    )
  }
}
