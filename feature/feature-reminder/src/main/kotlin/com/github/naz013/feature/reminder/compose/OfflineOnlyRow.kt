package com.github.naz013.feature.reminder.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons

/**
 * Shared by [com.github.naz013.feature.reminder.build.BuildReminderScreen] and
 * [com.github.naz013.feature.reminder.todo.TodoEditScreen] - both let a reminder/todo be marked to
 * skip cloud sync. The description text's start padding aligns it under the label (not the icon),
 * accounting for the row's own padding, the icon's start padding, the icon's width, and the spacer.
 */
@Composable
internal fun OfflineOnlyRow(
  modifier: Modifier = Modifier,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        painter = AppIcons.Fluent.Cloud,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 8.dp),
      )
      Spacer(modifier = Modifier.width(16.dp))
      Text(
        text = stringResource(R.string.offline_only_reminder),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.weight(1f),
      )
      Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    Text(
      text = stringResource(R.string.offline_only_reminder_description),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.fillMaxWidth().padding(start = 56.dp, end = 16.dp),
    )
  }
}
