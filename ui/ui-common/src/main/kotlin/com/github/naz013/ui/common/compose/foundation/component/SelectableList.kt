package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppTheme

/**
 * A grid of selectable cells, single- or multi-choice depending on how [selectedItems] is
 * updated by the caller. Generic replacement for the legacy
 * `AbstractSelectableArrayController`'s grid adapter + `list_item_builder_selectable.xml`, used
 * by e.g. days-of-week/day-of-month pickers and iCal by-day/by-month lists.
 *
 * @param items Options to choose from.
 * @param selectedItems The currently selected subset of [items].
 * @param onItemToggle Invoked with the tapped item; the caller decides whether to add/replace it
 * in [selectedItems] (single- vs multi-select).
 * @param itemLabel Renders an item as cell text.
 * @param columns Number of columns in the grid.
 */
@Composable
fun <T> SelectableChipGrid(
  items: List<T>,
  selectedItems: Set<T>,
  onItemToggle: (T) -> Unit,
  itemLabel: (T) -> String,
  modifier: Modifier = Modifier,
  columns: Int = 7,
  itemKey: ((T) -> Any)? = null,
) {
  LazyVerticalGrid(
    columns = GridCells.Fixed(columns),
    modifier = modifier,
  ) {
    items(items = items, key = itemKey) { item ->
      val selected = item in selectedItems
      Box(
        modifier = Modifier
          .padding(4.dp)
          .background(
            color = if (selected) {
              MaterialTheme.colorScheme.tertiaryContainer
            } else {
              MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(8.dp),
          )
          .clickable { onItemToggle(item) }
          .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = itemLabel(item),
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          color = if (selected) {
            MaterialTheme.colorScheme.onTertiaryContainer
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          },
        )
      }
    }
  }
}

@Preview(showBackground = true, name = "Selectable chip grid")
@Composable
private fun PreviewSelectableChipGrid() {
  AppTheme {
    val items = remember { listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su") }
    SelectableChipGrid(
      items = items,
      selectedItems = setOf("Mo", "We", "Fr"),
      onItemToggle = {},
      itemLabel = { it },
    )
  }
}
