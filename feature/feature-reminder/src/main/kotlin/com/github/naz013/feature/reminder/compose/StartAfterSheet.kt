package com.github.naz013.feature.reminder.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.reminder.build.UiStartAfterCandidate
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet

private val LIST_MAX_HEIGHT = 420.dp

/** Picker for the reminder a builder reminder should wait for. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StartAfterSheet(
  modifier: Modifier = Modifier,
  candidates: List<UiStartAfterCandidate>,
  onDismissRequest: () -> Unit,
  onCandidateClick: (UiStartAfterCandidate) -> Unit,
) {
  AppModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
    Text(
      text = stringResource(R.string.start_after_select_reminder),
      style = MaterialTheme.typography.headlineSmall,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
    if (candidates.isEmpty()) {
      Text(
        text = stringResource(R.string.start_after_no_reminders),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
      )
    } else {
      LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = LIST_MAX_HEIGHT)) {
        items(candidates, key = { it.id }) { candidate ->
          Text(
            text = candidate.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onCandidateClick(candidate) }
              .padding(horizontal = 16.dp, vertical = 14.dp),
          )
        }
      }
    }
  }
}
