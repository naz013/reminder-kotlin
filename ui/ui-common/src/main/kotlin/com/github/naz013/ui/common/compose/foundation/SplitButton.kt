package com.github.naz013.ui.common.compose.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Split Button component that divides a button into two interactive sections.
 *
 * This component follows the Material Design 3 specification for split buttons, providing
 * a primary action on the left and a secondary action (typically a dropdown trigger) on the right.
 * Both sides are fully customizable in terms of content, styling, and behavior.
 *
 * @param onLeftClick Callback invoked when the left side of the button is clicked
 * @param onRightClick Callback invoked when the right side of the button is clicked
 * @param leftContent The composable content to display on the left side of the button
 * @param rightContent The composable content to display on the right side of the button
 * @param modifier Modifier to be applied to the split button container
 * @param enabled Controls the enabled state of the entire button. When false, both sides are disabled
 * @param cornerRadius The corner radius for the button's rounded corners
 * @param containerColor The background color of the button
 * @param contentColor The color for content inside the button
 * @param disabledContainerColor The background color when the button is disabled
 * @param disabledContentColor The content color when the button is disabled
 * @param border Optional border to draw around the button
 * @param dividerColor The color of the divider between left and right sections
 * @param dividerThickness The thickness of the divider line
 * @param leftInteractionSource MutableInteractionSource for the left side button interactions
 * @param rightInteractionSource MutableInteractionSource for the right side button interactions
 * @param contentPadding The padding to apply to the content inside both sections
 */
@Composable
fun SplitButton(
  onLeftClick: () -> Unit,
  onRightClick: () -> Unit,
  leftContent: @Composable RowScope.() -> Unit,
  rightContent: @Composable RowScope.() -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  cornerRadius: Dp = 16.dp,
  containerColor: Color = MaterialTheme.colorScheme.primary,
  contentColor: Color = MaterialTheme.colorScheme.onPrimary,
  disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
  disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
  border: BorderStroke? = null,
  dividerColor: Color = Color.Transparent,
  dividerThickness: Dp = 1.dp,
  leftInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  rightInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
  val currentContainerColor = if (enabled) containerColor else disabledContainerColor
  val currentContentColor = if (enabled) contentColor else disabledContentColor

  Surface(
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left button section
      Surface(
        onClick = onLeftClick,
        modifier = Modifier.weight(1f).fillMaxHeight(),
        enabled = enabled,
        shape = RoundedCornerShape(
          topStart = cornerRadius,
          topEnd = 0.dp,
          bottomStart = cornerRadius,
          bottomEnd = 0.dp
        ),
        color = currentContainerColor,
        contentColor = currentContentColor,
        interactionSource = leftInteractionSource
      ) {
        Row(
          modifier = Modifier.padding(contentPadding),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
          content = leftContent
        )
      }

      // Divider
      HorizontalDivider(
        modifier = Modifier
          .fillMaxHeight()
          .width(dividerThickness),
        thickness = dividerThickness,
        color = dividerColor
      )

      // Right button section
      Surface(
        onClick = onRightClick,
        modifier = Modifier.fillMaxHeight(),
        enabled = enabled,
        shape = RoundedCornerShape(
          topStart = 0.dp,
          topEnd = cornerRadius,
          bottomStart = 0.dp,
          bottomEnd = cornerRadius
        ),
        color = currentContainerColor,
        contentColor = currentContentColor,
        interactionSource = rightInteractionSource
      ) {
        Row(
          modifier = Modifier.padding(contentPadding),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
          content = rightContent
        )
      }
    }
  }
}

/**
 * Material 3 Split Button component with simplified parameters for icon and text content.
 *
 * This convenience overload accepts individual icon and text parameters instead of content lambdas,
 * making it easier to create standard split buttons with icon+text on the left and an icon on the right.
 *
 * @param text The text to display on the left side of the button
 * @param onLeftClick Callback invoked when the left side of the button is clicked
 * @param onRightClick Callback invoked when the right side of the button is clicked
 * @param leftIcon Optional icon to display on the left side before the text
 * @param rightIcon Icon to display on the right side of the button
 * @param modifier Modifier to be applied to the split button container
 * @param enabled Controls the enabled state of the entire button. When false, both sides are disabled
 * @param cornerRadius The corner radius for the button's rounded corners
 * @param containerColor The background color of the button
 * @param contentColor The color for content inside the button
 * @param disabledContainerColor The background color when the button is disabled
 * @param disabledContentColor The content color when the button is disabled
 * @param border Optional border to draw around the button
 * @param dividerColor The color of the divider between left and right sections
 * @param dividerThickness The thickness of the divider line
 * @param leftIconContentDescription Content description for the left icon (accessibility)
 * @param rightIconContentDescription Content description for the right icon (accessibility)
 * @param leftInteractionSource MutableInteractionSource for the left side button interactions
 * @param rightInteractionSource MutableInteractionSource for the right side button interactions
 * @param contentPadding The padding to apply to the content inside both sections
 */
@Composable
fun SplitButton(
  text: String,
  onLeftClick: () -> Unit,
  onRightClick: () -> Unit,
  leftIcon: ImageVector?,
  rightIcon: ImageVector,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  cornerRadius: Dp = 16.dp,
  containerColor: Color = MaterialTheme.colorScheme.primary,
  contentColor: Color = MaterialTheme.colorScheme.onPrimary,
  disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
  disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
  border: BorderStroke? = null,
  dividerColor: Color = Color.Transparent,
  dividerThickness: Dp = 1.dp,
  leftIconContentDescription: String? = null,
  rightIconContentDescription: String? = null,
  leftInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  rightInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
  SplitButton(
    onLeftClick = onLeftClick,
    onRightClick = onRightClick,
    leftContent = {
      if (leftIcon != null) {
        Icon(
          imageVector = leftIcon,
          contentDescription = leftIconContentDescription
        )
      }
      Text(
        text = text,
        modifier = if (leftIcon != null) Modifier.padding(start = 8.dp) else Modifier
      )
    },
    rightContent = {
      Icon(
        imageVector = rightIcon,
        contentDescription = rightIconContentDescription
      )
    },
    modifier = modifier,
    enabled = enabled,
    cornerRadius = cornerRadius,
    containerColor = containerColor,
    contentColor = contentColor,
    disabledContainerColor = disabledContainerColor,
    disabledContentColor = disabledContentColor,
    border = border,
    dividerColor = dividerColor,
    dividerThickness = dividerThickness,
    leftInteractionSource = leftInteractionSource,
    rightInteractionSource = rightInteractionSource,
    contentPadding = contentPadding
  )
}

/**
 * Material 3 Split Button component with simplified parameters for icon and text content using Painter.
 *
 * This convenience overload accepts Painter for icons instead of ImageVector,
 * making it easier to use custom drawable resources.
 *
 * @param text The text to display on the left side of the button
 * @param onLeftClick Callback invoked when the left side of the button is clicked
 * @param onRightClick Callback invoked when the right side of the button is clicked
 * @param leftIcon Optional icon painter to display on the left side before the text
 * @param rightIcon Icon painter to display on the right side of the button
 * @param modifier Modifier to be applied to the split button container
 * @param enabled Controls the enabled state of the entire button. When false, both sides are disabled
 * @param cornerRadius The corner radius for the button's container
 * @param containerColor The background color of the button
 * @param contentColor The color for content inside the button
 * @param disabledContainerColor The background color when the button is disabled
 * @param disabledContentColor The content color when the button is disabled
 * @param border Optional border to draw around the button
 * @param dividerColor The color of the divider between left and right sections
 * @param dividerThickness The thickness of the divider line
 * @param leftIconContentDescription Content description for the left icon (accessibility)
 * @param rightIconContentDescription Content description for the right icon (accessibility)
 * @param leftInteractionSource MutableInteractionSource for the left side button interactions
 * @param rightInteractionSource MutableInteractionSource for the right side button interactions
 * @param contentPadding The padding to apply to the content inside both sections
 */
@Composable
fun SplitButton(
  text: String,
  onLeftClick: () -> Unit,
  onRightClick: () -> Unit,
  leftIcon: Painter?,
  rightIcon: Painter,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  cornerRadius: Dp = 16.dp,
  containerColor: Color = MaterialTheme.colorScheme.primary,
  contentColor: Color = MaterialTheme.colorScheme.onPrimary,
  disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
  disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
  border: BorderStroke? = null,
  dividerColor: Color = Color.Transparent,
  dividerThickness: Dp = 1.dp,
  leftIconContentDescription: String? = null,
  rightIconContentDescription: String? = null,
  leftInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  rightInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
  SplitButton(
    onLeftClick = onLeftClick,
    onRightClick = onRightClick,
    leftContent = {
      if (leftIcon != null) {
        Icon(
          painter = leftIcon,
          contentDescription = leftIconContentDescription
        )
      }
      Text(
        text = text,
        modifier = if (leftIcon != null) Modifier.padding(start = 8.dp) else Modifier
      )
    },
    rightContent = {
      Icon(
        painter = rightIcon,
        contentDescription = rightIconContentDescription
      )
    },
    modifier = modifier,
    enabled = enabled,
    cornerRadius = cornerRadius,
    containerColor = containerColor,
    contentColor = contentColor,
    disabledContainerColor = disabledContainerColor,
    disabledContentColor = disabledContentColor,
    border = border,
    dividerColor = dividerColor,
    dividerThickness = dividerThickness,
    leftInteractionSource = leftInteractionSource,
    rightInteractionSource = rightInteractionSource,
    contentPadding = contentPadding
  )
}

/**
 * Outlined variant of the Material 3 Split Button component.
 *
 * This variant provides an outlined style with transparent background and a border,
 * following the Material Design 3 specification for outlined buttons.
 *
 * @param onLeftClick Callback invoked when the left side of the button is clicked
 * @param onRightClick Callback invoked when the right side of the button is clicked
 * @param leftContent The composable content to display on the left side of the button
 * @param rightContent The composable content to display on the right side of the button
 * @param modifier Modifier to be applied to the split button container
 * @param enabled Controls the enabled state of the entire button. When false, both sides are disabled
 * @param cornerRadius The corner radius for the button's rounded corners
 * @param contentColor The color for content inside the button
 * @param disabledContentColor The content color when the button is disabled
 * @param borderColor The color of the button's border
 * @param disabledBorderColor The border color when the button is disabled
 * @param borderThickness The thickness of the border
 * @param dividerColor The color of the divider between left and right sections
 * @param dividerThickness The thickness of the divider line
 * @param leftInteractionSource MutableInteractionSource for the left side button interactions
 * @param rightInteractionSource MutableInteractionSource for the right side button interactions
 * @param contentPadding The padding to apply to the content inside both sections
 */
@Composable
fun OutlinedSplitButton(
  onLeftClick: () -> Unit,
  onRightClick: () -> Unit,
  leftContent: @Composable RowScope.() -> Unit,
  rightContent: @Composable RowScope.() -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  cornerRadius: Dp = 16.dp,
  contentColor: Color = MaterialTheme.colorScheme.primary,
  disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
  borderColor: Color = MaterialTheme.colorScheme.outline,
  disabledBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
  borderThickness: Dp = 1.dp,
  dividerColor: Color = Color.Transparent,
  dividerThickness: Dp = 1.dp,
  leftInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  rightInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
  val currentBorderColor = if (enabled) borderColor else disabledBorderColor

  SplitButton(
    onLeftClick = onLeftClick,
    onRightClick = onRightClick,
    leftContent = leftContent,
    rightContent = rightContent,
    modifier = modifier,
    enabled = enabled,
    cornerRadius = cornerRadius,
    containerColor = Color.Transparent,
    contentColor = contentColor,
    disabledContainerColor = Color.Transparent,
    disabledContentColor = disabledContentColor,
    border = BorderStroke(borderThickness, currentBorderColor),
    dividerColor = dividerColor,
    dividerThickness = dividerThickness,
    leftInteractionSource = leftInteractionSource,
    rightInteractionSource = rightInteractionSource,
    contentPadding = contentPadding
  )
}

/**
 * Outlined variant of the Material 3 Split Button with simplified parameters for icon and text content.
 *
 * This convenience overload accepts individual icon and text parameters instead of content lambdas,
 * making it easier to create standard outlined split buttons with icon+text on the left and an icon on the right.
 *
 * @param text The text to display on the left side of the button
 * @param onLeftClick Callback invoked when the left side of the button is clicked
 * @param onRightClick Callback invoked when the right side of the button is clicked
 * @param leftIcon Optional icon to display on the left side before the text
 * @param rightIcon Icon to display on the right side of the button
 * @param modifier Modifier to be applied to the split button container
 * @param enabled Controls the enabled state of the entire button. When false, both sides are disabled
 * @param cornerRadius The corner radius for the button's rounded corners
 * @param contentColor The color for content inside the button
 * @param disabledContentColor The content color when the button is disabled
 * @param borderColor The color of the button's border
 * @param disabledBorderColor The border color when the button is disabled
 * @param borderThickness The thickness of the border
 * @param dividerColor The color of the divider between left and right sections
 * @param dividerThickness The thickness of the divider line
 * @param leftIconContentDescription Content description for the left icon (accessibility)
 * @param rightIconContentDescription Content description for the right icon (accessibility)
 * @param leftInteractionSource MutableInteractionSource for the left side button interactions
 * @param rightInteractionSource MutableInteractionSource for the right side button interactions
 * @param contentPadding The padding to apply to the content inside both sections
 */
@Composable
fun OutlinedSplitButton(
  text: String,
  onLeftClick: () -> Unit,
  onRightClick: () -> Unit,
  leftIcon: ImageVector?,
  rightIcon: ImageVector,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  cornerRadius: Dp = 16.dp,
  contentColor: Color = MaterialTheme.colorScheme.primary,
  disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
  borderColor: Color = MaterialTheme.colorScheme.outline,
  disabledBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
  borderThickness: Dp = 1.dp,
  dividerColor: Color = Color.Transparent,
  dividerThickness: Dp = 1.dp,
  leftIconContentDescription: String? = null,
  rightIconContentDescription: String? = null,
  leftInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  rightInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
  OutlinedSplitButton(
    onLeftClick = onLeftClick,
    onRightClick = onRightClick,
    leftContent = {
      if (leftIcon != null) {
        Icon(
          imageVector = leftIcon,
          contentDescription = leftIconContentDescription
        )
      }
      Text(
        text = text,
        modifier = if (leftIcon != null) Modifier.padding(start = 8.dp) else Modifier
      )
    },
    rightContent = {
      Icon(
        imageVector = rightIcon,
        contentDescription = rightIconContentDescription
      )
    },
    modifier = modifier,
    enabled = enabled,
    cornerRadius = cornerRadius,
    contentColor = contentColor,
    disabledContentColor = disabledContentColor,
    borderColor = borderColor,
    disabledBorderColor = disabledBorderColor,
    borderThickness = borderThickness,
    dividerColor = dividerColor,
    dividerThickness = dividerThickness,
    leftInteractionSource = leftInteractionSource,
    rightInteractionSource = rightInteractionSource,
    contentPadding = contentPadding
  )
}

/**
 * Outlined variant of the Material 3 Split Button with simplified parameters for icon and text content using Painter.
 *
 * This convenience overload accepts Painter for icons instead of ImageVector,
 * making it easier to use custom drawable resources.
 *
 * @param text The text to display on the left side of the button
 * @param onLeftClick Callback invoked when the left side of the button is clicked
 * @param onRightClick Callback invoked when the right side of the button is clicked
 * @param leftIcon Optional icon painter to display on the left side before the text
 * @param rightIcon Icon painter to display on the right side of the button
 * @param modifier Modifier to be applied to the split button container
 * @param enabled Controls the enabled state of the entire button. When false, both sides are disabled
 * @param cornerRadius The corner radius for the button's container
 * @param contentColor The color for content inside the button
 * @param disabledContentColor The content color when the button is disabled
 * @param borderColor The color of the button's border
 * @param disabledBorderColor The border color when the button is disabled
 * @param borderThickness The thickness of the border
 * @param dividerColor The color of the divider between left and right sections
 * @param dividerThickness The thickness of the divider line
 * @param leftIconContentDescription Content description for the left icon (accessibility)
 * @param rightIconContentDescription Content description for the right icon (accessibility)
 * @param leftInteractionSource MutableInteractionSource for the left side button interactions
 * @param rightInteractionSource MutableInteractionSource for the right side button interactions
 * @param contentPadding The padding to apply to the content inside both sections
 */
@Composable
fun OutlinedSplitButton(
  text: String,
  onLeftClick: () -> Unit,
  onRightClick: () -> Unit,
  leftIcon: Painter?,
  rightIcon: Painter,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  cornerRadius: Dp = 16.dp,
  contentColor: Color = MaterialTheme.colorScheme.primary,
  disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
  borderColor: Color = MaterialTheme.colorScheme.outline,
  disabledBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
  borderThickness: Dp = 1.dp,
  dividerColor: Color = Color.Transparent,
  dividerThickness: Dp = 1.dp,
  leftIconContentDescription: String? = null,
  rightIconContentDescription: String? = null,
  leftInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  rightInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
  OutlinedSplitButton(
    onLeftClick = onLeftClick,
    onRightClick = onRightClick,
    leftContent = {
      if (leftIcon != null) {
        Icon(
          painter = leftIcon,
          contentDescription = leftIconContentDescription
        )
      }
      Text(
        text = text,
        modifier = if (leftIcon != null) Modifier.padding(start = 8.dp) else Modifier
      )
    },
    rightContent = {
      Icon(
        painter = rightIcon,
        contentDescription = rightIconContentDescription
      )
    },
    modifier = modifier,
    enabled = enabled,
    cornerRadius = cornerRadius,
    contentColor = contentColor,
    disabledContentColor = disabledContentColor,
    borderColor = borderColor,
    disabledBorderColor = disabledBorderColor,
    borderThickness = borderThickness,
    dividerColor = dividerColor,
    dividerThickness = dividerThickness,
    leftInteractionSource = leftInteractionSource,
    rightInteractionSource = rightInteractionSource,
    contentPadding = contentPadding
  )
}

// Preview functions
@Preview(showBackground = true, name = "Split Button - Filled")
@Composable
private fun SplitButtonPreview_Filled() {
  MaterialTheme {
    SplitButton(
      onLeftClick = { },
      onRightClick = { },
      leftContent = {
        Icon(
          imageVector = Icons.Default.SaveAlt,
          contentDescription = "Save"
        )
        Text(
          text = "Save",
          modifier = Modifier.padding(start = 8.dp)
        )
      },
      rightContent = {
        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = "More options"
        )
      },
      modifier = Modifier.padding(16.dp).height(56.dp),
      cornerRadius = 28.dp
    )
  }
}

@Preview(showBackground = true, name = "Split Button - Outlined")
@Composable
private fun SplitButtonPreview_Outlined() {
  MaterialTheme {
    OutlinedSplitButton(
      onLeftClick = { },
      onRightClick = { },
      leftContent = {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete"
        )
        Text(
          text = "Delete",
          modifier = Modifier.padding(start = 8.dp)
        )
      },
      rightContent = {
        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = "More options"
        )
      },
      modifier = Modifier.padding(16.dp).height(56.dp),
      cornerRadius = 28.dp
    )
  }
}

@Preview(showBackground = true, name = "Split Button - Disabled")
@Composable
private fun SplitButtonPreview_Disabled() {
  MaterialTheme {
    SplitButton(
      onLeftClick = { },
      onRightClick = { },
      leftContent = {
        Icon(
          imageVector = Icons.Default.SaveAlt,
          contentDescription = "Save"
        )
        Text(
          text = "Save",
          modifier = Modifier.padding(start = 8.dp)
        )
      },
      rightContent = {
        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = "More options"
        )
      },
      enabled = false,
      modifier = Modifier.padding(16.dp).height(56.dp),
      cornerRadius = 28.dp
    )
  }
}

@Preview(showBackground = true, name = "Split Button - Custom Colors")
@Composable
private fun SplitButtonPreview_CustomColors() {
  MaterialTheme {
    SplitButton(
      onLeftClick = { },
      onRightClick = { },
      leftContent = {
        Text(text = "Custom")
      },
      rightContent = {
        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = "Expand"
        )
      },
      containerColor = MaterialTheme.colorScheme.tertiary,
      contentColor = MaterialTheme.colorScheme.onTertiary,
      modifier = Modifier.padding(16.dp).height(56.dp),
      cornerRadius = 28.dp
    )
  }
}

@Preview(showBackground = true, name = "Split Button - With Icon and Text")
@Composable
private fun SplitButtonPreview_IconAndText() {
  MaterialTheme {
    SplitButton(
      text = "Save",
      onLeftClick = { },
      onRightClick = { },
      leftIcon = Icons.Default.SaveAlt,
      rightIcon = Icons.Default.ArrowDropDown,
      leftIconContentDescription = "Save",
      rightIconContentDescription = "More options",
      modifier = Modifier.padding(16.dp).height(56.dp),
      cornerRadius = 28.dp
    )
  }
}

@Preview(showBackground = true, name = "Outlined Split Button - With Icon and Text")
@Composable
private fun OutlinedSplitButtonPreview_IconAndText() {
  MaterialTheme {
    OutlinedSplitButton(
      text = "Delete",
      onLeftClick = { },
      onRightClick = { },
      leftIcon = Icons.Default.Delete,
      rightIcon = Icons.Default.ArrowDropDown,
      leftIconContentDescription = "Delete",
      rightIconContentDescription = "More options",
      modifier = Modifier.padding(16.dp).height(56.dp),
      cornerRadius = 28.dp
    )
  }
}

@Preview(showBackground = true, name = "Split Button - Text Only on Left")
@Composable
private fun SplitButtonPreview_TextOnly() {
  MaterialTheme {
    SplitButton(
      text = "Action",
      onLeftClick = { },
      onRightClick = { },
      leftIcon = null,
      rightIcon = Icons.Default.ArrowDropDown,
      rightIconContentDescription = "More options",
      modifier = Modifier.padding(16.dp).height(56.dp),
      cornerRadius = 28.dp
    )
  }
}

