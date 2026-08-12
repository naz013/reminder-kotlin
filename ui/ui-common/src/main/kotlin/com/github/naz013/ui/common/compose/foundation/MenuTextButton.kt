package com.github.naz013.ui.common.compose.foundation

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MenuTextButton(
  modifier: Modifier = Modifier,
  text: String,
  color: Color = MaterialTheme.colorScheme.tertiary,
  disabledColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  TextButton(
    modifier = modifier,
    onClick = onClick,
    enabled = enabled,
    colors = ButtonDefaults.textButtonColors(
      contentColor = color,
      disabledContentColor = disabledColor,
    ),
    content = {
      Text(text = text)
    },
  )
}

@Preview(showBackground = true)
@Composable
private fun MenuTextButtonPreview() {
  MenuTextButton(
    text = "Save",
    onClick = { },
  )
}

@Preview(showBackground = true)
@Composable
private fun MenuTextButtonPreview_Disabled() {
  MenuTextButton(
    text = "Save",
    enabled = false,
    onClick = { },
  )
}
