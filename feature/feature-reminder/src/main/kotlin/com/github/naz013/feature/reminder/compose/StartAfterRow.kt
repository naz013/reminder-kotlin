package com.github.naz013.feature.reminder.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

/**
 * Shown in [com.github.naz013.feature.reminder.build.BuildReminderScreen] to make a reminder wait
 * for another one. [selectedTitle] is the title of the chosen predecessor, null when none is set.
 */
@Composable
internal fun StartAfterRow(
  modifier: Modifier = Modifier,
  selectedTitle: String?,
  onClick: () -> Unit,
  onClearClick: () -> Unit,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = AppIcons.Fluent.Link,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = stringResource(R.string.start_after_reminder),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Text(
        text = selectedTitle ?: stringResource(R.string.start_after_reminder_description),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
    if (selectedTitle != null) {
      MenuIconButton(
        icon = AppIcons.Fluent.Dismiss,
        contentDescription = stringResource(R.string.start_after_clear),
        onClick = onClearClick,
      )
    }
  }
}
