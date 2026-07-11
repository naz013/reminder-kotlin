package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppTheme
import kotlinx.coroutines.launch

private const val DISABLED_ALPHA = 0.38f

/**
 * A scrollable, snapping vertical picker wheel: the selected item sits in the center row,
 * flanked by dividers, with neighboring items fading out via smaller/dimmer text. This is the
 * Compose replacement for the custom-drawn `VerticalWheelSelector` View (fling physics on a
 * `Canvas`), rebuilt on top of [LazyColumn] snapping instead of manual gesture/fling handling.
 *
 * @param items The picker's labels, in order.
 * @param selectedIndex Index of the currently selected item in [items].
 * @param onSelectedIndexChange Invoked once the wheel settles on a new index (scroll end or tap).
 * @param visibleItemCount How many rows are visible at once. Must be odd so one row can be
 * centered.
 * @param itemHeight Height of a single row; also used to size the centered selection window.
 * @param enabled When false, scrolling/tapping is disabled and the wheel is dimmed.
 */
@Composable
fun WheelPicker(
  items: List<String>,
  selectedIndex: Int,
  onSelectedIndexChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
  visibleItemCount: Int = 3,
  itemHeight: Dp = 40.dp,
  enabled: Boolean = true,
) {
  require(visibleItemCount % 2 == 1) { "visibleItemCount must be odd, was $visibleItemCount" }
  if (items.isEmpty()) return

  val listState = rememberLazyListState()
  val flingBehavior = rememberSnapFlingBehavior(listState)
  val coroutineScope = rememberCoroutineScope()
  val sidePadding = itemHeight * (visibleItemCount / 2)

  LaunchedEffect(selectedIndex, items.size) {
    val target = selectedIndex.coerceIn(items.indices)
    if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != target) {
      listState.scrollToItem(target)
    }
  }

  LaunchedEffect(listState) {
    snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
      if (!scrolling) {
        val settledIndex = listState.firstVisibleItemIndex.coerceIn(items.indices)
        if (settledIndex != selectedIndex) {
          onSelectedIndexChange(settledIndex)
        }
      }
    }
  }

  Box(
    modifier = modifier
      .height(itemHeight * visibleItemCount)
      .alpha(if (enabled) 1f else DISABLED_ALPHA),
  ) {
    LazyColumn(
      state = listState,
      flingBehavior = flingBehavior,
      contentPadding = PaddingValues(vertical = sidePadding),
      modifier = Modifier.fillMaxWidth(),
      userScrollEnabled = enabled,
    ) {
      itemsIndexed(items) { index, label ->
        val selected = index == selectedIndex
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .clickable(enabled = enabled) {
              coroutineScope.launch { listState.animateScrollToItem(index) }
              onSelectedIndexChange(index)
            },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = label,
            style = if (selected) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            color = if (selected) {
              MaterialTheme.colorScheme.onSurface
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
          )
        }
      }
    }

    // Dividers mark the top/bottom edge of the centered selection row.
    HorizontalDivider(
      color = MaterialTheme.colorScheme.outlineVariant,
      modifier = Modifier.align(Alignment.TopCenter).offset(y = sidePadding),
    )
    HorizontalDivider(
      color = MaterialTheme.colorScheme.outlineVariant,
      modifier = Modifier.align(Alignment.TopCenter).offset(y = sidePadding + itemHeight),
    )
  }
}

@Preview(showBackground = true, name = "Wheel picker")
@Composable
private fun PreviewWheelPicker() {
  AppTheme {
    val items = remember { listOf("Low", "Normal", "High", "Urgent", "Critical") }
    WheelPicker(
      items = items,
      selectedIndex = 2,
      onSelectedIndexChange = {},
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
