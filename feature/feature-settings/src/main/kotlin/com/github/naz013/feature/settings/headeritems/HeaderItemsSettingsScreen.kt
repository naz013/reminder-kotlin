package com.github.naz013.feature.settings.headeritems

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.domain.home.HeaderNavigationSection
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.DisabledAlpha

private val ROW_HEIGHT = 64.dp

@Composable
internal fun HeaderItemsSettingsScreen(
  state: HeaderItemsSettingsState,
  onToggle: (section: HeaderNavigationSection, enabled: Boolean) -> Unit,
  onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val latestConfigurableItems = rememberUpdatedState(state.configurableItems)
  var draggedSectionKey by remember { mutableStateOf<Int?>(null) }
  var dragOffset by remember { mutableFloatStateOf(0f) }
  val rowHeightPx = with(LocalDensity.current) { ROW_HEIGHT.toPx() }

  LazyColumn(modifier = modifier.fillMaxWidth()) {
    items(state.pinnedItems, key = { it.section.ordinal }) { row ->
      PinnedHeaderItemRow(row = row)
    }

    if (state.pinnedItems.isNotEmpty() && state.configurableItems.isNotEmpty()) {
      item(key = "divider") { HorizontalDivider() }
    }

    items(state.configurableItems, key = { it.section.ordinal }) { row ->
      val sectionKey = row.section.ordinal
      ConfigurableHeaderItemRow(
        row = row,
        onToggle = { enabled -> onToggle(row.section, enabled) },
        rowModifier =
          Modifier
            .animateItem()
            .graphicsLayer {
              translationY = if (draggedSectionKey == sectionKey) dragOffset else 0f
            },
        dragHandleModifier =
          Modifier.pointerInput(sectionKey) {
            detectDragGesturesAfterLongPress(
              onDragStart = {
                draggedSectionKey = sectionKey
                dragOffset = 0f
              },
              onDragEnd = {
                draggedSectionKey = null
                dragOffset = 0f
              },
              onDragCancel = {
                draggedSectionKey = null
                dragOffset = 0f
              },
              onDrag = { change, dragAmount ->
                change.consume()
                dragOffset += dragAmount.y
                val steps = (dragOffset / rowHeightPx).toInt()
                if (steps != 0) {
                  val items = latestConfigurableItems.value
                  val fromIndex = items.indexOfFirst { it.section.ordinal == sectionKey }
                  if (fromIndex != -1) {
                    val toIndex = (fromIndex + steps).coerceIn(0, items.size - 1)
                    if (toIndex != fromIndex) {
                      onReorder(fromIndex, toIndex)
                    }
                  }
                  dragOffset -= steps * rowHeightPx
                }
              },
            )
          },
      )
    }
  }
}

@Composable
private fun PinnedHeaderItemRow(
  row: HeaderItemRow,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(ROW_HEIGHT)
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(modifier = Modifier.size(20.dp))
    Icon(
      painter = painterResource(row.iconRes),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier
        .padding(start = 8.dp)
        .size(24.dp),
    )
    Text(
      text = stringResource(row.titleRes),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier
        .weight(1f)
        .padding(start = 20.dp),
    )
    Text(
      text = stringResource(R.string.header_item_always_shown),
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun ConfigurableHeaderItemRow(
  row: HeaderItemRow,
  onToggle: (Boolean) -> Unit,
  rowModifier: Modifier = Modifier,
  dragHandleModifier: Modifier = Modifier,
) {
  val contentAlpha = if (row.isEnabled) 1f else DisabledAlpha
  Row(
    modifier = rowModifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.background)
      .height(ROW_HEIGHT)
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = AppIcons.Fluent.ReOrderDots,
      contentDescription = stringResource(R.string.todo_drag_to_reorder),
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = dragHandleModifier.size(20.dp),
    )
    Icon(
      painter = painterResource(row.iconRes),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onBackground.copy(alpha = contentAlpha),
      modifier = Modifier
        .padding(start = 8.dp)
        .size(24.dp),
    )
    Text(
      text = stringResource(row.titleRes),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
      modifier = Modifier
        .weight(1f)
        .padding(start = 20.dp),
    )
    Box(modifier = Modifier.width(16.dp))
    Switch(checked = row.isEnabled, onCheckedChange = onToggle)
  }
}
