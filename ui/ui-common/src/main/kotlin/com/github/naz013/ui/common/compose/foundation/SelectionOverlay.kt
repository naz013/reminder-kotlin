package com.github.naz013.ui.common.compose.foundation

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable

/**
 * Trailing-content swap for the app's multiselect pattern (see `docs/multiselect.md`): renders a
 * checkbox while a list is in selection mode, falling back to the item's normal trailing
 * [content] (typically a per-item overflow menu) otherwise. Use this inside a list item's
 * `trailingContent` slot.
 */
@Composable
fun BoxScope.SelectionOverlay(
  isSelectionMode: Boolean,
  isSelected: Boolean,
  onToggleSelected: () -> Unit,
  content: @Composable BoxScope.() -> Unit,
) {
  if (isSelectionMode) {
    Checkbox(checked = isSelected, onCheckedChange = { onToggleSelected() })
  } else {
    content()
  }
}
