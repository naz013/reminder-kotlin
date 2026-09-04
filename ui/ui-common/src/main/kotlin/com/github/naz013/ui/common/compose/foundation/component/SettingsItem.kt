package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.DisabledAlpha
import kotlinx.coroutines.delay

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
 *
 * [locked] is for a Pro-only feature shown to a free user: it dims the row the same way
 * [enabled]=false does, but — unlike [enabled] — never disables the click, since a locked row
 * still needs to be tappable to route to the paywall. Pass a locked-aware [onClick] that does
 * that routing. When [locked] is true and no explicit [trailing] is given, a [ProBadgeChip] is
 * shown automatically.
 *
 * [selected] gives the row a tonal background - for a two-pane list-detail layout (see
 * `DetailPanePlaceholder`), pass `true` for whichever row's destination is currently showing in
 * the detail pane, the same way a selected row is highlighted elsewhere in the app.
 *
 * [itemKey] is this row's stable identity for settings search: when it matches
 * [LocalSettingsHighlightKey] (provided once per screen by `SettingsHighlightScope` after a
 * search-result jump), the row scrolls itself into view and flashes its background once so the
 * user can find the setting they searched for. Leave it `null` for rows that aren't indexed by
 * search.
 */
@Composable
fun SettingsItem(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  icon: Painter? = null,
  enabled: Boolean = true,
  locked: Boolean = false,
  isLoading: Boolean = false,
  selected: Boolean = false,
  itemKey: String? = null,
  dividerTop: Boolean = false,
  dividerBottom: Boolean = false,
  onClick: (() -> Unit)? = null,
  trailing: @Composable (() -> Unit)? = null
) {
  val contentAlpha = if (enabled && !locked) 1f else DisabledAlpha
  val effectiveTrailing = trailing ?: if (locked) { @Composable { ProBadgeChip() } } else null

  val bringIntoViewRequester = remember { BringIntoViewRequester() }
  val flashColor = MaterialTheme.colorScheme.tertiaryContainer
  var isFlashing by remember { mutableStateOf(false) }
  val highlightBackground by animateColorAsState(
    targetValue = if (isFlashing) flashColor else Color.Transparent,
    animationSpec = tween(durationMillis = if (isFlashing) 150 else 900),
    label = "settingsItemHighlight",
  )

  val isHighlightTarget = itemKey != null && itemKey == LocalSettingsHighlightKey.current
  LaunchedEffect(isHighlightTarget) {
    if (isHighlightTarget) {
      bringIntoViewRequester.bringIntoView()
      isFlashing = true
      delay(600)
      isFlashing = false
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    if (dividerTop) HorizontalDivider()

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .bringIntoViewRequester(bringIntoViewRequester)
        .then(
          if (isFlashing || highlightBackground != Color.Transparent) {
            Modifier.background(highlightBackground)
          } else if (selected) {
            Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
          } else {
            Modifier
          }
        )
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

      if (effectiveTrailing != null) {
        Box(modifier = Modifier.padding(start = TrailingSpacing)) {
          effectiveTrailing()
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
  itemKey: String? = null,
  dividerTop: Boolean = false,
  dividerBottom: Boolean = false
) {
  SettingsItem(
    title = title,
    modifier = modifier,
    subtitle = if (checked) subtitleOn else subtitleOff,
    icon = icon,
    enabled = enabled,
    itemKey = itemKey,
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
  itemKey: String? = null,
  dividerTop: Boolean = false,
  dividerBottom: Boolean = false
) {
  SettingsItem(
    title = title,
    modifier = modifier,
    subtitle = subtitle,
    icon = icon,
    enabled = enabled,
    itemKey = itemKey,
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

@Preview(showBackground = true, name = "Settings item - locked (PRO)")
@Composable
private fun PreviewSettingsItemLocked() {
  AppTheme {
    SettingsItem(
      title = "Streaks & Insights",
      subtitle = "See your reminder streaks and trends",
      icon = AppIcons.Fluent.Settings,
      locked = true,
      dividerBottom = true,
      onClick = {}
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
