package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme

/**
 * Small error-colored pill marking a reminder as critical (alarm-clock-style escalation, bypasses
 * Do Not Disturb). Used on both the reminder list row and the reminder preview screen so the
 * indicator reads the same everywhere.
 */
@Composable
fun CriticalBadgeChip(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = MaterialTheme.colorScheme.errorContainer,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
      Icon(
        painter = AppIcons.Fluent.Warning,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.size(14.dp),
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = stringResource(R.string.critical),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onErrorContainer,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun CriticalBadgeChipPreview() {
  AppTheme {
    CriticalBadgeChip()
  }
}
