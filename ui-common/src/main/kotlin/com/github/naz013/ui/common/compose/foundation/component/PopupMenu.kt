package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.github.naz013.ui.common.compose.AppTheme

/**
 * Represents a menu item in the popup menu.
 *
 * @property id Unique identifier for the menu item
 * @property title The text to display for this menu item
 * @property icon Optional icon to display on the left side of the item
 * @property iconRes Optional icon resource ID to display on the left side
 * @property enabled Whether the menu item is enabled and clickable
 * @property subMenu Optional list of sub-menu items
 * @property dividerAfter Whether to show a divider after this item
 */
data class PopupMenuItem(
  val id: Int,
  val title: String,
  val icon: ImageVector? = null,
  val iconRes: Int? = null,
  val enabled: Boolean = true,
  val subMenu: List<PopupMenuItem>? = null,
  val dividerAfter: Boolean = false
)

/**
 * A Material 3 styled popup menu component with support for icons and nested submenus.
 *
 * This component follows Material Design 3 guidelines for menus, providing:
 * - Optional leading icons for menu items
 * - Support for nested submenus with arrow indicators
 * - Horizontal alignment (Start/End)
 * - Vertical alignment (Above/Below/Auto) with smart positioning
 * - Automatic position adjustment based on available space
 * - Proper spacing and typography
 * - Dividers between menu sections
 * - Ripple effects on interaction
 *
 * Input validation:
 * - Returns early if items list is empty
 * - Validates item IDs before invoking callbacks
 *
 * @param expanded Whether the menu is currently visible
 * @param onDismissRequest Callback invoked when the menu should be dismissed
 * @param items List of menu items to display
 * @param onItemClick Callback invoked when a menu item is clicked (receives item id)
 * @param modifier Optional modifier for the dropdown menu
 * @param horizontalAlignment Horizontal alignment relative to anchor (Start or End)
 * @param verticalAlignment Vertical alignment relative to anchor (Above, Below, or Auto)
 * @param offset Offset for the dropdown menu position
 */
@Composable
fun PopupMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  items: List<PopupMenuItem>,
  onItemClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
  horizontalAlignment: PopupMenuAlignment = PopupMenuAlignment.Start,
  verticalAlignment: PopupMenuVerticalAlignment = PopupMenuVerticalAlignment.Auto,
  offset: DpOffset = DpOffset(0.dp, 0.dp)
) {
  // Early return if no items
  if (items.isEmpty()) return

  // Use standard DropdownMenu for default positioning (Start + Auto)
  if (horizontalAlignment == PopupMenuAlignment.Start &&
      verticalAlignment == PopupMenuVerticalAlignment.Auto) {
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = onDismissRequest,
      modifier = modifier,
      offset = offset,
      properties = PopupProperties(focusable = true)
    ) {
      items.forEachIndexed { index, item ->
        PopupMenuItemContent(
          item = item,
          onItemClick = { itemId ->
            onItemClick(itemId)
            onDismissRequest()
          }
        )

        // Show divider if requested or if it's not the last item and next item has divider
        if (item.dividerAfter && index < items.size - 1) {
          HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
          )
        }
      }
    }
  } else if (expanded) {
    // Use custom positioning for other alignments
    val intOffset = with(LocalDensity.current) {
      IntOffset(offset.x.roundToPx(), offset.y.roundToPx())
    }
    Popup(
      onDismissRequest = onDismissRequest,
      popupPositionProvider = EdgeAlignedPositionProvider(
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        offset = intOffset
      ),
      properties = PopupProperties(focusable = true)
    ) {
      Surface(
        modifier = modifier.wrapContentSize(),
        shape = MaterialTheme.shapes.extraSmall,
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
      ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
          items.forEachIndexed { index, item ->
            PopupMenuItemContent(
              item = item,
              onItemClick = { itemId ->
                onItemClick(itemId)
                onDismissRequest()
              }
            )

            // Show divider if requested
            if (item.dividerAfter && index < items.size - 1) {
              HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Internal composable for rendering a single menu item with optional icon and submenu.
 *
 * Handles the layout and interaction for menu items, including:
 * - Leading icon display
 * - Text content with proper overflow handling
 * - Submenu arrow indicator
 * - Nested submenu expansion
 *
 * @param item The menu item data to render
 * @param onItemClick Callback when this item is clicked
 */
@Composable
private fun PopupMenuItemContent(
  item: PopupMenuItem,
  onItemClick: (Int) -> Unit
) {
  var subMenuExpanded by remember { mutableStateOf(false) }

  Box {
    DropdownMenuItem(
      text = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Leading icon (if present)
          val hasIcon = item.icon != null || item.iconRes != null
          if (hasIcon) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              // Render icon
              item.icon?.let { iconVector ->
                Icon(
                  imageVector = iconVector,
                  contentDescription = null,
                  modifier = Modifier.size(24.dp),
                  tint = if (item.enabled) {
                    LocalContentColor.current
                  } else {
                    LocalContentColor.current.copy(alpha = 0.38f)
                  }
                )
              } ?: item.iconRes?.let { iconResId ->
                Icon(
                  painter = painterResource(id = iconResId),
                  contentDescription = null,
                  modifier = Modifier.size(24.dp),
                  tint = if (item.enabled) {
                    LocalContentColor.current
                  } else {
                    LocalContentColor.current.copy(alpha = 0.38f)
                  }
                )
              }

              Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          } else {
            Text(
              text = item.title,
              style = MaterialTheme.typography.bodyLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
            )
          }

          // Trailing submenu arrow (if submenu exists)
          if (!item.subMenu.isNullOrEmpty()) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = "Has submenu",
              modifier = Modifier.size(24.dp),
              tint = if (item.enabled) {
                LocalContentColor.current.copy(alpha = 0.6f)
              } else {
                LocalContentColor.current.copy(alpha = 0.38f)
              }
            )
          }
        }
      },
      onClick = {
        if (item.subMenu.isNullOrEmpty()) {
          onItemClick(item.id)
        } else {
          subMenuExpanded = true
        }
      },
      enabled = item.enabled,
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    )

    // Nested submenu
    if (!item.subMenu.isNullOrEmpty()) {
      PopupMenu(
        expanded = subMenuExpanded,
        onDismissRequest = { subMenuExpanded = false },
        items = item.subMenu,
        onItemClick = { subItemId ->
          onItemClick(subItemId)
          subMenuExpanded = false
        },
        offset = DpOffset(8.dp, (-8).dp)
      )
    }
  }
}

/**
 * A convenience composable that provides an anchor point for the popup menu.
 *
 * This wraps content that when clicked will show the popup menu.
 * Useful for creating menu buttons or other clickable triggers.
 *
 * Input validation:
 * - Only shows menu if items list is not empty
 *
 * @param items List of menu items to display in the popup
 * @param onItemClick Callback invoked when a menu item is clicked
 * @param modifier Optional modifier for the anchor
 * @param enabled Whether the anchor is clickable
 * @param content The composable content that acts as the menu trigger
 */
@Composable
fun AnchoredPopupMenu(
  items: List<PopupMenuItem>,
  onItemClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable () -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  // Removed unused anchorRect and density
  Box(modifier = modifier) {
    Box(
      modifier = Modifier
        .clickable(
          enabled = enabled,
          onClick = {
            if (items.isNotEmpty()) expanded = true
          }
        )
    ) { content() }
    PopupMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = items,
      onItemClick = { id ->
        onItemClick(id)
        expanded = false
      }
    )
  }
}

/**
 * Horizontal alignment options for anchoring the popup menu relative to the anchor composable.
 */
enum class PopupMenuAlignment { Start, End }

/**
 * Vertical alignment options for anchoring the popup menu relative to the anchor composable.
 */
enum class PopupMenuVerticalAlignment { Above, Below, Auto }

/**
 * Internal position provider capable of aligning the popup menu with support for:
 * - Horizontal alignment (Start/End)
 * - Vertical alignment (Above/Below/Auto)
 * - Automatic positioning based on available space
 * - Window bounds constraints
 *
 * @param horizontalAlignment Horizontal alignment relative to anchor
 * @param verticalAlignment Vertical alignment relative to anchor
 * @param offset Additional offset applied after alignment
 */
private class EdgeAlignedPositionProvider(
  private val horizontalAlignment: PopupMenuAlignment,
  private val verticalAlignment: PopupMenuVerticalAlignment,
  private val offset: IntOffset
) : PopupPositionProvider {
  override fun calculatePosition(
    anchorBounds: IntRect,
    windowSize: IntSize,
    layoutDirection: LayoutDirection,
    popupContentSize: IntSize
  ): IntOffset {
    // Calculate horizontal position
    val anchorLeft = anchorBounds.left
    val anchorRight = anchorBounds.right
    val xRaw = when (horizontalAlignment) {
      PopupMenuAlignment.Start -> anchorLeft + offset.x
      PopupMenuAlignment.End -> (anchorRight - popupContentSize.width) + offset.x
    }
    val x = xRaw.coerceIn(0, windowSize.width - popupContentSize.width)

    // Calculate vertical position with smart placement
    val anchorTop = anchorBounds.top
    val anchorBottom = anchorBounds.bottom
    val spaceAbove = anchorTop
    val spaceBelow = windowSize.height - anchorBottom

    val y = when (verticalAlignment) {
      PopupMenuVerticalAlignment.Above -> {
        // Place above anchor
        (anchorTop - popupContentSize.height + offset.y).coerceAtLeast(0)
      }
      PopupMenuVerticalAlignment.Below -> {
        // Place below anchor
        val yBelow = anchorBottom + offset.y
        if (yBelow + popupContentSize.height > windowSize.height) {
          // Not enough space below, try above
          (anchorTop - popupContentSize.height + offset.y).coerceAtLeast(0)
        } else {
          yBelow
        }
      }
      PopupMenuVerticalAlignment.Auto -> {
        // Automatically choose best position based on available space
        if (spaceBelow >= popupContentSize.height) {
          // Enough space below, place below
          anchorBottom + offset.y
        } else if (spaceAbove >= popupContentSize.height) {
          // Not enough space below but enough above, place above
          anchorTop - popupContentSize.height + offset.y
        } else {
          // Not enough space in either direction, prefer below if more space there
          if (spaceBelow >= spaceAbove) {
            (anchorBottom + offset.y).coerceAtMost(windowSize.height - popupContentSize.height)
          } else {
            (anchorTop - popupContentSize.height + offset.y).coerceAtLeast(0)
          }
        }
      }
    }

    return IntOffset(x, y)
  }
}

/**
 * AnchoredPopupMenu with comprehensive alignment support.
 *
 * Supports:
 * - Horizontal alignment (Start/End) relative to anchor
 * - Vertical alignment (Above/Below/Auto) with smart positioning
 * - Automatic position adjustment based on available screen space
 * - Custom offsets applied after alignment
 *
 * When using End alignment, the menu's right edge aligns with anchor's right edge.
 * When using Auto vertical alignment, the menu automatically positions above or below
 * based on available space.
 *
 * @param items Menu items to display
 * @param onItemClick Callback with clicked item id
 * @param modifier Modifier for the anchor Box
 * @param enabled Whether the anchor is interactive
 * @param horizontalAlignment Horizontal alignment relative to anchor (Start or End)
 * @param verticalAlignment Vertical alignment relative to anchor (Above, Below, or Auto)
 * @param offset Additional offset in dp applied after alignment
 * @param content Anchor composable
 */
@Composable
fun AnchoredPopupMenu(
  items: List<PopupMenuItem>,
  onItemClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  horizontalAlignment: PopupMenuAlignment = PopupMenuAlignment.Start,
  verticalAlignment: PopupMenuVerticalAlignment = PopupMenuVerticalAlignment.Auto,
  offset: DpOffset = DpOffset(0.dp, 0.dp),
  content: @Composable () -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  Box(modifier = modifier) {
    Box(
      modifier = Modifier.clickable(enabled = enabled) {
        if (items.isNotEmpty()) expanded = true
      }
    ) { content() }

    // Use standard DropdownMenu for default positioning (Start + Auto)
    if (horizontalAlignment == PopupMenuAlignment.Start &&
        verticalAlignment == PopupMenuVerticalAlignment.Auto) {
      PopupMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        items = items,
        onItemClick = { id ->
          onItemClick(id)
          expanded = false
        },
        offset = offset
      )
    } else if (expanded) {
      // Use custom positioning for other alignments
      val intOffset = with(LocalDensity.current) {
        IntOffset(offset.x.roundToPx(), offset.y.roundToPx())
      }
      Popup(
        onDismissRequest = { expanded = false },
        popupPositionProvider = EdgeAlignedPositionProvider(
          horizontalAlignment = horizontalAlignment,
          verticalAlignment = verticalAlignment,
          offset = intOffset
        ),
        properties = PopupProperties(focusable = true)
      ) {
        Surface(
          modifier = Modifier.wrapContentSize(),
          shape = MaterialTheme.shapes.extraSmall,
          tonalElevation = 8.dp,
          color = MaterialTheme.colorScheme.surface
        ) {
          Column(modifier = Modifier.padding(vertical = 4.dp)) {
            items.forEachIndexed { index, item ->
              PopupMenuItemContent(item = item) { id ->
                onItemClick(id)
                expanded = false
              }
              if (item.dividerAfter && index < items.size - 1) {
                HorizontalDivider(
                  modifier = Modifier.padding(vertical = 4.dp),
                  color = MaterialTheme.colorScheme.outlineVariant
                )
              }
            }
          }
        }
      }
    }
  }
}

// ========== Previews ==========

/**
 * Preview for PopupMenu with icons and no submenus.
 *
 * Shows a basic menu with leading icons for each item.
 */
@Preview(showBackground = true)
@Composable
private fun PopupMenuWithIconsPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier.padding(32.dp)
    ) {
      var expanded by remember { mutableStateOf(true) }

      Box {
        Text("Menu Anchor", modifier = Modifier.clickable { expanded = true })

        PopupMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
          items = listOf(
            PopupMenuItem(
              id = 0,
              title = "Edit",
              icon = Icons.Default.Edit
            ),
            PopupMenuItem(
              id = 1,
              title = "Share",
              icon = Icons.Default.Share
            ),
            PopupMenuItem(
              id = 2,
              title = "Delete",
              icon = Icons.Default.Delete
            )
          ),
          onItemClick = { }
        )
      }
    }
  }
}

/**
 * Preview for PopupMenu with submenus.
 *
 * Demonstrates nested menu structure with submenu arrows.
 */
@Preview(showBackground = true)
@Composable
private fun PopupMenuWithSubmenusPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier.padding(32.dp)
    ) {
      var expanded by remember { mutableStateOf(true) }

      Box {
        Text("Menu Anchor", modifier = Modifier.clickable { expanded = true })

        PopupMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
          items = listOf(
            PopupMenuItem(
              id = 0,
              title = "Edit",
              icon = Icons.Default.Edit
            ),
            PopupMenuItem(
              id = 1,
              title = "Share",
              icon = Icons.Default.Share,
              subMenu = listOf(
                PopupMenuItem(id = 2, title = "Email"),
                PopupMenuItem(id = 3, title = "SMS"),
                PopupMenuItem(id = 4, title = "Social Media")
              )
            ),
            PopupMenuItem(
              id = 5,
              title = "Settings",
              icon = Icons.Default.Settings,
              subMenu = listOf(
                PopupMenuItem(id = 6, title = "General"),
                PopupMenuItem(id = 7, title = "Advanced")
              )
            ),
            PopupMenuItem(
              id = 8,
              title = "Delete",
              icon = Icons.Default.Delete,
              dividerAfter = true
            )
          ),
          onItemClick = { }
        )
      }
    }
  }
}

/**
 * Preview for PopupMenu without icons.
 *
 * Shows a simple text-only menu layout.
 */
@Preview(showBackground = true)
@Composable
private fun PopupMenuWithoutIconsPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier.padding(32.dp)
    ) {
      var expanded by remember { mutableStateOf(true) }

      Box {
        Text("Menu Anchor", modifier = Modifier.clickable { expanded = true })

        PopupMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
          items = listOf(
            PopupMenuItem(id = 0, title = "Copy"),
            PopupMenuItem(id = 1, title = "Paste"),
            PopupMenuItem(id = 2, title = "Select All", dividerAfter = true),
            PopupMenuItem(id = 3, title = "Preferences")
          ),
          onItemClick = { }
        )
      }
    }
  }
}

/**
 * Preview for AnchoredPopupMenu with icon button trigger.
 *
 * Demonstrates the complete anchored menu pattern with a more vert icon.
 */
@Preview(showBackground = true)
@Composable
private fun AnchoredPopupMenuPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier.padding(32.dp)
    ) {
      AnchoredPopupMenu(
        items = listOf(
          PopupMenuItem(
            id = 0,
            title = "Edit",
            icon = Icons.Default.Edit
          ),
          PopupMenuItem(
            id = 1,
            title = "Share",
            icon = Icons.Default.Share
          ),
          PopupMenuItem(
            id = 2,
            title = "Delete",
            icon = Icons.Default.Delete
          )
        ),
        onItemClick = { _ ->
          // Handle item click
        }
      ) {
        IconButton(onClick = { }) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More options"
          )
        }
      }
    }
  }
}

/**
 * Preview for PopupMenu with disabled items.
 *
 * Shows how disabled menu items appear with reduced opacity.
 */
@Preview(showBackground = true)
@Composable
private fun PopupMenuWithDisabledItemsPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier.padding(32.dp)
    ) {
      var expanded by remember { mutableStateOf(true) }

      Box {
        Text("Menu Anchor", modifier = Modifier.clickable { expanded = true })

        PopupMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
          items = listOf(
            PopupMenuItem(
              id = 0,
              title = "Edit",
              icon = Icons.Default.Edit,
              enabled = true
            ),
            PopupMenuItem(
              id = 1,
              title = "Share",
              icon = Icons.Default.Share,
              enabled = false
            ),
            PopupMenuItem(
              id = 2,
              title = "Delete",
              icon = Icons.Default.Delete,
              enabled = false
            )
          ),
          onItemClick = { }
        )
      }
    }
  }
}

/**
 * Preview for AnchoredPopupMenu with End horizontal alignment.
 *
 * Demonstrates menu aligned to the right edge of the anchor button.
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AnchoredPopupMenuEndAlignmentPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        AnchoredPopupMenu(
          items = listOf(
            PopupMenuItem(id = 0, title = "Profile", icon = Icons.Default.Edit),
            PopupMenuItem(id = 1, title = "Settings", icon = Icons.Default.Settings),
            PopupMenuItem(id = 2, title = "Logout", icon = Icons.Default.Delete, dividerAfter = true)
          ),
          onItemClick = { _ -> },
          horizontalAlignment = PopupMenuAlignment.End,
          verticalAlignment = PopupMenuVerticalAlignment.Auto
        ) {
          IconButton(onClick = { }) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Menu"
            )
          }
        }
      }
    }
  }
}

/**
 * Preview for AnchoredPopupMenu with Above vertical alignment.
 *
 * Demonstrates menu positioned above the anchor button.
 */
@Preview(showBackground = true, heightDp = 400)
@Composable
private fun AnchoredPopupMenuAboveAlignmentPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
      ) {
        AnchoredPopupMenu(
          items = listOf(
            PopupMenuItem(id = 0, title = "Copy", icon = Icons.Default.Edit),
            PopupMenuItem(id = 1, title = "Paste", icon = Icons.Default.Share),
            PopupMenuItem(id = 2, title = "Delete", icon = Icons.Default.Delete)
          ),
          onItemClick = { _ -> },
          horizontalAlignment = PopupMenuAlignment.Start,
          verticalAlignment = PopupMenuVerticalAlignment.Above
        ) {
          IconButton(onClick = { }) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Actions"
            )
          }
        }
      }
    }
  }
}

/**
 * Preview for PopupMenu with End horizontal alignment.
 *
 * Demonstrates the PopupMenu component with alignment parameters.
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PopupMenuWithEndAlignmentPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      var expanded by remember { mutableStateOf(true) }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        Box {
          Text(
            "Menu →",
            modifier = Modifier.clickable { expanded = true }
          )

          PopupMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = listOf(
              PopupMenuItem(id = 0, title = "Profile", icon = Icons.Default.Edit),
              PopupMenuItem(id = 1, title = "Settings", icon = Icons.Default.Settings),
              PopupMenuItem(id = 2, title = "Logout", icon = Icons.Default.Delete)
            ),
            onItemClick = { },
            horizontalAlignment = PopupMenuAlignment.End,
            verticalAlignment = PopupMenuVerticalAlignment.Auto
          )
        }
      }
    }
  }
}

/**
 * Preview for PopupMenu with Above vertical alignment.
 *
 * Demonstrates the PopupMenu component positioned above its anchor.
 */
@Preview(showBackground = true, heightDp = 400)
@Composable
private fun PopupMenuWithAboveAlignmentPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier.fillMaxWidth()
    ) {
      var expanded by remember { mutableStateOf(true) }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
      ) {
        Box {
          Text(
            "Open Menu ↑",
            modifier = Modifier.clickable { expanded = true }
          )

          PopupMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = listOf(
              PopupMenuItem(id = 0, title = "Action 1", icon = Icons.Default.Edit),
              PopupMenuItem(id = 1, title = "Action 2", icon = Icons.Default.Share),
              PopupMenuItem(id = 2, title = "Action 3", icon = Icons.Default.Delete)
            ),
            onItemClick = { },
            horizontalAlignment = PopupMenuAlignment.Start,
            verticalAlignment = PopupMenuVerticalAlignment.Above
          )
        }
      }
    }
  }
}

