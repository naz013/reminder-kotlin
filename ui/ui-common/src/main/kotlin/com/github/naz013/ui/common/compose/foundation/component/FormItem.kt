package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.DisabledAlpha

private val ItemPadding = 16.dp
private val IconSize = 24.dp
private val IconSpacing = 20.dp
private val TrailingSpacing = 16.dp

/**
 * A single label/value row for editor-style screens (birthday/group/task editors, etc.) - the
 * non-settings equivalent of [SettingsItem]. Use this (not [SettingsItem]) for rows inside forms
 * that aren't a preferences/settings screen; [SettingsItem] stays reserved for screens under the
 * `settings` package (plus any deliberate exemptions), since it carries preference-list-specific
 * behavior (loading state, checkbox variant) a plain editor row doesn't need.
 *
 * Layout mirrors [SettingsItem]: optional leading [icon], [title] with an optional [subtitle]
 * below it, and an optional [trailing] slot - e.g. `trailing = { Text(currentValue) }` for a
 * tap-to-pick-a-value row.
 */
@Composable
fun FormItem(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  icon: Painter? = null,
  enabled: Boolean = true,
  dividerTop: Boolean = false,
  dividerBottom: Boolean = false,
  onClick: (() -> Unit)? = null,
  trailing: @Composable (() -> Unit)? = null,
) {
  val contentAlpha = if (enabled) 1f else DisabledAlpha

  Column(modifier = modifier.fillMaxWidth()) {
    if (dividerTop) HorizontalDivider()

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .then(
          if (onClick != null) {
            Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
          } else {
            Modifier
          }
        )
        .padding(ItemPadding),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (icon != null) {
        Icon(
          painter = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
          modifier = Modifier.size(IconSize)
        )
        Box(modifier = Modifier.width(IconSpacing))
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
          overflow = TextOverflow.Ellipsis
        )
        if (!subtitle.isNullOrEmpty()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
          )
        }
      }

      if (trailing != null) {
        Box(modifier = Modifier.padding(start = TrailingSpacing)) {
          trailing()
        }
      }
    }

    if (dividerBottom) HorizontalDivider()
  }
}

/** [FormItem] with a trailing [Switch]. Tapping anywhere on the row toggles it. */
@Composable
fun FormSwitchItem(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  icon: Painter? = null,
  enabled: Boolean = true,
  dividerTop: Boolean = false,
  dividerBottom: Boolean = false
) {
  FormItem(
    title = title,
    modifier = modifier,
    subtitle = subtitle,
    icon = icon,
    enabled = enabled,
    dividerTop = dividerTop,
    dividerBottom = dividerBottom,
    onClick = { onCheckedChange(!checked) },
    trailing = {
      Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
  )
}

@Preview(showBackground = true, name = "Form item - clickable with value")
@Composable
private fun PreviewFormItemClickable() {
  AppTheme {
    FormItem(
      title = "Select date",
      onClick = {},
      trailing = { Text("25 May, 2000", style = MaterialTheme.typography.titleMedium) },
    )
  }
}

@Preview(showBackground = true, name = "Form item - with icon and subtitle")
@Composable
private fun PreviewFormItemWithIcon() {
  AppTheme {
    FormItem(
      title = "Reminder",
      subtitle = "Every year on this day",
      icon = AppIcons.Fluent.Calendar,
      onClick = {},
    )
  }
}

@Preview(showBackground = true, name = "Form item - switch")
@Composable
private fun PreviewFormSwitchItem() {
  AppTheme {
    FormSwitchItem(
      title = "I don't know a year",
      checked = false,
      onCheckedChange = {},
      dividerBottom = true,
    )
  }
}
