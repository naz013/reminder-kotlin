package com.github.naz013.feature.note.share

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.note.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet

/** Lets the user pick the [NoteShareFormat] to share a note as. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteShareSheet(
  modifier: Modifier = Modifier,
  onDismissRequest: () -> Unit,
  onFormatClick: (NoteShareFormat) -> Unit,
) {
  AppModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
    Text(
      text = stringResource(R.string.note_share_as),
      style = MaterialTheme.typography.headlineSmall,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
    NoteShareFormat.entries.forEach { format ->
      NoteShareFormatRow(
        title = stringResource(format.titleRes()),
        icon = format.icon(),
        onClick = { onFormatClick(format) },
      )
    }
  }
}

@Composable
private fun NoteShareFormatRow(
  modifier: Modifier = Modifier,
  title: String,
  icon: Painter,
  onClick: () -> Unit,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(start = 16.dp),
    )
  }
}

private fun NoteShareFormat.titleRes(): Int = when (this) {
  NoteShareFormat.ORIGINAL -> R.string.note_share_format_original
  NoteShareFormat.PLAIN_TEXT -> R.string.note_share_format_plain_text
  NoteShareFormat.MARKDOWN -> R.string.note_share_format_markdown
  NoteShareFormat.HTML -> R.string.note_share_format_html
}

@Composable
private fun NoteShareFormat.icon(): Painter = when (this) {
  NoteShareFormat.ORIGINAL -> AppIcons.Fluent.Note
  NoteShareFormat.PLAIN_TEXT -> AppIcons.Fluent.Text
  NoteShareFormat.MARKDOWN -> AppIcons.Fluent.TextAsterisk
  NoteShareFormat.HTML -> AppIcons.Fluent.Code
}
