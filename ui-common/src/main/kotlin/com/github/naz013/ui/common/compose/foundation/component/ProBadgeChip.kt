package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppTheme

/**
 * Small "PRO" pill used as the trailing element on a locked [SettingsItem] row, so free users can
 * discover a Pro-only feature instead of the row being omitted entirely.
 */
@Composable
fun ProBadgeChip() {
  Surface(
    shape = RoundedCornerShape(50),
    color = MaterialTheme.colorScheme.tertiaryContainer,
  ) {
    Text(
      text = stringResource(R.string.pro_badge),
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onTertiaryContainer,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun ProBadgeChipPreview() {
  AppTheme {
    ProBadgeChip()
  }
}
