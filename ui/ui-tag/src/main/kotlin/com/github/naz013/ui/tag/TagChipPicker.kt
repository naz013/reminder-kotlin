package com.github.naz013.ui.tag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.domain.Tag
import com.github.naz013.ui.common.compose.AppIcons

/**
 * Embeddable in any edit screen (reminder/note) that wants to attach tags to the item it's
 * editing - the caller owns which tags are currently selected and persists the change.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagChipPicker(
  allTags: List<TagChipState>,
  selectedTagIds: Set<String>,
  onToggle: (TagChipState) -> Unit,
  onManageTagsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    allTags.forEach { tag ->
      FilterChip(
        selected = tag.id in selectedTagIds,
        onClick = { onToggle(tag) },
        label = { Text(tag.name) }
      )
    }
    AssistChip(
      onClick = onManageTagsClick,
      leadingIcon = { Icon(AppIcons.Fluent.Add, contentDescription = null) },
      label = { Text(stringResource(R.string.manage_tags)) }
    )
  }
}
