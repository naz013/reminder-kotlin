package com.github.naz013.ui.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
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

private val BUBBLE_SIZE = 44.dp
private val BUBBLE_ICON_SIZE = 22.dp
private val OPTION_SIZE = 40.dp
private val OPTION_ICON_SIZE = 20.dp
private val GRID_MAX_HEIGHT = 220.dp
private const val GRID_COLUMNS = 6

/**
 * Circular "bubble" trigger showing the routine's selected icon (or a generic add-icon placeholder
 * when none is picked) that opens a grid popup of [RoutineIconSet.ALL] to choose from, plus a
 * "None" tile to clear the selection. [selectedIndex] is the same index [RoutineIconSet.ALL] uses,
 * mirroring how [RoutineColorPicker] takes a plain index rather than a resolved value.
 */
@Composable
fun RoutineIconPicker(
  selectedIndex: Int?,
  onIconSelected: (Int?) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedIconRes = selectedIndex?.let { RoutineIconSet.ALL.getOrNull(it) }

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
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      // A plain Column/Row grid, not LazyVerticalGrid: Material3's DropdownMenu wraps its content
      // in a Column measured with IntrinsicSize.Max, and a SubcomposeLayout-backed lazy layout
      // (LazyVerticalGrid included) can't answer an intrinsic-width query, which crashes at
      // runtime. 33 options is small enough that a non-lazy grid costs nothing.
      val options: List<Int?> = listOf(null) + RoutineIconSet.ALL.indices.toList()
      Column(
        modifier = Modifier
          .heightIn(max = GRID_MAX_HEIGHT)
          .verticalScroll(rememberScrollState())
          .padding(4.dp),
      ) {
        options.chunked(GRID_COLUMNS).forEach { rowOptions ->
          Row {
            rowOptions.forEach { index ->
              IconOption(
                iconRes = index?.let { RoutineIconSet.ALL[it] },
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
  }
}

@Composable
private fun IconOption(
  iconRes: Int?,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Box(
    modifier = Modifier
      .padding(4.dp)
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

@Preview(showBackground = true)
@Composable
private fun RoutineIconPickerPreview() {
  AppTheme {
    RoutineIconPicker(
      selectedIndex = 3,
      onIconSelected = {},
    )
  }
}
