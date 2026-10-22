package com.github.naz013.feature.note.preview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.note.R
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet

private val CONTENT_MAX_HEIGHT = 420.dp

/** Lists a note's stored versions; selecting one shows its full text read-only with a Restore action. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteVersionHistorySheet(
  modifier: Modifier = Modifier,
  state: NoteHistoryState,
  onDismissRequest: () -> Unit,
  onVersionClick: (Long) -> Unit,
  onBackToListClick: () -> Unit,
  onRestoreClick: () -> Unit,
) {
  AppModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
    val selected = state.selectedVersion
    Text(
      text = selected?.savedAtFormatted ?: stringResource(R.string.note_version_history),
      style = MaterialTheme.typography.headlineSmall,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
    when {
      selected != null -> NoteVersionDetail(
        version = selected,
        onBackToListClick = onBackToListClick,
        onRestoreClick = onRestoreClick,
      )

      state.versions.isEmpty() -> Text(
        text = stringResource(R.string.note_version_history_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
      )

      else -> LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = CONTENT_MAX_HEIGHT)) {
        items(state.versions, key = { it.id }) { version ->
          NoteVersionRow(version = version, onClick = { onVersionClick(version.id) })
        }
      }
    }
  }
}

@Composable
private fun NoteVersionRow(
  modifier: Modifier = Modifier,
  version: UiNoteVersion,
  onClick: () -> Unit,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 12.dp),
  ) {
    Text(
      text = version.savedAtFormatted,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.primary,
    )
    Text(
      text = version.preview,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun NoteVersionDetail(
  modifier: Modifier = Modifier,
  version: UiNoteVersion,
  onBackToListClick: () -> Unit,
  onRestoreClick: () -> Unit,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = version.fullText,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = CONTENT_MAX_HEIGHT)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp),
    )
    Row(
      horizontalArrangement = Arrangement.End,
      modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
      TextButton(onClick = onBackToListClick) {
        Text(text = stringResource(R.string.cd_back))
      }
      Button(onClick = onRestoreClick) {
        Text(text = stringResource(R.string.note_version_restore))
      }
    }
  }
}
