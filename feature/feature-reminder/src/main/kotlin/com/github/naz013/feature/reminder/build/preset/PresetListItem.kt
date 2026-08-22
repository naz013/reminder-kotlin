package com.github.naz013.feature.reminder.build.preset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

/**
 * A single recur-preset row: name + description, with an optional trailing delete button.
 * Reused by both the preset management screen (deletable) and preset picker UIs (not deletable).
 */
@Composable
internal fun PresetListItem(
  modifier: Modifier = Modifier,
  preset: UiPresetList,
  onClick: () -> Unit = {},
  canDelete: Boolean = true,
  onDeleteClick: (() -> Unit)? = null,
  dividerBottom: Boolean = true,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clickable(onClick = onClick)
          .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = preset.name,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = preset.description,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
      if (canDelete) {
        MenuIconButton(
          icon = painterResource(R.drawable.ic_fluent_delete),
          contentDescription = stringResource(R.string.delete),
          iconColor = MaterialTheme.colorScheme.onSurface,
          onClick = { onDeleteClick?.invoke() },
        )
      }
    }
    if (dividerBottom) HorizontalDivider()
  }
}

@Preview(showBackground = true)
@Composable
private fun PresetListItemPreview() {
  AppTheme {
    Column {
      PresetListItem(
        preset = UiPresetList(name = "Daily standup", id = "1", description = "RRULE:FREQ=DAILY"),
        onClick = {},
      )
      PresetListItem(
        preset = UiPresetList(name = "Weekly review", id = "2", description = "RRULE:FREQ=WEEKLY"),
        onClick = {},
        canDelete = false,
      )
    }
  }
}
