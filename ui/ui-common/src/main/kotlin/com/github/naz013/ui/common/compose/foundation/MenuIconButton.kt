package com.github.naz013.ui.common.compose.foundation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MenuIconButton(
  modifier: Modifier = Modifier,
  icon: ImageVector,
  contentDescription: String? = null,
  color: Color = Color.Transparent,
  iconColor: Color = MaterialTheme.colorScheme.onSurface,
  disabledColor: Color = Color.Transparent,
  disabledIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  TooltipIconButton(modifier = modifier, contentDescription = contentDescription) {
    IconButton(
      onClick = onClick,
      enabled = enabled,
      colors = IconButtonDefaults.iconButtonColors(
        containerColor = color,
        contentColor = iconColor,
        disabledContainerColor = disabledColor,
        disabledContentColor = disabledIconColor
      ),
      shape = IconButtonDefaults.outlinedShape,
      content = {
        Icon(
          imageVector = icon,
          contentDescription = contentDescription
        )
      }
    )
  }
}

@Composable
fun MenuIconButton(
  modifier: Modifier = Modifier,
  icon: Painter,
  contentDescription: String? = null,
  color: Color = Color.Transparent,
  iconColor: Color = MaterialTheme.colorScheme.onSurface,
  disabledColor: Color = Color.Transparent,
  disabledIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  TooltipIconButton(modifier = modifier, contentDescription = contentDescription) {
    IconButton(
      onClick = onClick,
      enabled = enabled,
      colors = IconButtonDefaults.iconButtonColors(
        containerColor = color,
        contentColor = iconColor,
        disabledContainerColor = disabledColor,
        disabledContentColor = disabledIconColor
      ),
      shape = IconButtonDefaults.outlinedShape,
      content = {
        Icon(
          painter = icon,
          contentDescription = contentDescription
        )
      }
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun MenuIconButtonPreview() {
  MenuIconButton(
    icon = Icons.Default.Menu,
    contentDescription = "Menu",
    onClick = { }
  )
}

@Preview(showBackground = true)
@Composable
private fun MenuIconButtonPreview_Disabled() {
  MenuIconButton(
    icon = Icons.Default.Menu,
    contentDescription = "Menu",
    enabled = false,
    onClick = { }
  )
}
