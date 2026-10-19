package com.github.naz013.ui.common.compose.foundation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.icon.DrawableCatalog

private val BUBBLE_SIZE = 48.dp
private val BUBBLE_ICON_SIZE = 22.dp
private val OPTION_SIZE = 44.dp
private val OPTION_ICON_SIZE = 22.dp
private val GRID_MIN_CELL_SIZE = 56.dp
private val GRID_PADDING = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)

/**
 * Circular "bubble" trigger showing the selected icon (or a generic add-icon placeholder when none
 * is picked) that opens a bottom sheet with a scrollable grid of [icons] to choose from, plus a
 * "None" tile to clear the selection. [selectedIndex] is an index into [icons] rather than a
 * resolved drawable, so callers can persist it; [icons] must only ever be appended to for that
 * reason.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPicker(
  modifier: Modifier = Modifier,
  icons: List<Int>,
  selectedIndex: Int?,
  onIconSelected: (Int?) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedIconRes = selectedIndex?.let { icons.getOrNull(it) }
  val options = remember(icons) { listOf<Int?>(null) + icons.indices.toList() }

  Box(modifier = modifier) {
    Box(
      modifier = Modifier
        .size(BUBBLE_SIZE)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        .clickable { expanded = true },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        painter = painterResource(selectedIconRes ?: DrawableCatalog.Fluent.Add),
        contentDescription = stringResource(R.string.routine_icon),
        tint = if (selectedIconRes != null) {
          MaterialTheme.colorScheme.onSurface
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.size(BUBBLE_ICON_SIZE),
      )
    }
  }
  if (expanded) {
    AppModalBottomSheet(onDismissRequest = { expanded = false }) {
      BottomSheetHeader(title = stringResource(R.string.routine_icon))
      LazyVerticalGrid(
        columns = GridCells.Adaptive(GRID_MIN_CELL_SIZE),
        contentPadding = GRID_PADDING,
        modifier = Modifier.fillMaxWidth(),
      ) {
        items(options) { index ->
          IconOption(
            iconRes = index?.let { icons[it] },
            selected = index == selectedIndex,
            onClick = {
              onIconSelected(index)
              expanded = false
            },
          )
        }
      }
    }
  }
}

@Composable
private fun IconOption(
  modifier: Modifier = Modifier,
  @DrawableRes iconRes: Int?,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .size(OPTION_SIZE)
        .clip(CircleShape)
        .background(
          if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        )
        .clickable(onClick = onClick),
      contentAlignment = Alignment.Center,
    ) {
      val tint = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
      } else {
        MaterialTheme.colorScheme.onSurfaceVariant
      }
      Icon(
        painter = painterResource(iconRes ?: DrawableCatalog.Fluent.Dismiss),
        contentDescription = if (iconRes == null) stringResource(R.string.no_icon) else null,
        tint = tint,
        modifier = Modifier.size(OPTION_ICON_SIZE),
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun IconPickerPreview() {
  AppTheme {
    IconPicker(
      icons = listOf(
        DrawableCatalog.Fluent.Home,
        DrawableCatalog.Fluent.Heart,
        DrawableCatalog.Fluent.Star,
        DrawableCatalog.Fluent.Sleep,
      ),
      selectedIndex = 3,
      onIconSelected = {},
    )
  }
}
