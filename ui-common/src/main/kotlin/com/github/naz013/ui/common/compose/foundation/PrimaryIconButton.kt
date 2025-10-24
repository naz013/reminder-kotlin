package com.github.naz013.ui.common.compose.foundation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
fun PrimaryIconButton(
  modifier: Modifier = Modifier,
  icon: ImageVector,
  contentDescription: String? = null,
  color: Color = MaterialTheme.colorScheme.primaryContainer,
  iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
  disabledColor: Color = MaterialTheme.colorScheme.surfaceContainer,
  disabledIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  IconButton(
    modifier = modifier,
    onClick = onClick,
    enabled = enabled,
    colors = IconButtonDefaults.outlinedIconButtonColors(
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

@Composable
fun PrimaryIconButton(
  modifier: Modifier = Modifier,
  icon: Painter,
  contentDescription: String? = null,
  color: Color = MaterialTheme.colorScheme.primaryContainer,
  iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
  disabledColor: Color = MaterialTheme.colorScheme.surfaceContainer,
  disabledIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  IconButton(
    modifier = modifier,
    onClick = onClick,
    enabled = enabled,
    colors = IconButtonDefaults.outlinedIconButtonColors(
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

// Preview functions
@Preview(showBackground = true, name = "Primary Icon Button - Enabled")
@Composable
private fun PrimaryIconButtonPreview_Enabled() {
  MaterialTheme {
    PrimaryIconButton(
      icon = Icons.Default.Add,
      contentDescription = "Add",
      enabled = true,
      onClick = { }
    )
  }
}

@Preview(showBackground = true, name = "Primary Icon Button - Disabled")
@Composable
private fun PrimaryIconButtonPreview_Disabled() {
  MaterialTheme {
    PrimaryIconButton(
      icon = Icons.Default.Delete,
      contentDescription = "Delete",
      enabled = false,
      onClick = { }
    )
  }
}

@Preview(showBackground = true, name = "Primary Icon Button - Edit Icon")
@Composable
private fun PrimaryIconButtonPreview_Edit() {
  MaterialTheme {
    PrimaryIconButton(
      icon = Icons.Default.Edit,
      color = MaterialTheme.colorScheme.tertiaryContainer,
      iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
      contentDescription = "Edit",
      onClick = { }
    )
  }
}
