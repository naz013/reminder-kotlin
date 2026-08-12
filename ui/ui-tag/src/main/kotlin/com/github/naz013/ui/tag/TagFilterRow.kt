package com.github.naz013.ui.tag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Single-select tag filter for a list of items - unlike [TagChipPicker] (multi-select, edit-only),
 * only one tag can be active at a time and clicking the active chip (or the "All" chip) clears the
 * filter. Renders nothing when there are no tags in the app yet.
 */
@Composable
fun TagFilterRow(
  allTags: List<TagChipState>,
  selectedTagId: String?,
  onTagSelected: (String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (allTags.isEmpty()) return
  LazyRow(
    modifier = modifier,
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    item {
      FilterChip(
        selected = selectedTagId == null,
        onClick = { onTagSelected(null) },
        label = { Text(stringResource(R.string.all)) },
      )
    }
    items(allTags, key = { it.id }) { tag ->
      FilterChip(
        selected = tag.id == selectedTagId,
        onClick = { onTagSelected(tag.id) },
        label = { Text(tag.name) },
      )
    }
  }
}
