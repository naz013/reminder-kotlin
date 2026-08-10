package com.github.naz013.ui.tag

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Read-only tag chips for preview/detail screens - unlike [TagChipPicker], these have no
 * click/selection semantics and no "manage tags" affordance. Renders nothing when [tags] is
 * empty, so callers can place it unconditionally.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagChipRow(
  tags: List<TagChipState>,
  modifier: Modifier = Modifier,
) {
  if (tags.isEmpty()) return
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    tags.forEach { tag -> TagChip(tag) }
  }
}

@Composable
private fun TagChip(tag: TagChipState) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
      Canvas(modifier = Modifier.size(8.dp)) { drawCircle(color = tag.color) }
      Text(
        text = tag.name,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 6.dp),
      )
    }
  }
}
