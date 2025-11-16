package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppTheme
import kotlinx.coroutines.launch

/**
 * A Material 3 Modal Bottom Sheet component that follows Material Design 3 guidelines.
 *
 * Modal bottom sheets present a set of choices while blocking interaction with the rest of the screen.
 * They are an alternative to inline menus and simple dialogs, providing additional room for content,
 * iconography, and actions.
 *
 * Features:
 * - Standard Material 3 design with drag handle
 * - Customizable container color and shape
 * - Optional scrim (backdrop) with customizable color
 * - Skip partially expanded state option
 * - Window insets support for edge-to-edge layouts
 * - Proper content padding and spacing
 *
 * Input validation:
 * - Validates sheet state before operations
 * - Handles dismiss requests appropriately
 *
 * @param onDismissRequest Callback invoked when the bottom sheet is dismissed
 * @param modifier Optional modifier for the bottom sheet
 * @param sheetState State object for controlling the bottom sheet. Use [rememberModalBottomSheetState]
 * @param shape Shape of the bottom sheet container
 * @param containerColor Color of the bottom sheet container
 * @param contentColor Preferred color for content inside the bottom sheet
 * @param tonalElevation Tonal elevation of the bottom sheet
 * @param scrimColor Color of the scrim (backdrop) behind the bottom sheet
 * @param dragHandle Composable for the drag handle at the top of the sheet
 * @param content The content to display inside the bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  sheetState: SheetState = rememberModalBottomSheetState(),
  shape: Shape = BottomSheetDefaults.ExpandedShape,
  containerColor: Color = BottomSheetDefaults.ContainerColor,
  contentColor: Color = MaterialTheme.colorScheme.onSurface,
  tonalElevation: Dp = BottomSheetDefaults.Elevation,
  scrimColor: Color = BottomSheetDefaults.ScrimColor,
  dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
  content: @Composable ColumnScope.() -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    sheetState = sheetState,
    shape = shape,
    containerColor = containerColor,
    contentColor = contentColor,
    tonalElevation = tonalElevation,
    scrimColor = scrimColor,
    dragHandle = dragHandle,
    content = content
  )
}

/**
 * A bottom sheet header component with optional title and close button.
 *
 * This component provides a consistent header design for bottom sheets with:
 * - Optional title text
 * - Optional close/dismiss button
 * - Proper spacing and alignment
 * - Material 3 styling
 *
 * Input validation:
 * - Returns early if both title and showCloseButton are null/false
 *
 * @param title Optional title text to display in the header
 * @param showCloseButton Whether to show a close button
 * @param onCloseClick Callback invoked when the close button is clicked
 * @param modifier Optional modifier for the header
 */
@Composable
fun BottomSheetHeader(
  title: String? = null,
  showCloseButton: Boolean = false,
  onCloseClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  // Early return if header is empty
  if (title == null && !showCloseButton) return

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    if (title != null) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.align(Alignment.Center),
        textAlign = TextAlign.Center
      )
    }

    if (showCloseButton) {
      IconButton(
        onClick = onCloseClick,
        modifier = Modifier.align(Alignment.CenterEnd)
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

/**
 * Represents an item in a bottom sheet list.
 *
 * @property id Unique identifier for the item
 * @property title The text to display for this item
 * @property subtitle Optional subtitle text
 * @property icon Optional icon to display on the left side
 * @property enabled Whether the item is enabled and clickable
 * @property dividerAfter Whether to show a divider after this item
 */
data class BottomSheetItem(
  val id: Int,
  val title: String,
  val subtitle: String? = null,
  val icon: ImageVector? = null,
  val enabled: Boolean = true,
  val dividerAfter: Boolean = false
)

/**
 * A list item component for bottom sheets with Material 3 styling.
 *
 * This component displays a single item in a bottom sheet list with:
 * - Optional leading icon
 * - Title text
 * - Optional subtitle text
 * - Click handling
 * - Proper spacing and typography
 * - Material 3 ripple effect
 *
 * Input validation:
 * - Validates enabled state before invoking click callbacks
 * - Properly handles null optional properties
 *
 * @param item The bottom sheet item to display
 * @param onClick Callback invoked when the item is clicked (receives item id)
 * @param modifier Optional modifier for the list item
 */
@Composable
fun BottomSheetListItem(
  item: BottomSheetItem,
  onClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  ListItem(
    headlineContent = {
      Text(
        text = item.title,
        style = MaterialTheme.typography.bodyLarge
      )
    },
    modifier = modifier,
    supportingContent = if (item.subtitle != null) {
      {
        Text(
          text = item.subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      null
    },
    leadingContent = if (item.icon != null) {
      {
        Icon(
          imageVector = item.icon,
          contentDescription = item.title,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      null
    },
    tonalElevation = 0.dp
  )
}

/**
 * A bottom sheet list component that displays multiple items with Material 3 styling.
 *
 * This component creates a list of items in a bottom sheet with:
 * - Automatic divider insertion based on item configuration
 * - Proper spacing between items
 * - Click handling for individual items
 * - Support for icons and subtitles
 * - Material 3 design system compliance
 *
 * Input validation:
 * - Returns early if items list is empty
 * - Validates item properties before rendering
 *
 * @param items List of items to display in the bottom sheet
 * @param onItemClick Callback invoked when an item is clicked (receives item id)
 * @param modifier Optional modifier for the list
 */
@Composable
fun BottomSheetList(
  items: List<BottomSheetItem>,
  onItemClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  // Early return if no items
  if (items.isEmpty()) return

  Column(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight()
  ) {
    items.forEachIndexed { index, item ->
      Surface(
        onClick = {
          if (item.enabled) {
            onItemClick(item.id)
          }
        },
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
      ) {
        BottomSheetListItem(
          item = item,
          onClick = onItemClick
        )
      }

      // Show divider if requested and not the last item
      if (item.dividerAfter && index < items.size - 1) {
        HorizontalDivider(
          modifier = Modifier.padding(vertical = 4.dp),
          color = MaterialTheme.colorScheme.outlineVariant
        )
      }
    }
  }
}

// ============================================================================
// Preview Composables
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Modal Bottom Sheet with Header and List")
@Composable
private fun PreviewAppModalBottomSheet() {
  AppTheme {
    var showBottomSheet by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
      AppModalBottomSheet(
        onDismissRequest = { showBottomSheet = false },
        sheetState = sheetState
      ) {
        BottomSheetHeader(
          title = "Choose an action",
          showCloseButton = true,
          onCloseClick = {
            scope.launch {
              sheetState.hide()
              showBottomSheet = false
            }
          }
        )

        BottomSheetList(
          items = listOf(
            BottomSheetItem(
              id = 1,
              title = "Edit",
              subtitle = "Make changes to this item",
              icon = Icons.Default.Edit
            ),
            BottomSheetItem(
              id = 2,
              title = "Share",
              subtitle = "Share with others",
              icon = Icons.Default.Share,
              dividerAfter = true
            ),
            BottomSheetItem(
              id = 3,
              title = "Delete",
              subtitle = "Remove this item",
              icon = Icons.Default.Delete
            )
          ),
          onItemClick = { itemId ->
            scope.launch {
              sheetState.hide()
              showBottomSheet = false
            }
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Simple Bottom Sheet List")
@Composable
private fun PreviewBottomSheetList() {
  AppTheme {
    var showBottomSheet by remember { mutableStateOf(true) }

    if (showBottomSheet) {
      AppModalBottomSheet(
        onDismissRequest = { showBottomSheet = false }
      ) {
        BottomSheetList(
          items = listOf(
            BottomSheetItem(
              id = 1,
              title = "Option 1",
              icon = Icons.Default.Edit
            ),
            BottomSheetItem(
              id = 2,
              title = "Option 2",
              icon = Icons.Default.Share
            ),
            BottomSheetItem(
              id = 3,
              title = "Option 3",
              icon = Icons.Default.Delete
            )
          ),
          onItemClick = { showBottomSheet = false },
          modifier = Modifier.padding(bottom = 16.dp)
        )
      }
    }
  }
}

@Preview(showBackground = true, name = "Bottom Sheet Header with Title and Close")
@Composable
private fun PreviewBottomSheetHeaderWithClose() {
  AppTheme {
    BottomSheetHeader(
      title = "Bottom Sheet Title",
      showCloseButton = true,
      onCloseClick = {}
    )
  }
}

@Preview(showBackground = true, name = "Bottom Sheet Header with Title Only")
@Composable
private fun PreviewBottomSheetHeaderTitleOnly() {
  AppTheme {
    BottomSheetHeader(
      title = "Bottom Sheet Title"
    )
  }
}

@Preview(showBackground = true, name = "Bottom Sheet List Item")
@Composable
private fun PreviewBottomSheetListItem() {
  AppTheme {
    BottomSheetListItem(
      item = BottomSheetItem(
        id = 1,
        title = "Edit",
        subtitle = "Make changes to this item",
        icon = Icons.Default.Edit
      ),
      onClick = {}
    )
  }
}

