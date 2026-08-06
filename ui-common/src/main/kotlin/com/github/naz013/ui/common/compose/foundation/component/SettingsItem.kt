package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
 * A single settings list row: leading icon, title/subtitle, and an optional trailing slot
 * (switch, checkbox, value text, custom button, chevron, etc). This is the Compose replacement
 * for the legacy `PrefsView` custom view, laid out to match it visually so migrated screens don't
 * shift.
 *
 * For a plain click-to-navigate row leave [trailing] null. For a value/button/icon on the
 * trailing edge, pass it directly, e.g. `trailing = { Text("Every 5 minutes") }`. For switches or
 * checkboxes, prefer [SettingsSwitchItem] / [SettingsCheckboxItem] below.
 *
 * Dependent enable/disable logic (e.g. "only enabled while PIN protection is on" in the legacy
 * `PrefsView.setDependentView`) is not part of this component — compute the combined [enabled]
 * value in the screen's ViewModel and pass it down, same as any other derived UI state.
 */
@Composable
fun SettingsItem(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  icon: Painter? = null,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  dividerTop: Boolean = false,
  dividerBottom: Boolean = false,
  onClick: (() -> Unit)? = null,
  trailing: @Composable (() -> Unit)? = null
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
      Box(modifier = Modifier.size(IconSize)) {
        if (icon != null) {
          Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = contentAlpha),
            modifier = Modifier.size(IconSize)
          )
        }
      }
      Box(modifier = Modifier.width(IconSpacing))

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

    if (isLoading) {
      LinearProgressIndicator(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = ItemPadding)
      )
    }

    if (dividerBottom) HorizontalDivider()
  }
}

/** [SettingsItem] with a trailing [Switch]. Tapping anywhere on the row toggles it, matching
 *  the legacy `PrefsView` switch rows. [subtitleOn]/[subtitleOff] swap based on [checked], for
 *  rows whose detail text describes the current state (e.g. "Notifications are enabled"). */
@Composable
fun SettingsSwitchItem(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  subtitleOn: String? = null,
  subtitleOff: String? = null,
  icon: Painter? = null,
  enabled: Boolean = true,
  dividerTop: Boolean = false,
  dividerBottom: Boolean = false
) {
  SettingsItem(
    title = title,
    modifier = modifier,
    subtitle = if (checked) subtitleOn else subtitleOff,
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

/** [SettingsItem] with a trailing [Checkbox]. Tapping anywhere on the row toggles it. */
@Composable
fun SettingsCheckboxItem(
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
  SettingsItem(
    title = title,
    modifier = modifier,
    subtitle = subtitle,
    icon = icon,
    enabled = enabled,
    dividerTop = dividerTop,
    dividerBottom = dividerBottom,
    onClick = { onCheckedChange(!checked) },
    trailing = {
      Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
  )
}

/** Section label for grouping [SettingsItem] rows, e.g. "Notification" above a block of related
 *  rows - mirrors the plain `TextView` section headers used between `PrefsView` rows. */
@Composable
fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.tertiary,
    modifier = modifier.padding(start = ItemPadding, top = 24.dp, bottom = 8.dp),
  )
}

@Preview(showBackground = true, name = "Settings item - clickable")
@Composable
private fun PreviewSettingsItemClickable() {
  AppTheme {
    SettingsItem(
      title = "Application language",
      icon = AppIcons.Fluent.Settings,
      dividerBottom = true,
      onClick = {}
    )
  }
}

@Preview(showBackground = true, name = "Settings item - with value")
@Composable
private fun PreviewSettingsItemWithValue() {
  AppTheme {
    SettingsItem(
      title = "Dark mode",
      subtitle = null,
      icon = AppIcons.Fluent.Settings,
      dividerBottom = true,
      onClick = {},
      trailing = { Text("Auto", style = MaterialTheme.typography.titleMedium) }
    )
  }
}

@Preview(showBackground = true, name = "Settings item - custom button")
@Composable
private fun PreviewSettingsItemCustomButton() {
  AppTheme {
    SettingsItem(
      title = "Cache",
      subtitle = "128 MB used",
      dividerBottom = true,
      trailing = { TextButton(onClick = {}) { Text("Clear") } }
    )
  }
}

@Preview(showBackground = true, name = "Settings item - switch on")
@Composable
private fun PreviewSettingsSwitchOn() {
  AppTheme {
    SettingsSwitchItem(
      title = "Dynamic colors",
      checked = true,
      onCheckedChange = {},
      subtitleOn = "Use theme colors from wallpaper",
      subtitleOff = "Use built-in theme color",
      icon = AppIcons.Fluent.Settings,
      dividerBottom = true
    )
  }
}

@Preview(showBackground = true, name = "Settings item - switch disabled")
@Composable
private fun PreviewSettingsSwitchDisabled() {
  AppTheme {
    SettingsSwitchItem(
      title = "Shuffle PIN view",
      checked = false,
      onCheckedChange = {},
      subtitleOn = "PIN pad order is randomized",
      subtitleOff = "PIN pad order is fixed",
      enabled = false,
      dividerBottom = true
    )
  }
}

@Preview(showBackground = true, name = "Settings item - checkbox")
@Composable
private fun PreviewSettingsCheckbox() {
  AppTheme {
    SettingsCheckboxItem(
      title = "Send crash reports",
      checked = true,
      onCheckedChange = {},
      dividerBottom = true
    )
  }
}
